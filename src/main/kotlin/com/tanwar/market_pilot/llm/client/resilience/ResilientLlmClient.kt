package com.tanwar.market_pilot.llm.client.resilience

import com.tanwar.market_pilot.exception.LlmRetryExhaustedException
import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.exception.LlmException
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.properties.RetryProperties
import org.slf4j.LoggerFactory

class ResilientLlmClient(
    private val client: LlmClient,
    private val retryPolicy: LlmRetryPolicy,
    private val backoffCalculator: RetryBackoffCalculator,
    retryProperties: RetryProperties,
) : LlmClient {

    private val maxAttempts = retryProperties.maxAttempts

    override fun generate(
        request: LlmRequest
    ): LlmResponse {
        var attempt = 1

        while (attempt <= maxAttempts) {
            val startedAt = System.nanoTime()
            try {
                log.info(
                    "LLM attempt started: attempt={}/{}",
                    attempt,
                    maxAttempts
                )

                val response = client.generate(request)
                log.info(
                    "LLM attempt succeeded: attempt={}/{} durationMs={}",
                    attempt,
                    maxAttempts,
                    elapsedMs(startedAt)
                )
                return response

            } catch (ex: LlmException) {
                val retryable = retryPolicy.shouldRetry(ex.statusCode)
                log.warn(
                    "LLM attempt failed: attempt={}/{} durationMs={} statusCode={} retryable={} reason={}",
                    attempt,
                    maxAttempts,
                    elapsedMs(startedAt),
                    ex.statusCode,
                    retryable,
                    ex.message
                )

                if (!retryable) {
                    throw ex
                }

                if (attempt >= maxAttempts) {
                    throw LlmRetryExhaustedException(
                        attempts = attempt,
                        cause = ex
                    )
                }

                backoff(attempt)

                attempt++
            }
        }

        throw IllegalStateException("Unreachable")
    }

    private fun backoff(attempt: Int) {
        val delayMs =
            backoffCalculator.calculateDelay(attempt)

        log.info(
            "Waiting before LLM retry: completedAttempt={} delayMs={}",
            attempt,
            delayMs
        )

        try {
            Thread.sleep(delayMs)
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
            throw LlmException(
                statusCode = 503,
                message = "LLM retry interrupted",
                cause = ex
            )
        }
    }

    private fun elapsedMs(startedAt: Long): Long =
        (System.nanoTime() - startedAt) / 1_000_000

    companion object {
        private val log =
            LoggerFactory.getLogger(ResilientLlmClient::class.java)
    }
}