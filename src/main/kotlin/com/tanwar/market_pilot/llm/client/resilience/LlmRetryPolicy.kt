package com.tanwar.market_pilot.llm.client.resilience

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LlmRetryPolicy {
    private val log =
        LoggerFactory.getLogger(LlmRetryPolicy::class.java)
    fun shouldRetry(statusCode: Int?): Boolean {
        return when (statusCode) {
            429, 500, 502, 503, 504, 523 -> true
            else -> false
        }
    }

}