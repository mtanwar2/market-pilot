package com.tanwar.market_pilot.rag.evaluation

import com.tanwar.market_pilot.llm.client.LlmClientFactory
import com.tanwar.market_pilot.llm.model.LlmMessage
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmRole
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import tools.jackson.databind.ObjectMapper

@Component
class HallucinationEvaluator(
    private val llmClientFactory: LlmClientFactory,
    private val objectMapper: ObjectMapper
) {

    fun evaluate(
        retrievedContext: List<String>,
        generatedAnswer: String
    ): Mono<HallucinationResult> {

        val prompt = buildPrompt(
            retrievedContext = retrievedContext,
            generatedAnswer = generatedAnswer
        )

        val request = LlmRequest(
            messages = listOf(
                LlmMessage(
                    role = LlmRole.USER,
                    content = prompt
                )
            ),
            temperature = 0.0
        )

        return llmClientFactory
            .getClient()
            .generate(request)
            .map { response ->

                val content = response.content
                    ?: throw IllegalStateException(
                        "LLM returned empty content while evaluating hallucination"
                    )

                objectMapper.readValue(
                    content,
                    HallucinationResult::class.java
                )
            }
    }

    private fun buildPrompt(
        retrievedContext: List<String>,
        generatedAnswer: String
    ): String {

        val context = retrievedContext.joinToString("\n\n")

        return """
            You are evaluating whether a generated answer contains hallucinated information.

            Retrieved context:
            $context

            Generated answer:
            $generatedAnswer

            Determine whether the generated answer introduces information
            that is not supported by the retrieved context.

            Return ONLY valid JSON in exactly this format:

            {
              "score": 1.0,
              "reason": "brief explanation"
            }

            Scoring:
            1.0 = no unsupported information detected
            0.5 = some unsupported information is present
            0.0 = significant unsupported information is present

            Judge only using the retrieved context.
            Do not use outside knowledge.

            Do not include markdown.
            Do not include ```json.
            Do not include any text outside the JSON.
        """.trimIndent()
    }
}