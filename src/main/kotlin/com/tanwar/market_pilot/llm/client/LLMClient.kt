package com.tanwar.market_pilot.llm.client

import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse

interface LlmClient {
    fun generate(request: LlmRequest): LlmResponse
}
