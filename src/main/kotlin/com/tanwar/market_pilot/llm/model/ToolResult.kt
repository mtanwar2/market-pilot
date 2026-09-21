package com.tanwar.market_pilot.llm.model

data class ToolResult(
    val toolCallId: String? = null,
    val toolName: String,
    val content: Any? = null,
)