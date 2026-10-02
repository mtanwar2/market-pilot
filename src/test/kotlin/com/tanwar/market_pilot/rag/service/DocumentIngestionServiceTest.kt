package com.tanwar.market_pilot.rag.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@SpringBootTest
class DocumentIngestionServiceTest {

    @Autowired
    lateinit var documentIngestionService: DocumentIngestionService

    @Test
    fun `should ingest document into embedded chunks`() {

        val documentId = UUID.randomUUID()

        val text = """
            NVIDIA reported strong revenue growth driven by AI demand.

            Its data center business grew significantly due to demand for AI infrastructure.

            NVIDIA expects continued growth as AI adoption increases.
        """.trimIndent()

        val chunks = documentIngestionService.ingest(
            documentId = documentId,
            text = text
        )

        assertEquals(3, chunks.size)

        chunks.forEach { chunk ->
            assertNotNull(chunk.id)
            assertEquals(documentId, chunk.documentId)
            assertFalse(chunk.chunkText.isBlank())
            assertEquals(768, chunk.embedding.size)
        }

        println("Created ${chunks.size} chunks")

        chunks.forEach {
            println("Chunk: ${it.chunkText}")
            println("Embedding dimensions: ${it.embedding.size}")
        }
    }
}