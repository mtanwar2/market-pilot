package com.tanwar.market_pilot.portfolio.controller

import com.tanwar.market_pilot.portfolio.model.Holding
import com.tanwar.market_pilot.portfolio.service.HoldingService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/portfolios/{portfolioId}/holdings")
class HoldingController(
    private val holdingService: HoldingService
) {

    @PostMapping
    fun addHolding(
        @PathVariable portfolioId: UUID,
        @Valid @RequestBody request: CreateHoldingRequest
    ): ResponseEntity<Holding> {

        val holding = holdingService.addHolding(
            portfolioId = portfolioId,
            request = request
        )

        return ResponseEntity.ok(holding)
    }
}