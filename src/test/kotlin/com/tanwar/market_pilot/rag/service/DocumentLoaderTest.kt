package com.tanwar.market_pilot.rag.service

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DocumentLoaderTest {

    @Autowired
    lateinit var documentLoader: DocumentLoader

    @Test
    fun `should load financial document`() {

        val text = documentLoader.load(
            "/documents/nvidia-report.txt"
        )

        assertTrue(text.isNotBlank())

        println(text)
    }
}