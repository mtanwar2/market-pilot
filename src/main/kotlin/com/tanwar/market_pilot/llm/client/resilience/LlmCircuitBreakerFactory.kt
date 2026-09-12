package com.tanwar.market_pilot.llm.client.resilience

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.springframework.stereotype.Component

@Component
class LlmCircuitBreakerFactory(
    private val registry: CircuitBreakerRegistry
) {

    fun getCircuitBreaker(provider: String): CircuitBreaker {
        return registry.circuitBreaker("llm-$provider")
    }
}