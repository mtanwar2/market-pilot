package com.tanwar.market_pilot.rag.reranking.impl

import com.tanwar.market_pilot.rag.entity.DocumentChunkEntity
import com.tanwar.market_pilot.rag.reranking.Reranker
import org.springframework.stereotype.Component

@Component
class SimpleReranker : Reranker {

    override fun rerank(
        query: String,
        chunks: List<DocumentChunkEntity>,
        limit: Int
    ): List<DocumentChunkEntity> {

        val queryWords = query
            .lowercase()
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .toSet()

        return chunks
            .map { chunk ->
                val chunkWords = chunk.chunkText
                    .lowercase()
                    .split("\\s+".toRegex())
                    .filter { it.isNotBlank() }
                    .toSet()

                val score = queryWords.intersect(chunkWords).size

                chunk to score
            }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }
}