package com.tanwar.market_pilot.llm.client

import com.tanwar.market_pilot.llm.client.resilience.CircuitBreakerLlmClient
import com.tanwar.market_pilot.llm.client.resilience.LlmCircuitBreakerFactory
import com.tanwar.market_pilot.llm.client.resilience.LlmRetryPolicy
import com.tanwar.market_pilot.llm.client.resilience.ResilientLlmClient
import com.tanwar.market_pilot.llm.client.resilience.RetryBackoffCalculator
import com.tanwar.market_pilot.llm.properties.LlmProperties
import com.tanwar.market_pilot.llm.properties.RetryProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LlmClientFactory(
    private val llmClients: Map<String, LlmClient>,
    private val circuitBreakerFactory: LlmCircuitBreakerFactory,
    private val retryPolicy: LlmRetryPolicy,
    private val backoffCalculator: RetryBackoffCalculator,
    private val retryProperties: RetryProperties,
    private val llmProperties: LlmProperties
) {

    private val resilientClients: Map<String, LlmClient> by lazy {
        llmClients.mapValues { (provider, client) ->
            val circuitBreakerClient = CircuitBreakerLlmClient(
                delegate = client,
                circuitBreaker = circuitBreakerFactory.getCircuitBreaker(provider)
            )

            ResilientLlmClient(
                client = circuitBreakerClient,
                retryPolicy = retryPolicy,
                backoffCalculator = backoffCalculator,
                retryProperties = retryProperties
            )
        }
    }

    fun getClient(): LlmClient {
        val provider = llmProperties.provider
        val client = resilientClients[provider]

        if (client == null) {
            log.error(
                "Unsupported LLM provider configured provider={} availableProviders={}",
                provider,
                resilientClients.keys.sorted()
            )
            throw IllegalArgumentException(
                "Unsupported LLM provider: $provider"
            )
        }

        log.debug(
            "Resolved resilient LLM client provider={}",
            provider
        )
        return client
    }

    companion object {
        private val log = LoggerFactory.getLogger(LlmClientFactory::class.java)
    }
}