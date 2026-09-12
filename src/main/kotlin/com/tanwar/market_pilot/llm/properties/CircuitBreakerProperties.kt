package com.tanwar.market_pilot.llm.properties

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "llm.circuit-breaker")
data class CircuitBreakerProperties(
    @field:Min(2)
    val slidingWindowSize: Int = 10,

    @field:Min(1)
    val minimumNumberOfCalls: Int = 5,

    @field:DecimalMin("1.0")
    @field:DecimalMax("100.0")
    val failureRateThreshold: Float = 50f,

    @field:Min(1)
    val waitDurationInOpenStateMs: Long = 30000,

    @field:Min(1)
    val permittedCallsInHalfOpenState: Int = 2
) {
    init {
        require(minimumNumberOfCalls <= slidingWindowSize) {
            "llm.circuit-breaker.minimum-number-of-calls must not exceed sliding-window-size"
        }
    }
}
