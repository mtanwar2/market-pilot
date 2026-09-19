package com.tanwar.market_pilot.recommendation.parser

import com.tanwar.market_pilot.recommendation.model.Recommendation
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tools.jackson.databind.ObjectMapper

class RecommendationParserTest {

    private val parser =
        RecommendationParser(ObjectMapper())

    @Test
    fun `should parse valid recommendation JSON`() {

        val json = """
            {
              "recommendations": [
                {
                  "symbol": "NVDA",
                  "recommendation": "BUY",
                  "confidence": 0.82,
                  "reasons": [
                    "Strong revenue growth",
                    "Increasing market demand"
                  ],
                  "risks": [
                    "High valuation"
                  ]
                }
              ]
            }
        """.trimIndent()

        val result = parser.parse(json)

        Assertions.assertEquals(
            1,
            result.recommendations.size
        )

        val recommendation = result.recommendations.first()

        Assertions.assertEquals(
            "NVDA",
            recommendation.symbol
        )

        Assertions.assertEquals(
            Recommendation.BUY,
            recommendation.recommendation
        )

        Assertions.assertEquals(
            0.82,
            recommendation.confidence
        )

        Assertions.assertEquals(
            2,
            recommendation.reasons.size
        )

        Assertions.assertEquals(
            1,
            recommendation.risks.size
        )
    }

    @Test
    fun `should parse multiple stock recommendations`() {

        val json = """
            {
              "recommendations": [
                {
                  "symbol": "NVDA",
                  "recommendation": "BUY",
                  "confidence": 0.82,
                  "reasons": ["Strong growth"],
                  "risks": ["High valuation"]
                },
                {
                  "symbol": "AAPL",
                  "recommendation": "HOLD",
                  "confidence": 0.61,
                  "reasons": ["Stable cash flow"],
                  "risks": ["Slowing hardware sales"]
                }
              ]
            }
        """.trimIndent()

        val result = parser.parse(json)

        Assertions.assertEquals(
            listOf("NVDA", "AAPL"),
            result.recommendations.map { it.symbol }
        )

        Assertions.assertEquals(
            Recommendation.HOLD,
            result.recommendations[1].recommendation
        )
    }

    @Test
    fun `should reject invalid JSON`() {

        val invalidJson = """
        {
          "recommendations": [
            {
              "symbol": "NVDA",
    """.trimIndent()

        assertThrows<Exception> {
            parser.parse(invalidJson)
        }
    }

    @Test
    fun `should reject missing recommendations array`() {

        val json = """
        {
          "recommendation": "BUY",
          "confidence": 0.82,
          "reasons": ["Strong growth"],
          "risks": ["High valuation"]
        }
    """.trimIndent()

        assertThrows<Exception> {
            parser.parse(json)
        }
    }

    @Test
    fun `should reject unsupported recommendation`() {

        val json = """
        {
          "recommendations": [
            {
              "symbol": "NVDA",
              "recommendation": "MAYBE",
              "confidence": 0.82,
              "reasons": ["Strong growth"],
              "risks": ["High valuation"]
            }
          ]
        }
    """.trimIndent()

        assertThrows<Exception> {
            parser.parse(json)
        }
    }

    @Test
    fun `should reject missing confidence`() {

        val json = """
        {
          "recommendations": [
            {
              "symbol": "NVDA",
              "recommendation": "BUY",
              "reasons": ["Strong growth"],
              "risks": ["High valuation"]
            }
          ]
        }
    """.trimIndent()

        assertThrows<Exception> {
            parser.parse(json)
        }
    }
}
