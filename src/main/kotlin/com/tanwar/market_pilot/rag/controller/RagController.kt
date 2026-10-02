package com.tanwar.market_pilot.rag.controller

import com.tanwar.market_pilot.rag.model.RagAskRequest
import com.tanwar.market_pilot.rag.model.RagAskResponse
import com.tanwar.market_pilot.rag.service.RagService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/rag")
class RagController(
    private val ragService: RagService
) {

    @PostMapping("/ask")
    fun ask(
        @Valid @RequestBody request: RagAskRequest
    ): RagAskResponse {

        val answer = ragService.generateAnswer(
            question = request.question
        )

        return RagAskResponse(
            answer = answer
        )
    }
}