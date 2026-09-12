package com.tanwar.market_pilot.llm.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "llm.timeout")
data class TimeoutProperties(
    val durationMs: Long = 10000
)