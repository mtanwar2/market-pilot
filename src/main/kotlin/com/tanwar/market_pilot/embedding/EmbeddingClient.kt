package com.tanwar.market_pilot.embedding

interface EmbeddingClient {

    fun embed(text: String): List<Float>
}