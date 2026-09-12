package com.tanwar.market_pilot.llm.properties

import jakarta.validation.constraints.Min
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "llm.timeout")
data class TimeoutProperties(
    @field:Min(1)
    val durationMs: Long = 10000
)