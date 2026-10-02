package com.tanwar.market_pilot.agent

import com.tanwar.market_pilot.llm.model.LlmMessage

data class AgentState(
    val messages: MutableList<LlmMessage>,
    val round: Int = 0
)