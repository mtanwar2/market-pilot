package com.tanwar.market_pilot.llm.properties

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "llm.retry")
data class RetryProperties(
    @field:Min(1)
    val maxAttempts: Int = 3,

    @field:Min(0)
    val initialBackoffMs: Long = 1000,

    @field:Min(0)
    val maxBackoffMs: Long = 10000,

    @field:DecimalMin("0.0")
    @field:DecimalMax("1.0")
    val jitterFactor: Double = 0.5
) {
    init {
        require(maxBackoffMs >= initialBackoffMs) {
            "llm.retry.max-backoff-ms must be greater than or equal to initial-backoff-ms"
        }
    }
}