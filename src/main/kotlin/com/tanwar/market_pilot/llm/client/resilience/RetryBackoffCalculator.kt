package com.tanwar.market_pilot.llm.client.resilience

import com.tanwar.market_pilot.llm.properties.RetryProperties
import org.springframework.stereotype.Component
import java.util.concurrent.ThreadLocalRandom

@Component
class RetryBackoffCalculator(
    private val retryProperties: RetryProperties
) {

    fun calculateDelay(attempt: Int): Long {
        require(attempt >= 1) { "Attempt must be at least 1" }

        var baseDelay = retryProperties.initialBackoffMs
            .coerceAtMost(retryProperties.maxBackoffMs)

        repeat(attempt - 1) {
            baseDelay = if (baseDelay > retryProperties.maxBackoffMs / 2) {
                retryProperties.maxBackoffMs
            } else {
                (baseDelay * 2).coerceAtMost(retryProperties.maxBackoffMs)
            }
        }

        val maximumJitter = minOf(
            (baseDelay * retryProperties.jitterFactor).toLong(),
            retryProperties.maxBackoffMs - baseDelay
        )
        val jitter = if (maximumJitter > 0) {
            ThreadLocalRandom.current().nextLong(maximumJitter + 1)
        } else {
            0
        }

        return baseDelay + jitter
    }
}