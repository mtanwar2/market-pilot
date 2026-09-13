package com.tanwar.market_pilot.service

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.client.LlmClientFactory
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.model.TokenUsage
import com.tanwar.market_pilot.model.ChatRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.slf4j.MDC

class ChatServiceTest {

    private val fakeLlmClient = mock<LlmClient>()
    private val llmClientFactory = mock<LlmClientFactory>()
    private val chatService = ChatService(
        llmClientFactory
    )

    @BeforeEach
    fun setUp() {
        whenever(llmClientFactory.getClient())
            .thenReturn(fakeLlmClient)
    }

    @AfterEach
    fun tearDown() {
        MDC.clear()
    }

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
            turnId = "turn-1",
            message = "Hello"
        )

        val response = chatService.chat(request)

        assertNotNull(response)
        assertEquals(
            "This is a response from the fake LLM.",
            response.message
        )
        assertNotNull(response.conversationId)
        assertEquals("turn-1", response.turnId)
    }

    @Test
    fun `should reject empty message`() {

        val request = ChatRequest(
            conversationId = null,
            turnId = "turn-empty",
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
            turnId = "turn-456",
            message = "Hello"
        )

        val response = chatService.chat(request)

        assertEquals("Hello from LLM", response.message)
        assertEquals("conversation-123", response.conversationId)
        assertEquals("turn-456", response.turnId)
    }

    @Test
    fun `should echo the client turnId for each message in the same conversation`() {
        whenever(fakeLlmClient.generate(any()))
            .thenReturn(
                LlmResponse(
                    content = "ok",
                    usage = TokenUsage(0, 0, 0),
                    finishReason = "stop"
                )
            )

        val first = chatService.chat(
            ChatRequest(
                conversationId = "conversation-123",
                turnId = "turn-1",
                message = "Hello"
            )
        )
        val second = chatService.chat(
            ChatRequest(
                conversationId = "conversation-123",
                turnId = "turn-2",
                message = "Follow up"
            )
        )

        assertEquals("conversation-123", first.conversationId)
        assertEquals("conversation-123", second.conversationId)
        assertEquals("turn-1", first.turnId)
        assertEquals("turn-2", second.turnId)
        assertNotEquals(first.turnId, second.turnId)
    }

    @Test
    fun `should not bind a turnId for rejected empty messages`() {
        assertThrows(IllegalArgumentException::class.java) {
            chatService.chat(
                ChatRequest(
                    conversationId = "conversation-123",
                    turnId = "turn-empty",
                    message = ""
                )
            )
        }

        assertNull(MDC.get("turnId"))
        assertNull(MDC.get("conversationId"))
    }

    @Test
    fun `should propagate LLM failure`() {

        whenever(fakeLlmClient.generate(any()))
            .thenThrow(RuntimeException("LLM unavailable"))

        val request = ChatRequest(
            conversationId = null,
            turnId = "turn-fail",
            message = "Hello"
        )

        val exception = assertThrows(RuntimeException::class.java) {
            chatService.chat(request)
        }

        assertEquals("LLM unavailable", exception.message)
        assertEquals("turn-fail", MDC.get("turnId"))
        assertNotNull(MDC.get("conversationId"))
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
            turnId = "turn-456",
            message = "Hello"
        )

        chatService.chat(request)

        val requestCaptor = argumentCaptor<LlmRequest>()
        verify(fakeLlmClient).generate(requestCaptor.capture())
        assertEquals(1, requestCaptor.firstValue.messages.size)
        assertEquals("Hello", requestCaptor.firstValue.messages.single().content)
    }
}