package com.tanwar.market_pilot.controller

import com.tanwar.market_pilot.config.RequestCorrelationFilter
import com.tanwar.market_pilot.exception.GlobalExceptionHandler
import com.tanwar.market_pilot.exception.LlmRetryExhaustedException
import com.tanwar.market_pilot.llm.exception.LlmException
import com.tanwar.market_pilot.model.ChatResponse
import com.tanwar.market_pilot.service.ChatService
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.slf4j.MDC
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import reactor.core.publisher.Mono

class ChatControllerTest {

    private val chatService = mock<ChatService>()
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        val validator = LocalValidatorFactoryBean()
        validator.afterPropertiesSet()

        mockMvc = MockMvcBuilders
            .standaloneSetup(ChatController(chatService))
            .setControllerAdvice(GlobalExceptionHandler())
            .setValidator(validator)
            .addFilters<StandaloneMockMvcBuilder>(RequestCorrelationFilter())
            .setAsyncRequestTimeout(5_000)
            .build()
    }

    @AfterEach
    fun tearDown() {
        MDC.clear()
    }

    @Test
    fun `rejects blank message with structured validation error`() {
        mockMvc.perform(
            post("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(chatBody(turnId = "turn-1", messagesJson = """[{"role":"user","content":"   "}]"""))
        )
            .awaitIfAsync()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.details['messages[0].content']").exists())
            .andExpect(header().exists(RequestCorrelationFilter.REQUEST_ID_HEADER))
    }

    @Test
    fun `rejects missing turnId with structured validation error`() {
        mockMvc.perform(
            post("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"messages":[{"role":"user","content":"Hello"}]}""")
        )
            .awaitIfAsync()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.details.turnId").exists())
    }

    @Test
    fun `rejects invalid turnId characters`() {
        mockMvc.perform(
            post("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(chatBody(turnId = "turn 456"))
        )
            .awaitIfAsync()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.details.turnId").exists())
    }

    @Test
    fun `rejects blank conversationId`() {
        mockMvc.perform(
            post("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(chatBody(turnId = "turn-1", conversationId = "   "))
        )
            .awaitIfAsync()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.details.conversationId").exists())
    }

    @Test
    fun `rejects malformed json`() {
        mockMvc.perform(
            post("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{")
        )
            .awaitIfAsync()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"))
            .andExpect(header().exists(RequestCorrelationFilter.REQUEST_ID_HEADER))
    }

    @Test
    fun `generates requestId when client omits it`() {
        whenever(chatService.chat(any()))
            .thenReturn(
                Mono.just(
                    ChatResponse(
                        message = "ok",
                        conversationId = "conversation-123",
                        turnId = "turn-456"
                    )
                )
            )

        mockMvc.perform(
            post("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(chatBody(turnId = "turn-456"))
        )
            .awaitIfAsync()
            .andExpect(status().isOk)
            .andExpect(header().exists(RequestCorrelationFilter.REQUEST_ID_HEADER))
    }

    @Test
    fun `echoes valid client requestId`() {
        whenever(chatService.chat(any()))
            .thenReturn(
                Mono.just(
                    ChatResponse(
                        message = "ok",
                        conversationId = "conversation-123",
                        turnId = "turn-456"
                    )
                )
            )

        mockMvc.perform(
            post("/chat")
                .header(RequestCorrelationFilter.REQUEST_ID_HEADER, "request-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(chatBody(turnId = "turn-456"))
        )
            .awaitIfAsync()
            .andExpect(status().isOk)
            .andExpect(
                header().string(
                    RequestCorrelationFilter.REQUEST_ID_HEADER,
                    "request-123"
                )
            )
    }

    @Test
    fun `ignores invalid client requestId and generates a new one`() {
        whenever(chatService.chat(any()))
            .thenReturn(
                Mono.just(
                    ChatResponse(
                        message = "ok",
                        conversationId = "conversation-123",
                        turnId = "turn-456"
                    )
                )
            )

        mockMvc.perform(
            post("/chat")
                .header(RequestCorrelationFilter.REQUEST_ID_HEADER, "not a valid id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(chatBody(turnId = "turn-456"))
        )
            .awaitIfAsync()
            .andExpect(status().isOk)
            .andExpect(header().exists(RequestCorrelationFilter.REQUEST_ID_HEADER))
            .andExpect(
                header().string(
                    RequestCorrelationFilter.REQUEST_ID_HEADER,
                    org.hamcrest.Matchers.not("not a valid id")
                )
            )
    }

    @Test
    fun `returns conversation and turn identifiers`() {
        whenever(chatService.chat(any()))
            .thenReturn(
                Mono.just(
                    ChatResponse(
                        message = "Hello from LLM",
                        conversationId = "conversation-123",
                        turnId = "turn-456"
                    )
                )
            )

        mockMvc.perform(
            post("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(chatBody(turnId = "turn-456", conversationId = "conversation-123"))
        )
            .awaitIfAsync()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Hello from LLM"))
            .andExpect(jsonPath("$.conversationId").value("conversation-123"))
            .andExpect(jsonPath("$.turnId").value("turn-456"))
            .andExpect(header().string(ChatController.CONVERSATION_ID_HEADER, "conversation-123"))
            .andExpect(header().string(ChatController.TURN_ID_HEADER, "turn-456"))
    }

    @Test
    fun `returns bad gateway without exposing provider details`() {
        whenever(chatService.chat(any()))
            .thenReturn(Mono.error(LlmException(401, "secret provider detail")))

        mockMvc.perform(
            post("/chat")
                .header(RequestCorrelationFilter.REQUEST_ID_HEADER, "request-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(chatBody(turnId = "turn-456"))
        )
            .awaitIfAsync()
            .andExpect(status().isBadGateway)
            .andExpect(jsonPath("$.code").value("LLM_PROVIDER_ERROR"))
            .andExpect(
                jsonPath("$.message").value(
                    "The LLM provider could not complete the request"
                )
            )
            .andExpect(jsonPath("$.requestId").value("request-123"))
            .andExpect(jsonPath("$.turnId").value("turn-456"))
            .andExpect(
                header().string(
                    RequestCorrelationFilter.REQUEST_ID_HEADER,
                    "request-123"
                )
            )
    }

    @Test
    fun `returns service unavailable when retries are exhausted`() {
        whenever(chatService.chat(any()))
            .thenReturn(
                Mono.error(
                    LlmRetryExhaustedException(
                        attempts = 3,
                        cause = LlmException(503, "unavailable")
                    )
                )
            )

        mockMvc.perform(
            post("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(chatBody(turnId = "turn-1"))
        )
            .awaitIfAsync()
            .andExpect(status().isServiceUnavailable)
            .andExpect(jsonPath("$.code").value("LLM_SERVICE_UNAVAILABLE"))
            .andExpect(jsonPath("$.details.attempts").value("3"))
    }

    @Test
    fun `returns service unavailable when circuit is open`() {
        val circuitBreaker = CircuitBreaker.of(
            "llm-gemini",
            CircuitBreakerConfig.ofDefaults()
        )
        whenever(chatService.chat(any()))
            .thenReturn(
                Mono.error(CallNotPermittedException.createCallNotPermittedException(circuitBreaker))
            )

        mockMvc.perform(
            post("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(chatBody(turnId = "turn-1"))
        )
            .awaitIfAsync()
            .andExpect(status().isServiceUnavailable)
            .andExpect(jsonPath("$.code").value("LLM_CIRCUIT_OPEN"))
    }

    @Test
    fun `accepts a user assistant transcript`() {
        whenever(chatService.chat(any()))
            .thenReturn(
                Mono.just(
                    ChatResponse(
                        message = "4",
                        conversationId = "conversation-123",
                        turnId = "turn-2"
                    )
                )
            )

        mockMvc.perform(
            post("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    chatBody(
                        turnId = "turn-2",
                        conversationId = "conversation-123",
                        messagesJson = """
                            [
                              {"role":"user","content":"Hello"},
                              {"role":"assistant","content":"Hi"},
                              {"role":"user","content":"What is 2+2?"}
                            ]
                        """.trimIndent()
                    )
                )
        )
            .awaitIfAsync()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("4"))
            .andExpect(jsonPath("$.turnId").value("turn-2"))
    }

    @Test
    fun `rejects empty transcript`() {
        mockMvc.perform(
            post("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(chatBody(turnId = "turn-1", messagesJson = "[]"))
        )
            .awaitIfAsync()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.details.messages").exists())
    }

    private fun chatBody(
        turnId: String,
        conversationId: String? = null,
        messagesJson: String = """[{"role":"user","content":"Hello"}]"""
    ): String {
        val conversationPart = conversationId?.let { ""","conversationId":"$it"""" } ?: ""
        return """{"turnId":"$turnId"$conversationPart,"messages":$messagesJson}"""
    }

    private fun ResultActions.awaitIfAsync(): ResultActions {
        val result = andReturn()
        return if (result.request.isAsyncStarted) {
            mockMvc.perform(asyncDispatch(result))
        } else {
            this
        }
    }
}
