package com.tanwar.market_pilot.llm.model

data class ToolResult(
    val toolCallId: String,
    val content: String
)