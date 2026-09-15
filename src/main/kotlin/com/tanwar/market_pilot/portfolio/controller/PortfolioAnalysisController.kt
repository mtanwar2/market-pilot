package com.tanwar.market_pilot.portfolio.controller

import com.tanwar.market_pilot.portfolio.analysis.model.PortfolioAnalysis
import com.tanwar.market_pilot.portfolio.service.PortfolioAnalysisService
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/portfolios")
class PortfolioAnalysisController(
    private val portfolioAnalysisService: PortfolioAnalysisService
) {

    @PostMapping("/{portfolioId}/analysis")
    fun analyzePortfolio(
        @PathVariable portfolioId: UUID
    ): PortfolioAnalysis {
        return portfolioAnalysisService.analyze(portfolioId)
    }
}