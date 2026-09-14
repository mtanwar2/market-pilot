package com.tanwar.market_pilot.recommendation.service

import com.tanwar.market_pilot.llm.client.LlmClientFactory
import com.tanwar.market_pilot.llm.model.LlmMessage
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmRole
import com.tanwar.market_pilot.recommendation.model.RecommendationResponse
import com.tanwar.market_pilot.recommendation.parser.RecommendationParser
import com.tanwar.market_pilot.recommendation.prompt.RecommendationPromptBuilder
import com.tanwar.market_pilot.recommendation.validation.RecommendationValidator
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class RecommendationService(
    private val llmClientFactory: LlmClientFactory,
    private val promptBuilder: RecommendationPromptBuilder,
    private val parser: RecommendationParser,
    private val validator: RecommendationValidator
) {

    fun recommend(stock: String): Mono<RecommendationResponse> {

        val prompt = promptBuilder.build(stock)

        val request = LlmRequest(
            messages = listOf(
                LlmMessage(
                    role = LlmRole.USER,
                    content = prompt
                )
            ),
            temperature = 0.2
        )

        return llmClientFactory
            .getClient()
            .generate(request)
            .map { response ->
                parser.parse(response.content)
            }
            .map { recommendation ->
                validator.validate(recommendation)
                recommendation
            }
    }

    fun process(content: String): RecommendationResponse {
        val response = parser.parse(content)
        validator.validate(response)
        return response
    }
}