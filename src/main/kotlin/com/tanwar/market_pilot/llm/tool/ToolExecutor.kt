package com.tanwar.market_pilot.llm.tool

import com.tanwar.market_pilot.llm.audit.AuditContext
import com.tanwar.market_pilot.llm.audit.AuditEvent
import com.tanwar.market_pilot.llm.audit.AuditEventType
import com.tanwar.market_pilot.llm.audit.AuditLogger
import com.tanwar.market_pilot.llm.model.ToolCall
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class ToolExecutor(
    private val toolRegistry: ToolRegistry,
    private val toolAuthorizationService: ToolAuthorizationService,
    private val auditLogger: AuditLogger
) {

    fun execute(
        toolCall: ToolCall,
        auditContext: AuditContext
    ): ToolExecutionResult {

        // Authorization happens before the tool is executed.
        toolAuthorizationService.authorize(
            toolCall.name
        )

        // Find the requested tool.
        val tool = toolRegistry.getTool(
            toolCall.name
        )

        // Start measuring execution time.
        val startTime = Instant.now()

        return try {

            // Execute the actual tool.
            val result = tool.execute(
                toolCall.arguments
            )

            // Calculate execution duration.
            val durationMs =
                java.time.Duration.between(
                    startTime,
                    Instant.now()
                ).toMillis()

            // Record successful execution.
            auditLogger.log(
                AuditEvent(
                    eventType = AuditEventType.TOOL_EXECUTION,
                    userId = auditContext.userId,
                    conversationId = auditContext.conversationId,
                    turnId = auditContext.turnId,
                    toolName = toolCall.name,
                    toolCallId = toolCall.id,
                    success = true,
                    errorCode = null,
                    durationMs = durationMs
                )
            )

            ToolExecutionResult(
                success = true,
                content = result
            )

        } catch (ex: IllegalArgumentException) {

            val durationMs =
                java.time.Duration.between(
                    startTime,
                    Instant.now()
                ).toMillis()

            auditLogger.log(
                AuditEvent(
                    eventType = AuditEventType.TOOL_EXECUTION,
                    userId = auditContext.userId,
                    conversationId = auditContext.conversationId,
                    turnId = auditContext.turnId,
                    toolName = toolCall.name,
                    toolCallId = toolCall.id,
                    success = false,
                    errorCode = "INVALID_ARGUMENT",
                    durationMs = durationMs
                )
            )

            ToolExecutionResult(
                success = false,
                error = ToolExecutionError(
                    code = "INVALID_ARGUMENT",
                    message = ex.message
                        ?: "Invalid tool arguments"
                )
            )

        } catch (ex: Exception) {

            val durationMs =
                java.time.Duration.between(
                    startTime,
                    Instant.now()
                ).toMillis()

            auditLogger.log(
                AuditEvent(
                    eventType = AuditEventType.TOOL_EXECUTION,
                    userId = auditContext.userId,
                    conversationId = auditContext.conversationId,
                    turnId = auditContext.turnId,
                    toolName = toolCall.name,
                    toolCallId = toolCall.id,
                    success = false,
                    errorCode = "TOOL_EXECUTION_FAILED",
                    durationMs = durationMs
                )
            )

            ToolExecutionResult(
                success = false,
                error = ToolExecutionError(
                    code = "TOOL_EXECUTION_FAILED",
                    message = "Tool execution failed"
                )
            )
        }
    }

}
