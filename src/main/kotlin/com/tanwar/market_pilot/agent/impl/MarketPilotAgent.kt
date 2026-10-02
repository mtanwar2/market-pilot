package com.tanwar.market_pilot.agent.impl

import com.tanwar.market_pilot.agent.Agent
import com.tanwar.market_pilot.agent.AgentState
import com.tanwar.market_pilot.config.ChatLogContext
import com.tanwar.market_pilot.llm.audit.AuditContext
import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.client.LlmClientFactory
import com.tanwar.market_pilot.llm.model.LlmMessage
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.model.LlmRole
import com.tanwar.market_pilot.llm.model.ToolDefinition
import com.tanwar.market_pilot.llm.model.ToolResult
import com.tanwar.market_pilot.llm.tool.ToolExecutor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class MarketPilotAgent(
    private val llmClientFactory: LlmClientFactory,
    private val toolExecutor: ToolExecutor,
) : Agent {

    override fun run(messages: List<LlmMessage>, auditContext: AuditContext): Mono<LlmResponse> {

        val state = createInitialState(messages)
        val llmClient = llmClientFactory.getClient()

        return runAgentLoop(
            state = state,
            llmClient = llmClient,
            auditContext = auditContext

        )
    }

    private fun runAgentLoop(
        state: AgentState,
        llmClient: LlmClient,
        auditContext: AuditContext
    ): Mono<LlmResponse> {

        if (state.round >= MAX_TOOL_ROUNDS) {
            return Mono.error(
                IllegalStateException(
                    "Exceeded maximum agent rounds: $MAX_TOOL_ROUNDS"
                )
            )
        }

        val request = LlmRequest(
            messages = state.messages.toList(),
            tools = toolDefinitions
        )

        return Mono.deferContextual { context ->

            log.info(
                "REACTOR_CONTEXT_TEST requestId={} conversationId={} turnId={}",
                context.getOrDefault(ChatLogContext.REQUEST_ID, "MISSING"),
                context.getOrDefault(ChatLogContext.CONVERSATION_ID, "MISSING"),
                context.getOrDefault(ChatLogContext.TURN_ID, "MISSING")
            )

            llmClient.generate(request)
        }
            .flatMap { response ->

                // No tool calls means the agent has completed its reasoning.
                // The response should contain the final JSON.
                if (response.toolCalls.isEmpty()) {

                    log.info(
                        "Agent round={} completed: no further tool calls required",
                        state.round
                    )

                    return@flatMap Mono.just(response)
                }

                log.info(
                    "Agent round={} requested tools={}",
                    state.round,
                    response.toolCalls.map { it.name }
                )

                handleToolCalls(
                    state = state,
                    response = response,
                    auditContext = auditContext
                )

                runAgentLoop(
                    state = state.copy(
                        round = state.round + 1
                    ),
                    llmClient = llmClient,
                    auditContext = auditContext
                )
            }
    }

    private fun handleToolCalls(
        state: AgentState,
        response: LlmResponse,
        auditContext: AuditContext
    ) {
        response.toolCalls.forEach { toolCall ->

            // Add the assistant's tool call to the conversation.
            state.messages.add(
                LlmMessage(
                    role = LlmRole.ASSISTANT,
                    toolCall = toolCall
                )
            )

            log.info(
                "Executing tool name={} id={}",
                toolCall.name,
                toolCall.id
            )

            // Execute the tool.
            val executionResult =
                toolExecutor.execute(toolCall,auditContext)

            // Convert the execution result into a message
            // that can be sent back to the LLM.
            state.messages.add(
                LlmMessage(
                    role = LlmRole.TOOL,
                    toolResult = ToolResult(
                        toolCallId = toolCall.id,
                        toolName = toolCall.name,
                        content = executionResult
                    )
                )
            )
        }


    }


    private fun createInitialState(
        messages: List<LlmMessage>
    ): AgentState {

        val agentMessages = mutableListOf(
            LlmMessage(
                role = LlmRole.SYSTEM,
                content = AGENT_SYSTEM_PROMPT
            )
        )

        agentMessages.addAll(messages)

        return AgentState(
            messages = agentMessages
        )
    }

    companion object {

        private const val MAX_TOOL_ROUNDS = 5

        private val AGENT_SYSTEM_PROMPT = """You are Market Pilot, a financial analysis assistant.

                You can use available tools to retrieve information needed
                to answer the user's request.
                
                Use a tool when current or external information is required.
                
                Tool arguments must come only from information explicitly
                available in the user's message or conversation history.
                
                Never invent, guess, generate, or fabricate identifiers such as:
                - portfolioId
                - userId
                - accountId
                - conversationId
                
                After receiving a tool result, use that result when answering
                the user's request.
                
                Do not call the same tool again if the existing tool result
                already contains the information required.
                
                Only call another tool if information required to answer
                the user's request is still missing.
                
                Once you have enough information to answer the user,
                stop calling tools and provide the final answer.
                
                For now, answer the user naturally. 
        """.trimIndent()

        private val toolDefinitions = listOf(

            ToolDefinition(
                name = "getStockPrice",
                description = "Get the current stock price for a stock symbol",
                parameters = mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "symbol" to mapOf(
                            "type" to "string",
                            "description" to "Stock ticker symbol, for example NVDA"
                        )
                    ),
                    "required" to listOf("symbol")
                )
            ),

        ToolDefinition(
        name = "getPortfolio",
        description = """
        Get a user's portfolio by portfolio ID.

        The portfolioId must come from the user's message or from
        information explicitly provided in the conversation.

        Never invent, guess, or fabricate a portfolioId.
    """.trimIndent(),
        parameters = mapOf(
        "type" to "object",
        "properties" to mapOf(
        "portfolioId" to mapOf(
        "type" to "string",
        "description" to "The portfolio UUID explicitly provided by the user"
        )
        ),
        "required" to listOf("portfolioId")
        )
        )

        )

        private val log =
            LoggerFactory.getLogger(MarketPilotAgent::class.java)
    }
}