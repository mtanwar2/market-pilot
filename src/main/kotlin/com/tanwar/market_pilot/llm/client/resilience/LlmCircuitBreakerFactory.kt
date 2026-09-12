package com.tanwar.market_pilot.llm.client.resilience

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LlmCircuitBreakerFactory(
    private val registry: CircuitBreakerRegistry
) {

    fun getCircuitBreaker(provider: String): CircuitBreaker {
        val circuitBreaker = registry.circuitBreaker("llm-$provider")
        circuitBreaker.eventPublisher
            .onStateTransition { event ->
                log.warn(
                    "LLM circuit breaker state changed name={} transition={}",
                    event.circuitBreakerName,
                    event.stateTransition
                )
            }
        return circuitBreaker
    }

    companion object {
        private val log =
            LoggerFactory.getLogger(LlmCircuitBreakerFactory::class.java)
    }
}