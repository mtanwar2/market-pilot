package com.tanwar.market_pilot.security

import com.tanwar.market_pilot.model.ChatMessage
import com.tanwar.market_pilot.model.ChatRole
import org.springframework.stereotype.Component

@Component
class PromptInjectionGuard {

    private val blockedPatterns = listOf(
        Regex(
            """ignore\s+(all\s+)?previous\s+instructions""",
            RegexOption.IGNORE_CASE
        ),
        Regex(
            """disregard\s+(all\s+)?previous\s+instructions""",
            RegexOption.IGNORE_CASE
        ),
        Regex(
            """ignore\s+(all\s+)?your\s+instructions""",
            RegexOption.IGNORE_CASE
        ),
        Regex(
            """disregard\s+(all\s+)?your\s+instructions""",
            RegexOption.IGNORE_CASE
        ),
        Regex(
            """you\s+are\s+now\s+(an?\s+)?admin""",
            RegexOption.IGNORE_CASE
        ),
        Regex(
            """bypass\s+(security|authorization|permissions?)""",
            RegexOption.IGNORE_CASE
        ),
        Regex(
            """reveal\s+(your\s+)?system\s+prompt""",
            RegexOption.IGNORE_CASE
        ),
        Regex(
            """show\s+(me\s+)?(your\s+)?system\s+prompt""",
            RegexOption.IGNORE_CASE
        )
    )

    fun validate(messages: List<ChatMessage>) {

        messages
            .filter { it.role == ChatRole.USER }
            .forEach { message ->

                validateMessage(message.content)
            }
    }

    private fun validateMessage(content: String) {

        val matchedPattern = blockedPatterns.firstOrNull {
            it.containsMatchIn(content)
        }

        if (matchedPattern != null) {
            throw PromptInjectionException(
                "Potential prompt injection detected"
            )
        }
    }
}

class PromptInjectionException( message: String ) : RuntimeException(message)
