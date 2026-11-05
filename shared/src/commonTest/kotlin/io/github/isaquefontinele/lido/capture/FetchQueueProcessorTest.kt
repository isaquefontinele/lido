package io.github.isaquefontinele.lido.capture

import io.github.isaquefontinele.lido.db.LidoDatabase
import io.github.isaquefontinele.lido.library.LibraryRepository
import io.github.isaquefontinele.lido.library.createTestDriver
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FetchQueueProcessorTest {
    private val driver = createTestDriver()
    private val database = LidoDatabase(driver)
    private val repository = LibraryRepository(database, StandardTestDispatcher())
    private val policy = RetryPolicy(baseDelayMillis = 1_000, maxDelayMillis = 60_000, maxAttempts = 3)
    private var clock = 0L

    /** Network state for the fake server: offline throws, otherwise serves the sample article. */
    private var online = false
    private var status = HttpStatusCode.OK
    private val client = mockClient {
        when {
            !online -> throw IOException("Offline")
            status != HttpStatusCode.OK -> respondError(status)
            else -> respondHtml(SAMPLE_ARTICLE_HTML)
        }
    }
    private val pipeline = ArticlePipeline(repository, PageFetcher(client), ArticleExtractor(), now = { clock })
    private val processor = FetchQueueProcessor(database, pipeline, policy, now = { clock })

    @AfterTest
    fun tearDown() {
        driver.close()
    }

    private fun saveAndEnqueue(id: String): String {
        repository.save(id, "https://example.com/$id", null, savedAt = clock)
        processor.enqueue(id)
        return id
    }

    @Test
    fun itemSavedOfflineIsExtractedOnceTheNetworkIsBack() = runTest {
        val id = saveAndEnqueue("1")

        val offlineRun = processor.processDue()
        assertEquals(QueueRunResult(extracted = 0, rescheduled = 1, gaveUp = 0, nextAttemptAt = 1_000), offlineRun)
        assertEquals("Offline", processor.entry(id)?.lastError)
        assertNull(repository.item(id)?.extractedAt)

        online = true
        clock = 1_000
        val onlineRun = processor.processDue()

        assertEquals(QueueRunResult(extracted = 1, rescheduled = 0, gaveUp = 0, nextAttemptAt = null), onlineRun)
        assertNull(processor.entry(id), "extracted items leave the queue")
        assertNotNull(repository.content(id))
    }

    @Test
    fun itemsAreNotRetriedBeforeTheirBackoffExpires() = runTest {
        saveAndEnqueue("1")
        processor.processDue()

        online = true
        clock = 999
        assertEquals(0, processor.processDue().extracted)
    }

    @Test
    fun backoffGrowsWithEachFailedAttempt() = runTest {
        val id = saveAndEnqueue("1")

        processor.processDue()
        assertEquals(1_000, processor.entry(id)?.nextAttemptAt)

        clock = 1_000
        processor.processDue()
        assertEquals(2L, processor.entry(id)?.attempts)
        assertEquals(1_000 + 2_000, processor.entry(id)?.nextAttemptAt)
    }

    @Test
    fun givesUpAfterMaxAttempts() = runTest {
        val id = saveAndEnqueue("1")

        repeat(policy.maxAttempts) {
            processor.processDue()
            clock = processor.entry(id)?.nextAttemptAt ?: clock
        }

        val entry = assertNotNull(processor.entry(id), "given-up items stay visible in the queue")
        assertEquals(3L, entry.attempts)
        assertNull(entry.nextAttemptAt)
        assertNull(processor.nextAttemptAt())
    }

    @Test
    fun permanentErrorsAreNotRetried() = runTest {
        online = true
        status = HttpStatusCode.Gone
        val id = saveAndEnqueue("1")

        assertEquals(1, processor.processDue().gaveUp)
        assertEquals("HTTP 410", processor.entry(id)?.lastError)
        assertNull(processor.entry(id)?.nextAttemptAt)
    }

    @Test
    fun savingAgainResetsAGivenUpItem() = runTest {
        online = true
        status = HttpStatusCode.Gone
        val id = saveAndEnqueue("1")
        processor.processDue()

        status = HttpStatusCode.OK
        processor.enqueue(id)

        assertEquals(1, processor.processDue().extracted)
    }

    @Test
    fun schedulerQueuesNewItemsAndSkipsExtractedOnes() = runTest {
        var runs = 0
        val scheduler = QueuedExtractionScheduler(processor, repository) { runs++ }
        repository.save("1", "https://example.com/1", null, savedAt = 0)

        scheduler.schedule("1")
        assertNotNull(processor.entry("1"))
        assertEquals(1, runs)

        online = true
        processor.processDue()
        scheduler.schedule("1")

        assertNull(processor.entry("1"))
        assertEquals(1, runs)
    }

    @Test
    fun deletingAnItemRemovesItFromTheQueue() = runTest {
        val id = saveAndEnqueue("1")

        repository.delete(id)

        assertNull(processor.entry(id))
    }
}
