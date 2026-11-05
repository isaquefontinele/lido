package io.github.isaquefontinele.lido.capture

import kotlin.test.Test
import kotlin.test.assertEquals

class RetryPolicyTest {
    private val policy = RetryPolicy(baseDelayMillis = 1_000, maxDelayMillis = 10_000)

    @Test
    fun delayDoublesAfterEachFailure() {
        assertEquals(listOf(1_000L, 2_000L, 4_000L, 8_000L), (1..4).map(policy::delayAfter))
    }

    @Test
    fun delayIsCapped() {
        assertEquals(10_000, policy.delayAfter(5))
        assertEquals(10_000, policy.delayAfter(1_000))
    }
}
