package com.tanwar.market_pilot.embedding.impl

import com.tanwar.market_pilot.embedding.EmbeddingClient
import com.tanwar.market_pilot.embedding.model.OllamaEmbeddingRequest
import com.tanwar.market_pilot.embedding.model.OllamaEmbeddingResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class OllamaEmbeddingClient(
    webClientBuilder: WebClient.Builder,
    @Value("\${llm.embedding.base-url:http://localhost:11434}")
    baseUrl: String,
    @Value("\${llm.embedding.model:nomic-embed-text}")
    private val model: String
) : EmbeddingClient {

    private val webClient = webClientBuilder
        .baseUrl(baseUrl)
        .build()

    override fun embed(text: String): List<Float> {

        val request = OllamaEmbeddingRequest(
            model = model,
            input = text
        )

        val response = webClient
            .post()
            .uri("/api/embed")
            .bodyValue(request)
            .retrieve()
            .bodyToMono<OllamaEmbeddingResponse>()
            .block()
            ?: throw IllegalStateException(
                "Empty response from Ollama embedding API"
            )

        return response.embeddings.firstOrNull()
            ?: throw IllegalStateException(
                "Ollama returned no embedding"
            )
    }
}