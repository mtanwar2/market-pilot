package com.tanwar.market_pilot.llm.client

import com.tanwar.market_pilot.llm.client.resilience.CircuitBreakerLlmClient
import com.tanwar.market_pilot.llm.client.resilience.LlmCircuitBreakerFactory
import com.tanwar.market_pilot.llm.client.resilience.ResilientLlmClient
import com.tanwar.market_pilot.llm.client.resilience.LlmRetryPolicy
import com.tanwar.market_pilot.llm.properties.LlmProperties
import com.tanwar.market_pilot.llm.properties.RetryProperties
import org.springframework.stereotype.Component

@Component
class LlmClientFactory(
    private val llmClients: Map<String, LlmClient>,
    private val circuitBreakerFactory: LlmCircuitBreakerFactory,
    private val retryPolicy: LlmRetryPolicy,
    private val retryProperties: RetryProperties,
    private val llmProperties: LlmProperties
) {

    fun getClient(): LlmClient {

        val provider = llmProperties.provider
        val client = llmClients[provider]
            ?: throw IllegalArgumentException(
                "Unsupported LLM provider: $provider"
            )

        val retryClient =
            ResilientLlmClient(
                client = client,
                retryPolicy = retryPolicy,
                retryProperties = retryProperties
            )

        val circuitBreaker =
            circuitBreakerFactory.getCircuitBreaker(provider)

        return CircuitBreakerLlmClient(
            delegate = retryClient,
            circuitBreaker = circuitBreaker
        )
    }
}