package com.tanwar.market_pilot.rag.service

import com.tanwar.market_pilot.embedding.EmbeddingClient
import com.tanwar.market_pilot.rag.chunking.TextChunker
import com.tanwar.market_pilot.rag.entity.DocumentChunkEntity
import com.tanwar.market_pilot.rag.repository.DocumentChunkRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class DocumentIngestionService(
    private val textChunker: TextChunker,
    private val embeddingClient: EmbeddingClient,
    private val documentChunkRepository: DocumentChunkRepository,
    private val documentLoader: DocumentLoader
) {

    fun ingest(
        documentId: UUID,
        text: String
    ): List<DocumentChunkEntity> {

        val chunks = textChunker.chunk(text)

        return chunks.map { chunkText ->

            val embedding = embeddingClient
                .embed(chunkText)
                .toFloatArray()

            val chunk = DocumentChunkEntity(
                id = UUID.randomUUID(),
                documentId = documentId,
                chunkText = chunkText,
                embedding = embedding,
                createdAt = LocalDateTime.now()
            )

            documentChunkRepository.save(chunk)
        }
    }

    fun ingestFromResource(
        documentId: UUID,
        path: String
    ): List<DocumentChunkEntity> {

        val text = documentLoader.load(path)

        return ingest(
            documentId = documentId,
            text = text
        )
    }
}