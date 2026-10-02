package com.tanwar.market_pilot.rag.chunking

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TextChunkerTest {

    private val textChunker = TextChunker()

    @Test
    fun `should split document into chunks`() {

        val text = """
            NVIDIA reported strong revenue growth driven by AI demand.
            
            Its data center business grew significantly due to demand for AI infrastructure.
            
            NVIDIA expects continued growth as AI adoption increases.
        """.trimIndent()

        val chunks = textChunker.chunk(text)

        assertEquals(3, chunks.size)

        assertEquals(
            "NVIDIA reported strong revenue growth driven by AI demand.",
            chunks[0]
        )

        assertEquals(
            "Its data center business grew significantly due to demand for AI infrastructure.",
            chunks[1]
        )

        assertEquals(
            "NVIDIA expects continued growth as AI adoption increases.",
            chunks[2]
        )
    }

    @Test
    fun `should ignore empty paragraphs`() {

        val text = """
            First paragraph.
            
            
            Second paragraph.
            
        """.trimIndent()

        val chunks = textChunker.chunk(text)

        assertEquals(2, chunks.size)
        assertTrue(chunks.none { it.isBlank() })
    }
}