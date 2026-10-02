package com.tanwar.market_pilot.rag.service

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@SpringBootTest
class FinancialDocumentIngestionTest {

    @Autowired
    lateinit var documentIngestionService: DocumentIngestionService

    @Test
    fun `should ingest financial document`() {

        val documentId = UUID.randomUUID()

        val chunks = documentIngestionService.ingestFromResource(
            documentId = documentId,
            path = "/documents/nvidia-report.txt"
        )

        assertTrue(chunks.isNotEmpty())

        println("Created ${chunks.size} chunks")

        chunks.forEach {
            println("Chunk: ${it.chunkText}")
            println("Embedding size: ${it.embedding.size}")
        }
    }
}