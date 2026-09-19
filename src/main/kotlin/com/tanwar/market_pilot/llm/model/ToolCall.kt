package com.tanwar.market_pilot.llm.model

data class ToolCall(
    val id: String? = null,
    val name: String,
    val arguments: Map<String, Any?>
)