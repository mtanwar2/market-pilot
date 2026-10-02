package com.tanwar.market_pilot.rag.evaluation

import com.tanwar.market_pilot.llm.client.LlmClientFactory
import com.tanwar.market_pilot.llm.model.LlmMessage
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmRole
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import tools.jackson.databind.ObjectMapper

@Component
class AnswerRelevanceEvaluator(
    private val llmClientFactory: LlmClientFactory,
    private val objectMapper: ObjectMapper
) {

    fun evaluate(
        question: String,
        generatedAnswer: String,
        expectedAnswer: String? = null
    ): Mono<AnswerRelevanceResult> {

        val prompt = buildPrompt(
            question = question,
            generatedAnswer = generatedAnswer,
            expectedAnswer = expectedAnswer
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
                        "LLM returned empty content while evaluating answer relevance"
                    )

                objectMapper.readValue(
                    content,
                    AnswerRelevanceResult::class.java
                )
            }
    }

    private fun buildPrompt(
        question: String,
        generatedAnswer: String,
        expectedAnswer: String?
    ): String {

        return """
            You are evaluating the relevance of an answer to a question.

            Determine whether the generated answer directly and meaningfully
            answers the question.

            Question:
            $question

            Generated answer:
            $generatedAnswer

            Reference answer:
            ${expectedAnswer ?: "Not provided"}

            Return ONLY valid JSON in exactly this format:

            {
              "score": 0.0,
              "reason": "brief explanation"
            }

            Scoring:
            0.0 = completely irrelevant
            0.5 = partially relevant
            1.0 = highly relevant

            Do not include markdown.
            Do not include ```json.
            Do not include any text outside the JSON.
        """.trimIndent()
    }
}