package com.tanwar.market_pilot.rag.evaluation

import com.tanwar.market_pilot.rag.service.RagService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertTrue

@SpringBootTest
class RagTokenUsageEvaluationTest {

    @Autowired
    lateinit var ragService: RagService

    @Autowired
    lateinit var llmClientFactory: com.tanwar.market_pilot.llm.client.LlmClientFactory

    @Test
    fun `should measure token usage for RAG request`() {

        val question =
            "What was NVIDIA's Data Center revenue growth?"

        val start = System.nanoTime()

        val result = ragService.generateAnswerWithUsage(
            question = question
        )

        val end = System.nanoTime()

        val latencyMs =
            (end - start) / 1_000_000

        val answer = result.first
        val usage = result.second

        println("Answer: $answer")
        println("Input tokens: ${usage.inputTokens}")
        println("Output tokens: ${usage.outputTokens}")
        println("Total tokens: ${usage.totalTokens}")
        println("Latency: $latencyMs ms")

        assertTrue(answer.isNotBlank())
    }

    @Test
    fun `should measure token usage across multiple RAG requests`() {

        val question =
            "What was NVIDIA's Data Center revenue growth?"

        // Warm-up request
        ragService.generateAnswerWithUsage(
            question = question
        )

        val inputTokens = mutableListOf<Long>()
        val outputTokens = mutableListOf<Long>()
        val totalTokens = mutableListOf<Long>()

        repeat(5) { index ->

            val result = ragService.generateAnswerWithUsage(
                question = question
            )

            val usage = result.second

            val input = usage.inputTokens ?: 0L
            val output = usage.outputTokens ?: 0L
            val total = usage.totalTokens ?: 0L

            inputTokens.add(input)
            outputTokens.add(output)
            totalTokens.add(total)

            println(
                "Request ${index + 1}: " +
                        "input=$input, " +
                        "output=$output, " +
                        "total=$total"
            )

            assertTrue(result.first.isNotBlank())
        }

        println()
        println("Input tokens: $inputTokens")
        println("Output tokens: $outputTokens")
        println("Total tokens: $totalTokens")

        println()
        println("Average input tokens: ${inputTokens.average()}")
        println("Average output tokens: ${outputTokens.average()}")
        println("Average total tokens: ${totalTokens.average()}")

        println("Minimum total tokens: ${totalTokens.minOrNull()}")
        println("Maximum total tokens: ${totalTokens.maxOrNull()}")
    }

    @Test
    fun `should measure token usage and latency together`() {

        val question =
            "What was NVIDIA's Data Center revenue growth?"

        // Warm-up
        ragService.generateAnswerWithUsage(
            question = question
        )

        val latencies = mutableListOf<Long>()
        val totalTokens = mutableListOf<Long>()

        repeat(5) { index ->

            val start = System.nanoTime()

            val result = ragService.generateAnswerWithUsage(
                question = question
            )

            val end = System.nanoTime()

            val latencyMs =
                (end - start) / 1_000_000

            val usage = result.second

            val total =
                usage.totalTokens ?: 0L

            latencies.add(latencyMs)
            totalTokens.add(total)

            println(
                "Request ${index + 1}: " +
                        "latency=${latencyMs} ms, " +
                        "input=${usage.inputTokens}, " +
                        "output=${usage.outputTokens}, " +
                        "total=${usage.totalTokens}"
            )

            assertTrue(result.first.isNotBlank())
        }

        println()
        println("Latencies: $latencies")
        println("Total tokens: $totalTokens")

        println()
        println("Average latency: ${latencies.average()} ms")
        println("Average total tokens: ${totalTokens.average()}")

        println("Minimum latency: ${latencies.minOrNull()} ms")
        println("Maximum latency: ${latencies.maxOrNull()} ms")

        println("Minimum total tokens: ${totalTokens.minOrNull()}")
        println("Maximum total tokens: ${totalTokens.maxOrNull()}")
    }
}