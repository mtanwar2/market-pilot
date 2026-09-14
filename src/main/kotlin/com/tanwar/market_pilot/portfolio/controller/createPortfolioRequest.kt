package com.tanwar.market_pilot.portfolio.controller

import jakarta.validation.constraints.NotBlank

data class CreatePortfolioRequest(

    @field:NotBlank
    val name: String
)