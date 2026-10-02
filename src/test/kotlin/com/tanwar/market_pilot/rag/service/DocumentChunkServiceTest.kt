package com.tanwar.market_pilot.rag.service

import com.tanwar.market_pilot.rag.repository.DocumentChunkRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@SpringBootTest
class DocumentChunkServiceTest {

    @Autowired
    lateinit var documentChunkService: DocumentChunkService

    @Autowired
    lateinit var documentChunkRepository: DocumentChunkRepository

    @Test
    fun `should generate embedding and save document chunk`() {

        val documentId = UUID.randomUUID()

        val text =
            "NVIDIA reported strong revenue growth driven by AI demand."

        val savedChunk = documentChunkService.saveChunk(
            documentId = documentId,
            text = text
        )

        assertNotNull(savedChunk.id)
        assertEquals(documentId, savedChunk.documentId)
        assertEquals(text, savedChunk.chunkText)
        assertEquals(768, savedChunk.embedding.size)

        val storedChunk =
            documentChunkRepository.findById(savedChunk.id)

        assertEquals(true, storedChunk.isPresent)
        assertEquals(text, storedChunk.get().chunkText)

        println("Stored chunk ID: ${savedChunk.id}")
        println("Embedding dimensions: ${savedChunk.embedding.size}")
    }

    @Test
    fun `should find semantically similar document chunks`() {

        val results = documentChunkService.search(
            query = "What is driving NVIDIA's growth?",
            limit = 5
        )

        assertFalse(results.isEmpty())

        println("Search results:")
        results.forEach {
            println(it.chunkText)
        }

        assertTrue(
            results.any {
                it.chunkText.contains("NVIDIA", ignoreCase = true)
            }
        )
    }

    @Test
    fun `should retrieve semantically relevant chunks`() {

        val documentId = UUID.randomUUID()

        documentChunkService.saveChunk(
            documentId,
            "NVIDIA reported strong revenue growth driven by AI demand."
        )

        documentChunkService.saveChunk(
            documentId,
            "NVIDIA's data center business grew significantly due to demand for AI infrastructure."
        )

        documentChunkService.saveChunk(
            documentId,
            "Apple launched a new iPhone with improved battery life and camera capabilities."
        )

        documentChunkService.saveChunk(
            documentId,
            "Microsoft reported continued growth in its Azure cloud business."
        )

        documentChunkService.saveChunk(
            documentId,
            "Tesla reported changes in vehicle deliveries during the quarter."
        )

        val results = documentChunkService.search(
            query = "What is driving NVIDIA's growth?",
            limit = 3
        )

        assertFalse(results.isEmpty())

        println("\nSearch results:")
        results.forEachIndexed { index, result ->
            println("${index + 1}. ${result.chunkText}")
        }

        assertTrue(
            results.take(3).any {
                it.chunkText.contains("NVIDIA", ignoreCase = true)
            }
        )
    }
}