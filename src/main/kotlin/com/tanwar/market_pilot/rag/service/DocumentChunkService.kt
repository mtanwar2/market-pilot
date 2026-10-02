package com.tanwar.market_pilot.rag.service

import com.tanwar.market_pilot.embedding.EmbeddingClient
import com.tanwar.market_pilot.rag.entity.DocumentChunkEntity
import com.tanwar.market_pilot.rag.repository.DocumentChunkRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class DocumentChunkService(
    private val embeddingClient: EmbeddingClient,
    private val documentChunkRepository: DocumentChunkRepository
) {

    fun saveChunk(
        documentId: UUID,
        text: String
    ): DocumentChunkEntity {

        val embedding = embeddingClient
            .embed(text)
            .toFloatArray()

        val chunk = DocumentChunkEntity(
            id = UUID.randomUUID(),
            documentId = documentId,
            chunkText = text,
            embedding = embedding,
            createdAt = LocalDateTime.now()
        )

        return documentChunkRepository.save(chunk)
    }

    fun search(
        query: String,
        limit: Int = 5
    ): List<DocumentChunkEntity> {

        val embedding = embeddingClient.embed(query)

        val queryVector = embedding.joinToString(
            prefix = "[",
            postfix = "]",
            separator = ","
        )

        return documentChunkRepository.findSimilar(
            queryVector = queryVector,
            limit = limit
        )
    }
}