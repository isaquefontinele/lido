package io.github.isaquefontinele.lido.capture

import io.github.isaquefontinele.lido.db.LidoDatabase
import io.github.isaquefontinele.lido.library.LibraryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import io.github.isaquefontinele.lido.db.FetchQueue as QueueEntry

data class QueueRunResult(
    val extracted: Int,
    val rescheduled: Int,
    val gaveUp: Int,
    /** When the next queued item is due, or null if nothing is waiting. */
    val nextAttemptAt: Long?,
)

/**
 * Persistent queue of items to fetch, so links saved offline are extracted once the network is back.
 * Retryable failures are rescheduled with [RetryPolicy] backoff; the rest are kept but given up on.
 */
class FetchQueueProcessor(
    private val database: LidoDatabase,
    private val pipeline: ArticlePipeline,
    private val policy: RetryPolicy,
    private val now: () -> Long,
) {
    private val queries get() = database.fetchQueueQueries
    private val mutex = Mutex()

    fun enqueue(itemId: String) {
        queries.enqueue(itemId, now())
    }

    fun entry(itemId: String): QueueEntry? = queries.selectByItemId(itemId).executeAsOneOrNull()

    fun nextAttemptAt(): Long? = queries.nextAttemptAt().executeAsOne().nextAttemptAt

    /** Processes every item that is due. Concurrent calls run one after the other. */
    suspend fun processDue(batchSize: Long = 20): QueueRunResult = mutex.withLock {
        var extracted = 0
        var rescheduled = 0
        var gaveUp = 0
        val handled = mutableSetOf<String>()
        while (true) {
            val due = queries.selectDue(now(), batchSize).executeAsList().filter { it.itemId !in handled }
            if (due.isEmpty()) break
            for (entry in due) {
                handled += entry.itemId
                when (val result = pipeline.process(entry.itemId)) {
                    PipelineResult.Extracted -> {
                        queries.remove(entry.itemId)
                        extracted++
                    }
                    is PipelineResult.Failed -> {
                        val attempts = entry.attempts + 1
                        if (result.retryable && attempts < policy.maxAttempts) {
                            queries.reschedule(
                                attempts = attempts,
                                nextAttemptAt = now() + policy.delayAfter(attempts.toInt()),
                                lastError = result.reason,
                                itemId = entry.itemId,
                            )
                            rescheduled++
                        } else {
                            queries.giveUp(attempts = attempts, lastError = result.reason, itemId = entry.itemId)
                            gaveUp++
                        }
                    }
                }
            }
        }
        QueueRunResult(extracted, rescheduled, gaveUp, nextAttemptAt())
    }
}

/** Asks the platform to run [FetchQueueProcessor.processDue] soon, ideally once a network is available. */
fun interface QueueRunTrigger {
    fun requestRun()
}

/** Queues saved items and wakes the platform runner, so extraction survives offline saves and restarts. */
class QueuedExtractionScheduler(
    private val processor: FetchQueueProcessor,
    private val repository: LibraryRepository,
    private val trigger: QueueRunTrigger,
) : ExtractionScheduler {
    override fun schedule(itemId: String) {
        // Saving an already extracted link again must not fetch it again.
        if (repository.item(itemId)?.extractedAt != null) return
        processor.enqueue(itemId)
        trigger.requestRun()
    }
}

/**
 * Runs the queue in-process: now, and again whenever the next retry is due, while the app is alive.
 * Used where no system scheduler is wired up yet (iOS).
 */
class InProcessQueueRunTrigger(
    private val scope: CoroutineScope,
    private val processor: FetchQueueProcessor,
    private val now: () -> Long,
) : QueueRunTrigger {
    private var job: Job? = null

    override fun requestRun() {
        job?.cancel()
        job = scope.launch {
            while (true) {
                val next = processor.processDue().nextAttemptAt ?: break
                delay((next - now()).coerceAtLeast(0))
            }
        }
    }
}
