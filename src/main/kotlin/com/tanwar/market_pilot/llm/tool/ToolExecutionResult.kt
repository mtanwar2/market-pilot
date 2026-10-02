package com.tanwar.market_pilot.llm.tool

data class ToolExecutionResult(
    val success: Boolean,
    val content: Any? = null,
    val error: ToolExecutionError? = null
)

data class ToolExecutionError(
    val code: String,
    val message: String
)
