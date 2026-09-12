package com.tanwar.market_pilot.llm.client.resilience

import com.tanwar.market_pilot.exception.LlmRetryExhaustedException
import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.client.resilience.LlmRetryPolicy
import com.tanwar.market_pilot.llm.client.resilience.RetryBackoffCalculator
import com.tanwar.market_pilot.llm.exception.LlmException
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.properties.RetryProperties
import org.slf4j.LoggerFactory

class ResilientLlmClient(
    private val client: LlmClient,
    private val retryPolicy: LlmRetryPolicy,
    retryProperties: RetryProperties,
) : LlmClient {

    private val log =
        LoggerFactory.getLogger(ResilientLlmClient::class.java)

    private val maxAttempts = retryProperties.maxAttempts
    private val backoffCalculator = RetryBackoffCalculator(retryProperties)

    override fun generate(
        request: LlmRequest
    ): LlmResponse {

        var attempt = 1

        while (attempt <= maxAttempts) {

            try {

                log.info(
                    "LLM attempt started: attempt={}/{}",
                    attempt,
                    maxAttempts
                )

                return client.generate(request)

            } catch (ex: LlmException) {

                log.warn(
                    "LLM attempt failed: attempt={}/{} reason={}",
                    attempt,
                    maxAttempts,
                    ex.message
                )

                if (!retryPolicy.shouldRetry(ex.statusCode)) {
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
            "Waiting before retry: attempt={}, delayMs={}",
            attempt,
            delayMs
        )

        Thread.sleep(delayMs)
    }
}