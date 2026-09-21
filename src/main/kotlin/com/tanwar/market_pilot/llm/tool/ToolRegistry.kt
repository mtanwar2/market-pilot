package com.tanwar.market_pilot.llm.tool

import org.springframework.stereotype.Component

@Component
class ToolRegistry(
    tools: List<Tool>
) {

    private val toolsByName =
        tools.associateBy { it.name }

    fun getTool(name: String): Tool =
        toolsByName[name]
            ?: throw IllegalArgumentException(
                "Unknown tool: $name"
            )
}