package io.github.isaquefontinele.lido.capture

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

private const val WORK_NAME = "fetch-queue"

private val networkConstraint = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .build()

/** Processes the fetch queue when a network is available and schedules itself for the next retry. */
class FetchQueueWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {
    private val processor: FetchQueueProcessor by inject()

    override suspend fun doWork(): Result {
        val next = processor.processDue().nextAttemptAt
        if (next != null) {
            val delay = (next - System.currentTimeMillis()).coerceAtLeast(0)
            // Appended, so it starts after this run instead of cancelling it.
            WorkManager.getInstance(applicationContext)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request(delay))
        }
        return Result.success()
    }
}

/** Runs the queue through WorkManager, which waits for connectivity and survives process death. */
class WorkManagerQueueRunTrigger(private val context: Context) : QueueRunTrigger {
    override fun requestRun() {
        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request(delayMillis = 0))
    }
}

private fun request(delayMillis: Long) = OneTimeWorkRequestBuilder<FetchQueueWorker>()
    .setConstraints(networkConstraint)
    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
    .build()
