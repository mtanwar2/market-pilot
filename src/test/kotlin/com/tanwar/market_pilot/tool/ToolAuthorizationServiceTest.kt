package com.tanwar.market_pilot.llm.tool

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ToolAuthorizationServiceTest {

    private val authorizationService =
        ToolAuthorizationService()

    @Test
    fun `getStockPrice should be authorized`() {

        assertDoesNotThrow {
            authorizationService.authorize(
                "getStockPrice"
            )
        }
    }

    @Test
    fun `getPortfolio should be authorized`() {

        assertDoesNotThrow {
            authorizationService.authorize(
                "getPortfolio"
            )
        }
    }

    @Test
    fun `unknown tool should be rejected`() {

        assertThrows(
            ToolAuthorizationException::class.java
        ) {
            authorizationService.authorize(
                "deletePortfolio"
            )
        }
    }
}