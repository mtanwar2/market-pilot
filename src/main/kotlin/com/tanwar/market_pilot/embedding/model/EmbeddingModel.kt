package com.tanwar.market_pilot.embedding.model

data class OllamaEmbeddingRequest(
    val model: String,
    val input: String
)

data class OllamaEmbeddingResponse(
    val embeddings: List<List<Float>>
)