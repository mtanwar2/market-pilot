package com.tanwar.market_pilot.rag.reranking

import com.tanwar.market_pilot.rag.entity.DocumentChunkEntity
import com.tanwar.market_pilot.rag.reranking.impl.SimpleReranker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class SimpleRerankerTest {

    private val reranker = SimpleReranker()

    @Test
    fun `should rank more relevant chunks first`() {

        val chunks = listOf(
            createChunk("Apple launched a new iPhone."),
            createChunk("NVIDIA reported strong revenue growth driven by AI demand."),
            createChunk("Tesla reported vehicle delivery numbers.")
        )

        val results = reranker.rerank(
            query = "What is driving NVIDIA growth?",
            chunks = chunks,
            limit = 2
        )

        assertEquals(2, results.size)

        assertEquals(
            "NVIDIA reported strong revenue growth driven by AI demand.",
            results[0].chunkText
        )
    }

    private fun createChunk(text: String): DocumentChunkEntity {
        return DocumentChunkEntity(
            id = UUID.randomUUID(),
            documentId = UUID.randomUUID(),
            chunkText = text,
            embedding = FloatArray(768),
            createdAt = LocalDateTime.now()
        )
    }
}