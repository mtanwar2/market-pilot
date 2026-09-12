package com.tanwar.market_pilot.llm.properties

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "llm")
data class LlmProperties(
    @field:NotBlank
    val provider: String,

    @field:NotEmpty
    @field:Valid
    val providers: Map<String, ProviderConfig>
) {

    data class ProviderConfig(
        @field:NotBlank
        val model: String,
        val apiKey: String? = null,
        val baseUrl: String? = null
    )
}