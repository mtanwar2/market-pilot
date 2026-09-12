package com.tanwar.market_pilot.llm.client.resilience

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import io.github.resilience4j.circuitbreaker.CircuitBreaker

class CircuitBreakerLlmClient(
    private val delegate: LlmClient,
    private val circuitBreaker: CircuitBreaker
) : LlmClient {

    override fun generate(request: LlmRequest): LlmResponse {

        val decoratedSupplier =
            CircuitBreaker.decorateSupplier(
                circuitBreaker
            ) {
                delegate.generate(request)
            }

        return decoratedSupplier.get()
    }
}