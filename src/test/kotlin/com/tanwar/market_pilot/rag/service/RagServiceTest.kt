package com.tanwar.market_pilot.rag.service

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class RagServiceTest {

    @Autowired
    lateinit var ragService: RagService

    @Test
    fun `should retrieve relevant context for question`() {

        val results = ragService.retrieveContext(
            question = "What is driving NVIDIA's growth?",
            limit = 3
        )

        assertFalse(results.isEmpty())

        results.forEach {
            println("Retrieved: ${it.chunkText}")
        }

        assertTrue(
            results.any {
                it.chunkText.contains(
                    "NVIDIA",
                    ignoreCase = true
                )
            }
        )
    }

    @Test
    fun `should build context from retrieved chunks`() {

        val context = ragService.buildContext(
            question = "What is driving NVIDIA's growth?",
            limit = 3
        )

        assertFalse(context.isBlank())

        println("Retrieved context:")
        println(context)

        assertTrue(
            context.contains(
                "NVIDIA",
                ignoreCase = true
            )
        )
    }

    @Test
    fun `should generate answer using retrieved context`() {

        val answer = ragService.generateAnswer(
            question = "What is driving NVIDIA's growth?",
            limit = 3
        )

        println("RAG Answer:")
        println(answer)

        assertFalse(answer.isBlank())
    }
}