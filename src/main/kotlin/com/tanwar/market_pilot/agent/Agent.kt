package com.tanwar.market_pilot.agent

import com.tanwar.market_pilot.llm.audit.AuditContext
import com.tanwar.market_pilot.llm.model.LlmMessage
import com.tanwar.market_pilot.llm.model.LlmResponse
import reactor.core.publisher.Mono

interface Agent {

    fun run(messages: List<LlmMessage>, auditContext: AuditContext): Mono<LlmResponse>
}