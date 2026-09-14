package com.tanwar.market_pilot.recommendation.prompt

import org.springframework.stereotype.Component

@Component
class RecommendationPromptBuilder {

    fun build(stock: String): String {
        return """
            Analyze the stock: $stock.

            Provide an investment recommendation based on the information
            available to you.

            Return ONLY valid JSON using exactly this structure:

            {
              "recommendation": "BUY | SELL | HOLD",
              "confidence": 0.0,
              "reasons": [
                "reason 1",
                "reason 2"
              ],
              "risks": [
                "risk 1",
                "risk 2"
              ]
            }

            Rules:
            - recommendation must be BUY, SELL, or HOLD
            - confidence must be between 0.0 and 1.0
            - reasons must contain at least one item
            - risks must contain at least one item
            - do not include markdown
            - do not include ```json
            - return only the JSON object
        """.trimIndent()
    }
}