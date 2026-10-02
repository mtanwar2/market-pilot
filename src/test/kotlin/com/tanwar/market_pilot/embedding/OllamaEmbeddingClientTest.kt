package com.tanwar.market_pilot.embedding

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class OllamaEmbeddingClientTest {

    @Autowired
    lateinit var embeddingClient: EmbeddingClient

    @Test
    fun `should generate embedding for text`() {

        val embedding = embeddingClient.embed(
            "NVDA is currently trading at 170 dollars"
        )

        assertFalse(embedding.isEmpty())
        assertTrue(embedding.size > 1)

        println("Embedding dimensions: ${embedding.size}")
        println("First 5 values: ${embedding.take(5)}")
    }
}