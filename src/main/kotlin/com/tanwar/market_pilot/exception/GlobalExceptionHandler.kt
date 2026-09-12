package com.tanwar.market_pilot.exception

import com.fasterxml.jackson.annotation.JsonInclude
import com.tanwar.market_pilot.config.ChatLogContext
import com.tanwar.market_pilot.llm.exception.LlmException
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(LlmRetryExhaustedException::class)
    fun handleLlmRetryExhausted(
        ex: LlmRetryExhaustedException
    ): ResponseEntity<ApiErrorResponse> {
        log.warn(
            "LLM retries exhausted attempts={} reason={}",
            ex.attempts,
            ex.cause?.message
        )

        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(
                error(
                    code = "LLM_SERVICE_UNAVAILABLE",
                    message = "LLM service is temporarily unavailable",
                    details = mapOf("attempts" to ex.attempts.toString())
                )
            )
    }

    @ExceptionHandler(CallNotPermittedException::class)
    fun handleOpenCircuit(
        ex: CallNotPermittedException
    ): ResponseEntity<ApiErrorResponse> {
        log.warn(
            "LLM request rejected because circuit is open circuitBreaker={}",
            ex.causingCircuitBreakerName
        )
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(
                error(
                    code = "LLM_CIRCUIT_OPEN",
                    message = "LLM service is temporarily unavailable"
                )
            )
    }

    @ExceptionHandler(LlmException::class)
    fun handleLlmException(
        ex: LlmException
    ): ResponseEntity<ApiErrorResponse> {
        log.warn(
            "LLM request failed statusCode={} reason={}",
            ex.statusCode,
            ex.message
        )
        return ResponseEntity
            .status(HttpStatus.BAD_GATEWAY)
            .body(
                error(
                    code = "LLM_PROVIDER_ERROR",
                    message = "The LLM provider could not complete the request"
                )
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<ApiErrorResponse> {
        val details = ex.bindingResult.fieldErrors.associate { fieldError ->
            fieldError.field to (fieldError.defaultMessage ?: "Invalid value")
        }
        log.warn("Chat request validation failed details={}", details)
        return ResponseEntity
            .badRequest()
            .body(
                error(
                    code = "VALIDATION_ERROR",
                    message = "Request validation failed",
                    details = details
                )
            )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableRequest(
        ex: HttpMessageNotReadableException
    ): ResponseEntity<ApiErrorResponse> {
        log.warn(
            "Malformed chat request exceptionType={}",
            ex.mostSpecificCause.javaClass.simpleName
        )
        return ResponseEntity
            .badRequest()
            .body(
                error(
                    code = "MALFORMED_REQUEST",
                    message = "Request body is missing or malformed"
                )
            )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException
    ): ResponseEntity<ApiErrorResponse> {
        log.warn("Invalid request reason={}", ex.message)
        return ResponseEntity
            .badRequest()
            .body(
                error(
                    code = "INVALID_REQUEST",
                    message = ex.message ?: "Invalid request"
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(
        ex: Exception
    ): ResponseEntity<ApiErrorResponse> {
        log.error(
            "Unhandled request failure exceptionType={}",
            ex.javaClass.simpleName,
            ex
        )
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                error(
                    code = "INTERNAL_SERVER_ERROR",
                    message = "An unexpected error occurred"
                )
            )
    }

    private fun error(
        code: String,
        message: String,
        details: Map<String, String>? = null
    ) = ApiErrorResponse(
        timestamp = Instant.now(),
        requestId = MDC.get(ChatLogContext.REQUEST_ID),
        conversationId = MDC.get(ChatLogContext.CONVERSATION_ID),
        turnId = MDC.get(ChatLogContext.TURN_ID),
        code = code,
        message = message,
        details = details
    )

    companion object {
        private val log =
            LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiErrorResponse(
    val timestamp: Instant,
    val requestId: String?,
    val conversationId: String?,
    val turnId: String?,
    val code: String,
    val message: String,
    val details: Map<String, String>? = null
)