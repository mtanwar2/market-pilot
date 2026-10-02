package com.tanwar.market_pilot.llm.tool.impl

import com.tanwar.market_pilot.llm.tool.Tool
import com.tanwar.market_pilot.portfolio.Portfolio
import com.tanwar.market_pilot.portfolio.service.PortfolioService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class GetPortfolioTool(
    private val portfolioService: PortfolioService,
) : Tool {

    override val name: String = "getPortfolio"

    override fun execute(
        arguments: Map<String, Any?>
    ): Portfolio {

        val portfolioIdValue =
            arguments["portfolioId"]
                ?: throw IllegalArgumentException(
                    "Missing required argument: portfolioId"
                )

        val portfolioId = try {
            UUID.fromString(
                portfolioIdValue.toString()
            )
        } catch (ex: IllegalArgumentException) {
            throw IllegalArgumentException(
                "Invalid portfolioId: $portfolioIdValue"
            )
        }

        log.info(
            "Executing getPortfolio for portfolioId={}",
            portfolioId
        )

        return portfolioService.getPortfolio(
            portfolioId
        )
    }

    companion object {
        private val log =
            LoggerFactory.getLogger(
                GetPortfolioTool::class.java
            )
    }
}