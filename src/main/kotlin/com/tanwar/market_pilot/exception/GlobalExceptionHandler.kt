package com.tanwar.market_pilot.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(LlmRetryExhaustedException::class)
    fun handleLlmRetryExhausted(
        ex: LlmRetryExhaustedException
    ): ResponseEntity<Map<String, Any>> {

        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(
                mapOf(
                    "error" to "LLM_SERVICE_UNAVAILABLE",
                    "message" to "LLM service is temporarily unavailable",
                    "attempts" to ex.attempts
                )
            )
    }
}