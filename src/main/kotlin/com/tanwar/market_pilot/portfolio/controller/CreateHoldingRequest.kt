package com.tanwar.market_pilot.portfolio.controller

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

data class CreateHoldingRequest(

    @field:NotBlank
    val symbol: String,

    @field:DecimalMin("0.0001")
    val quantity: BigDecimal,

    @field:DecimalMin("0.0001")
    val averagePrice: BigDecimal
)