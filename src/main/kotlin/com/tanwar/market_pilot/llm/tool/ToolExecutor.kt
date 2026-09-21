package com.tanwar.market_pilot.llm.tool

import com.tanwar.market_pilot.llm.model.ToolCall
import org.springframework.stereotype.Component

@Component
class ToolExecutor(
    private val toolRegistry: ToolRegistry
) {

    fun execute(toolCall: ToolCall): Any {

        val tool = toolRegistry.getTool(toolCall.name)

        return tool.execute(toolCall.arguments)
    }
}