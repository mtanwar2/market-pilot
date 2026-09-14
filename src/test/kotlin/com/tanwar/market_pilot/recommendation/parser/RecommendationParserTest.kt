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
        """.trimIndent()

        val result = parser.parse(json)

        Assertions.assertEquals(
            Recommendation.BUY,
            result.recommendation
        )

        Assertions.assertEquals(
            0.82,
            result.confidence
        )

        Assertions.assertEquals(
            2,
            result.reasons.size
        )

        Assertions.assertEquals(
            1,
            result.risks.size
        )
    }

    @Test
    fun `should reject invalid JSON`() {

        val invalidJson = """
        {
          "recommendation": "BUY",
          "confidence": 0.82,
    """.trimIndent()

        assertThrows<Exception> {
            parser.parse(invalidJson)
        }
    }

    @Test
    fun `should reject unsupported recommendation`() {

        val json = """
        {
          "recommendation": "MAYBE",
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
    fun `should reject missing confidence`() {

        val json = """
        {
          "recommendation": "BUY",
          "reasons": ["Strong growth"],
          "risks": ["High valuation"]
        }
    """.trimIndent()

        assertThrows<Exception> {
            parser.parse(json)
        }
    }
}