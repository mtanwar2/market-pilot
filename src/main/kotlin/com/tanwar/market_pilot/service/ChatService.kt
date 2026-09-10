package com.tanwar.market_pilot.service

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.model.LlmMessage
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmRole
import com.tanwar.market_pilot.llm.properties.LlmProperties
import com.tanwar.market_pilot.model.ChatRequest
import com.tanwar.market_pilot.model.ChatResponse
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ChatService(
    private val llmClients: Map<String, LlmClient>,
    private val llmProperties: LlmProperties
) {

    fun chat(request: ChatRequest): ChatResponse {

        val conversationId =
            request.conversationId ?: UUID.randomUUID().toString()
        val provider = llmProperties.provider
        val providerConfig = llmProperties.providers[provider]
            ?: throw IllegalArgumentException(
                "No configuration found for provider: $provider"
            )

        val llmClient = llmClients[provider]
            ?: throw IllegalArgumentException(
                "No LLM client found for provider: $provider"
            )
        val llmRequest = LlmRequest(
            model = providerConfig.model,
            messages = listOf(
                LlmMessage(
                    role = LlmRole.USER,
                    content = request.message
                )
            )
        )

        val llmResponse = llmClient.generate(llmRequest)

        return ChatResponse(
            message = llmResponse.content,
            conversationId = conversationId
        )
    }
}