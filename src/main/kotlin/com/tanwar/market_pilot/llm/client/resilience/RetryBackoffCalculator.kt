package com.tanwar.market_pilot.llm.client.resilience

import com.tanwar.market_pilot.llm.properties.RetryProperties
import org.springframework.stereotype.Component

@Component
class RetryBackoffCalculator(
    private val retryProperties: RetryProperties
) {

    fun calculateDelay(attempt: Int): Long {
        val exponentialDelay =
            retryProperties.initialBackoffMs *
                    (1L shl (attempt - 1))

        val jitter =
            (exponentialDelay * retryProperties.jitterFactor * Math.random())
                .toLong()

        return exponentialDelay + jitter
    }
}