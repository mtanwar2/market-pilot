package com.tanwar.market_pilot.rag.evaluation

import com.tanwar.market_pilot.llm.client.LlmClientFactory
import com.tanwar.market_pilot.llm.model.LlmMessage
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmRole
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import tools.jackson.databind.ObjectMapper

@Component
class FaithfulnessEvaluator(
    private val llmClientFactory: LlmClientFactory,
    private val objectMapper: ObjectMapper
) {

    fun evaluate(
        retrievedContext: List<String>,
        generatedAnswer: String
    ): Mono<FaithfulnessResult> {

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
                        "LLM returned empty content while evaluating faithfulness"
                    )

                objectMapper.readValue(
                    content,
                    FaithfulnessResult::class.java
                )
            }
    }

    private fun buildPrompt(
        retrievedContext: List<String>,
        generatedAnswer: String
    ): String {

        val context = retrievedContext.joinToString("\n\n")

        return """
            You are evaluating the faithfulness of a generated answer.

            Determine whether the claims made in the generated answer
            are supported by the retrieved context.

            Retrieved context:
            $context

            Generated answer:
            $generatedAnswer

            Return ONLY valid JSON in exactly this format:

            {
              "score": 0.0,
              "reason": "brief explanation"
            }

            Scoring:
            0.0 = the answer contains unsupported claims
            0.5 = some claims are supported but some are unsupported
            1.0 = all important claims are supported by the context

            Judge only whether the answer is supported by the context.
            Do not use outside knowledge.

            Do not include markdown.
            Do not include ```json.
            Do not include any text outside the JSON.
        """.trimIndent()
    }
}