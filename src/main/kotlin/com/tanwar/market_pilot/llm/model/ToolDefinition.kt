package com.tanwar.market_pilot.llm.model

data class ToolDefinition(
    val name: String,
    val description: String,
    val parameters: Map<String, Any?>
)