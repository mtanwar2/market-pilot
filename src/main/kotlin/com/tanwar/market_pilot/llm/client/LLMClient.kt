package com.tanwar.market_pilot.llm.client

import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import reactor.core.publisher.Mono

interface LlmClient {
    fun generate(request: LlmRequest): Mono<LlmResponse>
}
