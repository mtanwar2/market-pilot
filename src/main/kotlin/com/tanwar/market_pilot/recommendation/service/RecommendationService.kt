package com.tanwar.market_pilot.recommendation.service

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.client.LlmClientFactory
import com.tanwar.market_pilot.llm.model.LlmMessage
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.model.LlmRole
import com.tanwar.market_pilot.llm.model.ToolDefinition
import com.tanwar.market_pilot.llm.model.ToolResult
import com.tanwar.market_pilot.llm.tool.ToolExecutor
import com.tanwar.market_pilot.portfolio.service.PortfolioAnalysisService
import com.tanwar.market_pilot.recommendation.model.RecommendationResponse
import com.tanwar.market_pilot.recommendation.parser.RecommendationParser
import com.tanwar.market_pilot.recommendation.prompt.RecommendationPromptBuilder
import com.tanwar.market_pilot.recommendation.validation.RecommendationValidator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class RecommendationService(
    private val llmClientFactory: LlmClientFactory,
    private val portfolioAnalysisService: PortfolioAnalysisService,
    private val promptBuilder: RecommendationPromptBuilder,
    private val parser: RecommendationParser,
    private val validator: RecommendationValidator,
    private val toolExecutor: ToolExecutor
) {

    fun recommend(
        portfolioId: UUID
    ): Mono<RecommendationResponse> {

        // 1. Get portfolio analysis.
        //
        // This internally gets:
        // Portfolio -> Holdings -> Market Data -> Calculations
        val analysis =
            portfolioAnalysisService.analyze(portfolioId)

        // 2. Build an LLM prompt using the analysis.
        val prompt =
            promptBuilder.build(analysis)

        val conversationMessages = mutableListOf(
            LlmMessage(
                role = LlmRole.USER,
                content = prompt
            )
        )

        // 3. Call the LLM, executing tools if the model requests them.
        return generateWithTools(
            client = llmClientFactory.getClient(),
            conversationMessages = conversationMessages
        )
            // 4. Convert LLM JSON into RecommendationResponse.
            .map { response ->
                parser.parse(response.content)
            }
            // 5. Validate the structured recommendation.
            .map { recommendation ->
                validator.validate(recommendation)
                recommendation
            }
    }

    private fun generateWithTools(
        client: LlmClient,
        conversationMessages: MutableList<LlmMessage>,
        round: Int = 0
    ): Mono<LlmResponse> {

        if (round >= MAX_TOOL_ROUNDS) {
            return Mono.error(
                IllegalStateException(
                    "Exceeded maximum tool-call rounds ($MAX_TOOL_ROUNDS)"
                )
            )
        }

        val llmRequest = LlmRequest(
            messages = conversationMessages.toList(),
            temperature = 0.2,
            tools = toolDefinitions
        )

        return client.generate(llmRequest)
            .flatMap { response ->

                if (response.toolCalls.isEmpty()) {
                    return@flatMap Mono.just(response)
                }

                log.info(
                    "LLM requested {} tool(s): {}",
                    response.toolCalls.size,
                    response.toolCalls.map { it.name }
                )

                response.toolCalls.forEach { toolCall ->

                    conversationMessages.add(
                        LlmMessage(
                            role = LlmRole.ASSISTANT,
                            toolCall = toolCall
                        )
                    )

                    val result = toolExecutor.execute(toolCall)

                    log.info(
                        "Tool executed name={} result={}",
                        toolCall.name,
                        result
                    )

                    conversationMessages.add(
                        LlmMessage(
                            role = LlmRole.TOOL,
                            toolResult = ToolResult(
                                toolCallId = toolCall.id,
                                toolName = toolCall.name,
                                content = result
                            )
                        )
                    )
                }

                generateWithTools(
                    client = client,
                    conversationMessages = conversationMessages,
                    round = round + 1
                )
            }
    }

    companion object {

        private const val MAX_TOOL_ROUNDS = 5

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
            )
        )

        private val log =
            LoggerFactory.getLogger(RecommendationService::class.java)
    }
}
