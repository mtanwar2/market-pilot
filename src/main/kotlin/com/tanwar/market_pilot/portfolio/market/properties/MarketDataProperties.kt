package com.tanwar.market_pilot.portfolio.market.properties

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "market-data")
data class MarketDataProperties(

    @field:NotBlank
    val provider: String,

    @field:NotBlank
    val apiKey: String,

    @field:NotBlank
    val baseUrl: String,

    @field:Valid
    val rateLimit: RateLimitProperties = RateLimitProperties()
) {
    data class RateLimitProperties(
        val minIntervalMs: Long = 1100
    )
}
