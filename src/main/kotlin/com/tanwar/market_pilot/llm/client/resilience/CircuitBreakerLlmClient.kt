package com.tanwar.market_pilot.llm.client.resilience

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

class CircuitBreakerLlmClient(
    private val delegate: LlmClient,
    private val circuitBreaker: CircuitBreaker
) : LlmClient {

    override fun generate(
        request: LlmRequest
    ): Mono<LlmResponse> {

        return Mono.defer {
            delegate.generate(request)
        }
            .transformDeferred(
                CircuitBreakerOperator.of(circuitBreaker)
            )
            .doOnError { ex ->
                if (ex is CallNotPermittedException) {
                    log.warn(
                        "LLM call rejected by circuit breaker name={} state={}",
                        circuitBreaker.name,
                        circuitBreaker.state,
                        ex
                    )
                }
            }
    }

    companion object {
        private val log =
            LoggerFactory.getLogger(CircuitBreakerLlmClient::class.java)
    }
}
