package com.tanwar.market_pilot.llm.tool

import org.springframework.stereotype.Service

@Service
class ToolAuthorizationService {

    fun authorize(toolName: String) {

        when (toolName) {

            "getStockPrice",
            "getPortfolio" -> {
                return
            }

            else -> {
                throw ToolAuthorizationException(
                    "Tool execution is not authorized: $toolName"
                )
            }
        }
    }
}

class ToolAuthorizationException( message: String ) : RuntimeException(message)
