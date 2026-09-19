package com.tanwar.market_pilot.recommendation.service

import com.tanwar.market_pilot.llm.client.LlmClientFactory
import com.tanwar.market_pilot.llm.model.LlmMessage
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmRole
import com.tanwar.market_pilot.portfolio.service.PortfolioAnalysisService
import com.tanwar.market_pilot.recommendation.model.RecommendationResponse
import com.tanwar.market_pilot.recommendation.parser.RecommendationParser
import com.tanwar.market_pilot.recommendation.prompt.RecommendationPromptBuilder
import com.tanwar.market_pilot.recommendation.validation.RecommendationValidator
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class RecommendationService(
    private val llmClientFactory: LlmClientFactory,
    private val portfolioAnalysisService: PortfolioAnalysisService,
    private val promptBuilder: RecommendationPromptBuilder,
    private val parser: RecommendationParser,
    private val validator: RecommendationValidator
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

        // 3. Create the generic LLM request.
        val request = LlmRequest(
            messages = listOf(
                LlmMessage(
                    role = LlmRole.USER,
                    content = prompt
                )
            ),
            temperature = 0.2
        )

        // 4. Call the LLM.
        return llmClientFactory
            .getClient()
            .generate(request)

            // 5. Convert LLM JSON into RecommendationResponse.
            .map { response ->
                parser.parse(response.content)
            }

            // 6. Validate the structured recommendation.
            .map { recommendation ->
                validator.validate(recommendation)
                recommendation
            }
    }
}