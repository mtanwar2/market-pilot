package com.tanwar.market_pilot.rag.model

import jakarta.validation.constraints.NotBlank

data class RagAskRequest(
    @field:NotBlank
    val question: String
)

data class RagAskResponse(
    val answer: String
)