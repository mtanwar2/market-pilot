package com.tanwar.market_pilot.portfolio.controller

import com.tanwar.market_pilot.portfolio.Portfolio
import com.tanwar.market_pilot.portfolio.service.PortfolioService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/portfolios")
class PortfolioController(
    private val portfolioService: PortfolioService
) {

    @PostMapping
    fun createPortfolio(
        @Valid @RequestBody request: CreatePortfolioRequest
    ): ResponseEntity<Portfolio> {

        val portfolio = Portfolio(
            null,
            name = request.name,
            createdAt = Instant.now()
        )

        val createdPortfolio =
            portfolioService.createPortfolio(portfolio)

        return ResponseEntity.ok(createdPortfolio)
    }

    @GetMapping("/{id}")
    fun getPortfolio(
        @PathVariable id: UUID
    ): ResponseEntity<Portfolio> {

        val portfolio = portfolioService.getPortfolio(id)

        return ResponseEntity.ok(portfolio)
    }
}