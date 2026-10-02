package com.tanwar.market_pilot.rag.chunking

import org.springframework.stereotype.Component

@Component
class TextChunker {

    fun chunk(text: String): List<String> {
        return text
            .split("\n\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }
}