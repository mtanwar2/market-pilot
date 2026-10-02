package com.tanwar.market_pilot.llm.client.impl

import com.tanwar.market_pilot.llm.model.LlmMessage
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmRole
import com.tanwar.market_pilot.llm.model.OllamaChatResponse
import com.tanwar.market_pilot.llm.model.OllamaFunction
import com.tanwar.market_pilot.llm.model.OllamaMessage
import com.tanwar.market_pilot.llm.model.OllamaToolCall
import com.tanwar.market_pilot.llm.model.ResponseFormat
import com.tanwar.market_pilot.llm.model.ToolCall
import com.tanwar.market_pilot.llm.model.ToolDefinition
import com.tanwar.market_pilot.llm.model.ToolResult
import com.tanwar.market_pilot.llm.tool.ToolExecutionResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper

class OllamaLlmClientToolMappingTest {

    private val objectMapper = ObjectMapper()

    @Test
    fun `should map tools tool calls and tool results to Ollama`() {
        val toolCall = ToolCall(
            id = "call-1",
            name = "getStockPrice",
            arguments = mapOf("symbol" to "NVDA")
        )

        val request = LlmRequest(
            messages = listOf(
                LlmMessage(
                    role = LlmRole.SYSTEM,
                    content = "You are Market Pilot."
                ),
                LlmMessage(
                    role = LlmRole.USER,
                    content = "What is the NVDA price?"
                ),
                LlmMessage(
                    role = LlmRole.ASSISTANT,
                    toolCall = toolCall
                ),
                LlmMessage(
                    role = LlmRole.TOOL,
                    toolResult = ToolResult(
                        toolCallId = "call-1",
                        toolName = "getStockPrice",
                        content = ToolExecutionResult(
                            success = true,
                            content = 170.00
                        )
                    )
                )
            ),
            temperature = 0.2,
            maxTokens = 512,
            tools = listOf(
                ToolDefinition(
                    name = "getStockPrice",
                    description = "Get the current stock price",
                    parameters = mapOf(
                        "type" to "object",
                        "properties" to mapOf(
                            "symbol" to mapOf("type" to "string")
                        ),
                        "required" to listOf("symbol")
                    )
                )
            ),
            responseFormat = ResponseFormat(ResponseFormat.Type.JSON)
        )

        val result = OllamaLlmClient.toOllamaRequest(
            request = request,
            model = "qwen3:4b",
            objectMapper = objectMapper
        )

        assertEquals("qwen3:4b", result.model)
        assertEquals("system", result.messages[0].role)
        assertEquals("user", result.messages[1].role)
        assertEquals("assistant", result.messages[2].role)
        assertEquals(
            "getStockPrice",
            result.messages[2].toolCalls
                ?.single()
                ?.function
                ?.name
        )
        assertEquals(
            mapOf("symbol" to "NVDA"),
            result.messages[2].toolCalls
                ?.single()
                ?.function
                ?.arguments
        )
        assertEquals("tool", result.messages[3].role)
        assertEquals("getStockPrice", result.messages[3].toolName)
        assertTrue(
            result.messages[3].content.orEmpty()
                .contains("\"success\":true")
        )
        assertEquals(
            "getStockPrice",
            result.tools?.single()?.function?.name
        )
        assertEquals("json", result.format)
        assertEquals(0.2, result.options?.temperature)
        assertEquals(512, result.options?.numPredict)

        val json = objectMapper.writeValueAsString(result)
        assertTrue(json.contains("\"tool_calls\""))
        assertTrue(json.contains("\"tool_name\""))
        assertTrue(json.contains("\"num_predict\""))
    }

    @Test
    fun `should map Ollama tool calls to common response`() {
        val response = OllamaChatResponse(
            model = "qwen3:4b",
            message = OllamaMessage(
                role = "assistant",
                content = "",
                toolCalls = listOf(
                    OllamaToolCall(
                        id = "call-1",
                        function = OllamaFunction(
                            name = "getPortfolio",
                            arguments = mapOf(
                                "portfolioId" to "portfolio-123"
                            )
                        )
                    )
                )
            ),
            done = true,
            done_reason = "stop",
            prompt_eval_count = 20,
            eval_count = 5
        )

        val result = OllamaLlmClient.toLlmResponse(response)

        assertNull(result.content)
        assertEquals("stop", result.finishReason)
        assertEquals(20, result.usage.inputTokens)
        assertEquals(5, result.usage.outputTokens)
        assertEquals(25, result.usage.totalTokens)
        assertEquals(
            ToolCall(
                id = "call-1",
                name = "getPortfolio",
                arguments = mapOf(
                    "portfolioId" to "portfolio-123"
                )
            ),
            result.toolCalls.single()
        )
    }

    @Test
    fun `should omit optional Ollama settings when not requested`() {
        val result = OllamaLlmClient.toOllamaRequest(
            request = LlmRequest(
                messages = listOf(
                    LlmMessage(
                        role = LlmRole.USER,
                        content = "Hello"
                    )
                )
            ),
            model = "qwen3:4b",
            objectMapper = objectMapper
        )

        assertNull(result.tools)
        assertNull(result.format)
        assertNull(result.options)
    }
}
