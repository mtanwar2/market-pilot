package com.tanwar.market_pilot.recommendation.prompt

import com.tanwar.market_pilot.portfolio.analysis.model.PortfolioAnalysis
import org.springframework.stereotype.Component

@Component
class RecommendationPromptBuilder {

    fun build(
        analysis: PortfolioAnalysis
    ): String {

        val holdings = analysis.holdings.joinToString("\n\n") { holding ->

            """
            Symbol: ${holding.symbol}
            Quantity: ${holding.quantity}
            Average Price: ${holding.averagePrice}
            Current Price: ${holding.currentPrice}
            Invested Amount: ${holding.investedAmount}
            Current Value: ${holding.currentValue}
            Profit: ${holding.profit}
            Profit Percentage: ${holding.profitPercentage}%
            Allocation Percentage: ${holding.allocationPercentage}%
            """.trimIndent()
        }

        return """
            You are a financial recommendation assistant.

            Analyze the following investment portfolio.

            Portfolio ID:
            ${analysis.portfolioId}

            Portfolio Summary:
            Total Invested: ${analysis.totalInvested}
            Total Current Value: ${analysis.totalCurrentValue}
            Total Profit: ${analysis.totalProfit}
            Total Profit Percentage: ${analysis.totalProfitPercentage}%

            Holdings:

            $holdings

            Based on the portfolio information above, provide one
            recommendation for every holding listed.

            Return ONLY valid JSON in exactly this format:

            {
              "recommendations": [
                {
                  "symbol": "NVDA",
                  "recommendation": "BUY",
                  "confidence": 0.82,
                  "reasons": [
                    "Reason 1",
                    "Reason 2"
                  ],
                  "risks": [
                    "Risk 1",
                    "Risk 2"
                  ]
                }
              ]
            }

            Rules:
            - include exactly one entry per holding, using the same symbol
            - recommendation must be one of: BUY, SELL, HOLD
            - confidence must be between 0.0 and 1.0
            - provide at least one reason per holding
            - provide at least one risk per holding
            - do not include markdown
            - do not include any text outside the JSON
        """.trimIndent()
    }
}