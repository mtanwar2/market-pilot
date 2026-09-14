package com.tanwar.market_pilot.llm.client.resilience

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.exception.LlmException
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.concurrent.atomic.AtomicInteger

class CircuitBreakerLlmClientTest {

    @Test
    fun `opens circuit after configured failure threshold`() {
        val calls = AtomicInteger()
        val delegate = object : LlmClient {
            override fun generate(request: LlmRequest): Mono<LlmResponse> {
                return Mono.defer {
                    calls.incrementAndGet()
                    Mono.error(LlmException(503, "unavailable"))
                }
            }
        }
        val circuitBreaker = CircuitBreaker.of(
            "test",
            CircuitBreakerConfig.custom()
                .slidingWindowSize(2)
                .minimumNumberOfCalls(2)
                .failureRateThreshold(50f)
                .build()
        )
        val client = CircuitBreakerLlmClient(delegate, circuitBreaker)
        val request = LlmRequest(emptyList())

        repeat(2) {
            StepVerifier.create(client.generate(request))
                .expectError(LlmException::class.java)
                .verify()
        }

        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.state)
        val callsWhenOpen = calls.get()
        StepVerifier.create(client.generate(request))
            .expectError(CallNotPermittedException::class.java)
            .verify()
        assertEquals(callsWhenOpen, calls.get())
        assertEquals(2, callsWhenOpen)
    }
}
