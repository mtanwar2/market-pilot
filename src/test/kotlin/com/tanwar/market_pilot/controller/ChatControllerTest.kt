package com.tanwar.market_pilot.controller

import com.tanwar.market_pilot.config.RequestCorrelationFilter
import com.tanwar.market_pilot.exception.GlobalExceptionHandler
import com.tanwar.market_pilot.llm.exception.LlmException
import com.tanwar.market_pilot.model.ChatResponse
import com.tanwar.market_pilot.service.ChatService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

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
            .build()
    }

    @Test
    fun `rejects blank message with structured validation error`() {
        mockMvc.perform(
            post("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"message":"   "}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.details.message").exists())
            .andExpect(header().exists(RequestCorrelationFilter.REQUEST_ID_HEADER))
    }

    @Test
    fun `returns bad gateway without exposing provider details`() {
        whenever(chatService.chat(any()))
            .thenThrow(LlmException(401, "secret provider detail"))

        mockMvc.perform(
            post("/chat")
                .header(RequestCorrelationFilter.REQUEST_ID_HEADER, "request-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"message":"Hello"}""")
        )
            .andExpect(status().isBadGateway)
            .andExpect(jsonPath("$.code").value("LLM_PROVIDER_ERROR"))
            .andExpect(jsonPath("$.message").value(
                "The LLM provider could not complete the request"
            ))
            .andExpect(jsonPath("$.requestId").value("request-123"))
            .andExpect(jsonPath("$.turnId").doesNotExist())
            .andExpect(header().string(
                RequestCorrelationFilter.REQUEST_ID_HEADER,
                "request-123"
            ))
    }

    @Test
    fun `returns conversation and turn identifiers`() {
        whenever(chatService.chat(any()))
            .thenReturn(
                ChatResponse(
                    message = "Hello from LLM",
                    conversationId = "conversation-123",
                    turnId = "turn-456"
                )
            )

        mockMvc.perform(
            post("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"message":"Hello","conversationId":"conversation-123"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.conversationId").value("conversation-123"))
            .andExpect(jsonPath("$.turnId").value("turn-456"))
            .andExpect(header().string(ChatController.CONVERSATION_ID_HEADER, "conversation-123"))
            .andExpect(header().string(ChatController.TURN_ID_HEADER, "turn-456"))
    }
}
