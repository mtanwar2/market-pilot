package com.tanwar.market_pilot.llm.client.resilience

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.exception.LlmException
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

class CircuitBreakerLlmClientTest {

    @Test
    fun `opens circuit after configured failure threshold`() {
        val calls = AtomicInteger()
        val delegate = object : LlmClient {
            override fun generate(request: LlmRequest): LlmResponse {
                calls.incrementAndGet()
                throw LlmException(503, "unavailable")
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
            assertThrows(LlmException::class.java) {
                client.generate(request)
            }
        }

        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.state)
        assertThrows(CallNotPermittedException::class.java) {
            client.generate(request)
        }
        assertEquals(2, calls.get())
    }
}
