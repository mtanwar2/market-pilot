package com.tanwar.market_pilot.llm.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "llm")
data class LlmProperties(
    val provider: String,
    val providers: Map<String, ProviderConfig>
) {

    data class ProviderConfig(
        val model: String,
        val apiKey: String? = null,
        val baseUrl: String? = null
    )
}