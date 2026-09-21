package com.tanwar.market_pilot.service

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.client.LlmClientFactory
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.model.LlmRole
import com.tanwar.market_pilot.llm.model.TokenUsage
import com.tanwar.market_pilot.llm.model.ToolCall
import com.tanwar.market_pilot.llm.tool.ToolExecutor
import com.tanwar.market_pilot.model.ChatMessage
import com.tanwar.market_pilot.model.ChatRequest
import com.tanwar.market_pilot.model.ChatRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Duration

class ChatServiceTest {

    private val fakeLlmClient = mock<LlmClient>()
    private val llmClientFactory = mock<LlmClientFactory>()
    private val toolExecutor = mock<ToolExecutor>()
    private val chatService = ChatService(llmClientFactory, toolExecutor)

    @BeforeEach
    fun setUp() {
        whenever(llmClientFactory.getClient())
            .thenReturn(fakeLlmClient)
    }

    @Test
    fun `should return response for valid request`() {
        whenever(fakeLlmClient.generate(any()))
            .thenReturn(Mono.just(llmResponse("This is a response from the fake LLM.")))

        val response = chatService.chat(
            chatRequest(turnId = "turn-1", message = "Hello")
        ).block(Duration.ofSeconds(1))

        assertNotNull(response)
        assertEquals("This is a response from the fake LLM.", response!!.message)
        assertNotNull(response.conversationId)
        assertEquals("turn-1", response.turnId)
    }

    @Test
    fun `should reject empty message`() {
        assertThrows(IllegalArgumentException::class.java) {
            chatService.chat(
                chatRequest(turnId = "turn-empty", message = "")
            )
        }
    }

    @Test
    fun `should reject missing turnId`() {
        assertThrows(IllegalArgumentException::class.java) {
            chatService.chat(
                chatRequest(
                    conversationId = "conversation-123",
                    turnId = null,
                    message = "Hello"
                )
            )
        }
    }

    @Test
    fun `should return LLM response correctly`() {
        whenever(fakeLlmClient.generate(any()))
            .thenReturn(Mono.just(llmResponse("Hello from LLM", TokenUsage(10, 5, 15))))

        val response = chatService.chat(
            chatRequest(
                conversationId = "conversation-123",
                turnId = "turn-456",
                message = "Hello"
            )
        ).block(Duration.ofSeconds(1))

        assertEquals("Hello from LLM", response!!.message)
        assertEquals("conversation-123", response.conversationId)
        assertEquals("turn-456", response.turnId)
    }

    @Test
    fun `should echo the client turnId for each message in the same conversation`() {
        whenever(fakeLlmClient.generate(any()))
            .thenReturn(Mono.just(llmResponse("ok")))

        val first = chatService.chat(
            chatRequest(
                conversationId = "conversation-123",
                turnId = "turn-1",
                message = "Hello"
            )
        ).block(Duration.ofSeconds(1))

        val second = chatService.chat(
            chatRequest(
                conversationId = "conversation-123",
                turnId = "turn-2",
                message = "Follow up"
            )
        ).block(Duration.ofSeconds(1))

        assertEquals("conversation-123", first!!.conversationId)
        assertEquals("conversation-123", second!!.conversationId)
        assertEquals("turn-1", first.turnId)
        assertEquals("turn-2", second.turnId)
        assertNotEquals(first.turnId, second.turnId)
    }

    @Test
    fun `should mint a conversationId when the client omits one`() {
        whenever(fakeLlmClient.generate(any()))
            .thenReturn(Mono.just(llmResponse("ok")))

        val first = chatService.chat(
            chatRequest(turnId = "turn-1", message = "Hello")
        ).block(Duration.ofSeconds(1))
        val second = chatService.chat(
            chatRequest(turnId = "turn-2", message = "Hello again")
        ).block(Duration.ofSeconds(1))

        assertNotNull(first!!.conversationId)
        assertNotNull(second!!.conversationId)
        assertNotEquals(first.conversationId, second.conversationId)
    }

    @Test
    fun `should propagate LLM failure`() {
        whenever(fakeLlmClient.generate(any()))
            .thenReturn(Mono.error(RuntimeException("LLM unavailable")))

        StepVerifier.create(
            chatService.chat(
                chatRequest(
                    conversationId = null,
                    turnId = "turn-fail",
                    message = "Hello"
                )
            )
        )
            .expectErrorMatches { error ->
                error is RuntimeException && error.message == "LLM unavailable"
            }
            .verify()
    }

    @Test
    fun `should call LLM generate with the user message`() {
        whenever(fakeLlmClient.generate(any()))
            .thenReturn(Mono.just(llmResponse("Fake response")))

        chatService.chat(
            chatRequest(
                conversationId = "conversation-123",
                turnId = "turn-456",
                message = "Hello"
            )
        ).block(Duration.ofSeconds(1))

        val requestCaptor = argumentCaptor<LlmRequest>()
        verify(fakeLlmClient).generate(requestCaptor.capture())
        val conversation = requestCaptor.firstValue.messages
            .filter { it.role != LlmRole.SYSTEM }
        assertEquals(1, conversation.size)
        assertEquals(LlmRole.USER, conversation.single().role)
        assertEquals("Hello", conversation.single().content)
        assertEquals("getStockPrice", requestCaptor.firstValue.tools.single().name)
    }

    @Test
    fun `should send the full user assistant transcript to the LLM`() {
        whenever(fakeLlmClient.generate(any()))
            .thenReturn(Mono.just(llmResponse("Sure")))

        chatService.chat(
            ChatRequest(
                conversationId = "conversation-123",
                turnId = "turn-2",
                messages = listOf(
                    ChatMessage(ChatRole.USER, "Hello"),
                    ChatMessage(ChatRole.ASSISTANT, "Hi, how can I help?"),
                    ChatMessage(ChatRole.USER, "What is 2+2?")
                )
            )
        ).block(Duration.ofSeconds(1))

        val requestCaptor = argumentCaptor<LlmRequest>()
        verify(fakeLlmClient).generate(requestCaptor.capture())
        val conversation = requestCaptor.firstValue.messages
            .filter { it.role != LlmRole.SYSTEM }
        assertEquals(3, conversation.size)
        assertEquals(LlmRole.USER, conversation[0].role)
        assertEquals("Hello", conversation[0].content)
        assertEquals(LlmRole.ASSISTANT, conversation[1].role)
        assertEquals("Hi, how can I help?", conversation[1].content)
        assertEquals(LlmRole.USER, conversation[2].role)
        assertEquals("What is 2+2?", conversation[2].content)
    }

    @Test
    fun `should instruct the model to answer only the final user message`() {
        whenever(fakeLlmClient.generate(any()))
            .thenReturn(Mono.just(llmResponse("4")))

        chatService.chat(
            ChatRequest(
                turnId = "turn-2",
                messages = listOf(
                    ChatMessage(ChatRole.USER, "Hello"),
                    ChatMessage(ChatRole.ASSISTANT, "Hi"),
                    ChatMessage(ChatRole.USER, "What is 2+2?")
                )
            )
        ).block(Duration.ofSeconds(1))

        val requestCaptor = argumentCaptor<LlmRequest>()
        verify(fakeLlmClient).generate(requestCaptor.capture())
        val systemMessages = requestCaptor.firstValue.messages
            .filter { it.role == LlmRole.SYSTEM }
        assertEquals(1, systemMessages.size)
        assertEquals(LlmRole.SYSTEM, requestCaptor.firstValue.messages.first().role)
        assertEquals(
            true,
            systemMessages.single().content.orEmpty().contains("final user message")
        )
        assertEquals(
            "What is 2+2?",
            requestCaptor.firstValue.messages.last().content
        )
    }

    @Test
    fun `should reject a transcript that does not end with a user message`() {
        assertThrows(IllegalArgumentException::class.java) {
            chatService.chat(
                ChatRequest(
                    turnId = "turn-1",
                    messages = listOf(
                        ChatMessage(ChatRole.USER, "Hello"),
                        ChatMessage(ChatRole.ASSISTANT, "Hi")
                    )
                )
            )
        }
    }

    @Test
    fun `should execute tool calls and send results back to the LLM`() {
        val toolCall = ToolCall(
            id = "call-1",
            name = "getStockPrice",
            arguments = mapOf("symbol" to "NVDA")
        )

        whenever(fakeLlmClient.generate(any()))
            .thenReturn(
                Mono.just(
                    llmResponse(
                        content = null,
                        toolCalls = listOf(toolCall)
                    )
                )
            )
            .thenReturn(Mono.just(llmResponse("NVDA is trading at 170.")))

        whenever(toolExecutor.execute(toolCall))
            .thenReturn(170.00)

        val response = chatService.chat(
            chatRequest(
                conversationId = "conversation-123",
                turnId = "turn-1",
                message = "What is the NVDA price?"
            )
        ).block(Duration.ofSeconds(1))

        assertEquals("NVDA is trading at 170.", response!!.message)
        verify(toolExecutor).execute(toolCall)

        val requestCaptor = argumentCaptor<LlmRequest>()
        verify(fakeLlmClient, times(2)).generate(requestCaptor.capture())

        val followUp = requestCaptor.secondValue.messages
        assertEquals(LlmRole.ASSISTANT, followUp[followUp.size - 2].role)
        assertEquals(toolCall, followUp[followUp.size - 2].toolCall)
        assertEquals(LlmRole.TOOL, followUp.last().role)
        assertEquals("getStockPrice", followUp.last().toolResult?.toolName)
        assertEquals(170.00, followUp.last().toolResult?.content)
    }

    private fun chatRequest(
        turnId: String? = "turn-1",
        conversationId: String? = null,
        message: String
    ) = ChatRequest(
        turnId = turnId,
        conversationId = conversationId,
        messages = listOf(ChatMessage(ChatRole.USER, message))
    )

    private fun llmResponse(
        content: String?,
        usage: TokenUsage = TokenUsage(0, 0, 0),
        toolCalls: List<ToolCall> = emptyList()
    ) = LlmResponse(
        content = content,
        usage = usage,
        finishReason = "stop",
        toolCalls = toolCalls
    )
}
