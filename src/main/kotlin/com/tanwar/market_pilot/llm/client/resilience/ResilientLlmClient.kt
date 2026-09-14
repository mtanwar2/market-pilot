package com.tanwar.market_pilot.llm.client.resilience

import com.tanwar.market_pilot.exception.LlmRetryExhaustedException
import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.exception.LlmException
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.properties.RetryProperties
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.time.Duration

class ResilientLlmClient(
    private val client: LlmClient,
    private val retryPolicy: LlmRetryPolicy,
    private val backoffCalculator: RetryBackoffCalculator,
    retryProperties: RetryProperties
) : LlmClient {

    private val maxAttempts = retryProperties.maxAttempts

    override fun generate(
        request: LlmRequest
    ): Mono<LlmResponse> {

        return Mono.defer {
            attempt(
                request = request,
                attempt = 1
            )
        }
    }

    private fun attempt(
        request: LlmRequest,
        attempt: Int
    ): Mono<LlmResponse> {

        val startedAt = System.nanoTime()

        log.info(
            "LLM attempt started: attempt={}/{}",
            attempt,
            maxAttempts
        )

        return client
            .generate(request)
            .doOnSuccess {
                log.info(
                    "LLM attempt succeeded: attempt={}/{} durationMs={}",
                    attempt,
                    maxAttempts,
                    elapsedMs(startedAt)
                )
            }
            .onErrorResume { ex ->

                if (ex !is LlmException) {
                    return@onErrorResume Mono.error(ex)
                }

                val retryable =
                    retryPolicy.shouldRetry(ex.statusCode)

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
                    return@onErrorResume Mono.error(ex)
                }

                if (attempt >= maxAttempts) {
                    return@onErrorResume Mono.error(
                        LlmRetryExhaustedException(
                            attempts = attempt,
                            cause = ex
                        )
                    )
                }

                val delayMs =
                    backoffCalculator.calculateDelay(attempt)

                log.info(
                    "Waiting before LLM retry: completedAttempt={} delayMs={}",
                    attempt,
                    delayMs
                )

                Mono.delay(Duration.ofMillis(delayMs))
                    .then(
                        attempt(
                            request = request,
                            attempt = attempt + 1
                        )
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