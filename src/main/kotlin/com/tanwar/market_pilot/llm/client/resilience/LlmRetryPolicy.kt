package com.tanwar.market_pilot.llm.client.resilience

import org.springframework.stereotype.Component

@Component
class LlmRetryPolicy {

    fun shouldRetry(statusCode: Int?): Boolean {
        return when (statusCode) {
            408, 425, 429, 500, 502, 503, 504, 523 -> true
            else -> false
        }
    }
}