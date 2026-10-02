package com.tanwar.market_pilot.rag.service

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@SpringBootTest
class FinancialRagTest {

    @Autowired
    lateinit var documentIngestionService: DocumentIngestionService

    @Autowired
    lateinit var ragService: RagService

    @Test
    fun `should answer question using financial document`() {

        val documentId = UUID.randomUUID()

        documentIngestionService.ingestFromResource(
            documentId = documentId,
            path = "/documents/nvidia-report.txt"
        )

        val answer = ragService.generateAnswer(
            question = "What is NVIDIA's CEO's favorite food?"
        )

        println("Answer:")
        println(answer)

        assertTrue(answer.isNotBlank())
    }
}