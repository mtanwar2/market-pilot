package com.tanwar.market_pilot.llm.client.resilience

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import org.slf4j.LoggerFactory

class CircuitBreakerLlmClient(
    private val delegate: LlmClient,
    private val circuitBreaker: CircuitBreaker
) : LlmClient {

    override fun generate(request: LlmRequest): LlmResponse {
        try {
            return circuitBreaker.executeSupplier {
                delegate.generate(request)
            }
        } catch (ex: CallNotPermittedException) {
            log.warn(
                "LLM call rejected by circuit breaker name={} state={}",
                circuitBreaker.name,
                circuitBreaker.state
            )
            throw ex
        }
    }

    companion object {
        private val log =
            LoggerFactory.getLogger(CircuitBreakerLlmClient::class.java)
    }
}