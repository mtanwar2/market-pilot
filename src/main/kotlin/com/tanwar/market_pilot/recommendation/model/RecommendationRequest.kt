package com.tanwar.market_pilot.recommendation.model

import jakarta.validation.constraints.NotBlank

data class RecommendationRequest(
    @field:NotBlank
    val stock: String
)