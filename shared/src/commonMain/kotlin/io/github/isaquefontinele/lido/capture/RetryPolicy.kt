package io.github.isaquefontinele.lido.capture

import kotlin.math.min

/** Exponential backoff for retryable fetch failures: base, 2×base, 4×base… capped at [maxDelayMillis]. */
data class RetryPolicy(
    val baseDelayMillis: Long = 30_000,
    val maxDelayMillis: Long = 6 * 60 * 60 * 1000L,
    val maxAttempts: Int = 10,
) {
    /** Delay before the next try after [attempts] failed attempts (attempts >= 1). */
    fun delayAfter(attempts: Int): Long {
        val doublings = (attempts - 1).coerceIn(0, 30)
        return min(baseDelayMillis shl doublings, maxDelayMillis)
    }
}
