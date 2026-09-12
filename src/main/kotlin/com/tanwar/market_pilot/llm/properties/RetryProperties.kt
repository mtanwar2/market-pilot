package com.tanwar.market_pilot.llm.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "llm.retry")
data class RetryProperties(
    val maxAttempts: Int = 3,
    val initialBackoffMs: Long = 1000,
    val jitterFactor: Double = 0.5
)