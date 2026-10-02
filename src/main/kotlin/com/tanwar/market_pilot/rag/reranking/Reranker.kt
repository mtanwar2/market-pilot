package com.tanwar.market_pilot.rag.reranking

import com.tanwar.market_pilot.rag.entity.DocumentChunkEntity

interface Reranker {

    fun rerank(
        query: String,
        chunks: List<DocumentChunkEntity>,
        limit: Int
    ): List<DocumentChunkEntity>
}