package com.tanwar.market_pilot.llm.client.resilience

import com.tanwar.market_pilot.exception.LlmRetryExhaustedException
import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.exception.LlmException
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.model.TokenUsage
import com.tanwar.market_pilot.llm.properties.RetryProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

class ResilientLlmClientTest {

    private val retryProperties = RetryProperties(
        maxAttempts = 3,
        initialBackoffMs = 0,
        maxBackoffMs = 0,
        jitterFactor = 0.0
    )
    private val retryPolicy = LlmRetryPolicy()
    private val backoffCalculator =
        RetryBackoffCalculator(retryProperties)
    private val request = LlmRequest(messages = emptyList())

    @Test
    fun `retries transient failure and returns successful response`() {
        val calls = AtomicInteger()
        val expected = response()
        val delegate = client {
            if (calls.incrementAndGet() < 3) {
                throw LlmException(503, "temporarily unavailable")
            }
            expected
        }

        val actual = resilient(delegate).generate(request)

        assertEquals(expected, actual)
        assertEquals(3, calls.get())
    }

    @Test
    fun `does not retry non-transient failure`() {
        val calls = AtomicInteger()
        val delegate = client {
            calls.incrementAndGet()
            throw LlmException(400, "bad request")
        }

        assertThrows(LlmException::class.java) {
            resilient(delegate).generate(request)
        }
        assertEquals(1, calls.get())
    }

    @Test
    fun `throws exhausted exception after maximum attempts`() {
        val calls = AtomicInteger()
        val delegate = client {
            calls.incrementAndGet()
            throw LlmException(429, "rate limited")
        }

        val exception = assertThrows(
            LlmRetryExhaustedException::class.java
        ) {
            resilient(delegate).generate(request)
        }

        assertEquals(3, exception.attempts)
        assertEquals(503, exception.statusCode)
        assertEquals(3, calls.get())
    }

    private fun resilient(delegate: LlmClient) =
        ResilientLlmClient(
            client = delegate,
            retryPolicy = retryPolicy,
            backoffCalculator = backoffCalculator,
            retryProperties = retryProperties
        )

    private fun client(block: () -> LlmResponse) =
        object : LlmClient {
            override fun generate(request: LlmRequest): LlmResponse = block()
        }

    private fun response() = LlmResponse(
        content = "ok",
        usage = TokenUsage(1, 1, 2),
        finishReason = "stop"
    )
}
