package com.tanwar.market_pilot.service

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.client.LlmClientFactory
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.model.TokenUsage
import com.tanwar.market_pilot.llm.properties.LlmProperties
import com.tanwar.market_pilot.model.ChatRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ChatServiceTest {

    private val fakeLlmClient = mock<LlmClient>()

    private val properties = LlmProperties(
        provider = "fake",
        providers = mapOf(
            "fake" to LlmProperties.ProviderConfig(
                model = "fake-model",
                apiKey = "fake-key"
            )
        )
    )

    private val llmClients = mapOf(
        "fake" to fakeLlmClient
    )

    private val llmClientFactory = mock<LlmClientFactory>()

    private val chatService = ChatService(
        llmClientFactory
    )

    @Test
    fun `should return response for valid request`() {

        whenever(fakeLlmClient.generate(any()))
            .thenReturn(
                LlmResponse(
                    content = "This is a response from the fake LLM.",
                    usage = TokenUsage(0, 0, 0),
                    finishReason = "stop"
                )
            )

        val request = ChatRequest(
            conversationId = null,
            message = "Hello"
        )

        val response = chatService.chat(request)

        assertNotNull(response)
        assertEquals(
            "This is a response from the fake LLM.",
            response.message
        )
        assertNotNull(response.conversationId)
    }

    @Test
    fun `should reject empty message`() {

        val request = ChatRequest(
            conversationId = null,
            message = ""
        )

        assertThrows(IllegalArgumentException::class.java) {
            chatService.chat(request)
        }
    }

    @Test
    fun `should return LLM response correctly`() {

        whenever(fakeLlmClient.generate(any()))
            .thenReturn(
                LlmResponse(
                    content = "Hello from LLM",
                    usage = TokenUsage(10, 5, 15),
                    finishReason = "stop"
                )
            )

        val request = ChatRequest(
            conversationId = "conversation-123",
            message = "Hello"
        )

        val response = chatService.chat(request)

        assertEquals("Hello from LLM", response.message)
        assertEquals("conversation-123", response.conversationId)
    }

    @Test
    fun `should propagate LLM failure`() {

        whenever(fakeLlmClient.generate(any()))
            .thenThrow(RuntimeException("LLM unavailable"))

        val request = ChatRequest(
            conversationId = null,
            message = "Hello"
        )

        val exception = assertThrows(RuntimeException::class.java) {
            chatService.chat(request)
        }

        assertEquals("LLM unavailable", exception.message)
    }

    @Test
    fun `should call LLM generate`() {

        whenever(fakeLlmClient.generate(any()))
            .thenReturn(
                LlmResponse(
                    content = "Fake response",
                    usage = TokenUsage(0, 0, 0),
                    finishReason = "stop"
                )
            )

        val request = ChatRequest(
            conversationId = "conversation-123",
            message = "Hello"
        )

        chatService.chat(request)

        verify(fakeLlmClient).generate(any())
    }
}