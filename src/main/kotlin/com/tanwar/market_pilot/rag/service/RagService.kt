package com.tanwar.market_pilot.rag.service

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.client.LlmClientFactory
import com.tanwar.market_pilot.llm.model.LlmMessage
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmRole
import com.tanwar.market_pilot.llm.model.TokenUsage
import com.tanwar.market_pilot.rag.entity.DocumentChunkEntity
import com.tanwar.market_pilot.rag.reranking.Reranker
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RagService(
    private val documentChunkService: DocumentChunkService,
    private val llmClientFactory: LlmClientFactory,
    private val reranker: Reranker
) {

    fun retrieveContext(
        question: String,
        limit: Int = 5
    ): List<DocumentChunkEntity> {

        return documentChunkService.search(
            query = question,
            limit = limit
        )
    }

    fun buildContext(
        question: String,
        limit: Int = 5
    ): String {

        val chunks = retrieveContext(
            question = question,
            limit = 10
        )

        val relevantChunks = reranker.rerank(
            query = question,
            chunks = chunks,
            limit = limit
        )
        return relevantChunks.joinToString("\n\n") {
            it.chunkText
        }
    }

    fun generateAnswer(
        question: String,
        limit: Int = 5
    ): String {


        val context = buildContext(
            question = question,
            limit = limit
        )

        val prompt = """
        Answer the question using only the provided context.

        If the answer cannot be found in the context, say:
        "I don't have enough information to answer this."

        Context:
        $context

        Question:
        $question
    """.trimIndent()

        val request = LlmRequest(
            messages = listOf(
                LlmMessage(
                    role = LlmRole.USER,
                    content = prompt
                )
            )
        )

        val response = llmClientFactory.getClient()
            .generate(request)
            .block()
            ?: throw IllegalStateException("LLM returned empty response")

        return response.content
            ?: throw IllegalStateException("LLM returned no content")
    }

    fun generateAnswerWithUsage(
        question: String,
        limit: Int = 5
    ): Pair<String, TokenUsage> {

        val context = buildContext(
            question = question,
            limit = limit
        )

        val prompt = """
        Answer the question using only the provided context.

        If the answer cannot be found in the context, say:
        "I don't have enough information to answer this."

        Context:
        $context

        Question:
        $question
    """.trimIndent()

        val request = LlmRequest(
            messages = listOf(
                LlmMessage(
                    role = LlmRole.USER,
                    content = prompt
                )
            )
        )

        val response = llmClientFactory.getClient()
            .generate(request)
            .block()
            ?: throw IllegalStateException("LLM returned empty response")

        val answer = response.content
            ?: throw IllegalStateException("LLM returned no content")

        return Pair(
            answer,
            response.usage
        )
    }
}