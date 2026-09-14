package com.tanwar.market_pilot.llm.client.resilience

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LlmRetryPolicyTest {

    private val policy = LlmRetryPolicy()

    @Test
    fun `retries transient status codes`() {
        listOf(404, 408, 425, 429, 500, 502, 503, 504, 523).forEach { status ->
            assertTrue(policy.shouldRetry(status), "expected retry for $status")
        }
    }

    @Test
    fun `does not retry client or unknown failures`() {
        assertFalse(policy.shouldRetry(null))
        assertFalse(policy.shouldRetry(400))
        assertFalse(policy.shouldRetry(401))
    }
}
