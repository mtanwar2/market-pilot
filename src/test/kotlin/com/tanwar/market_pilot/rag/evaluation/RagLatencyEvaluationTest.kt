package com.tanwar.market_pilot.rag.evaluation

import com.tanwar.market_pilot.rag.service.RagService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertTrue

@SpringBootTest
class RagLatencyEvaluationTest {

    @Autowired
    lateinit var ragService: RagService

    @Test
    fun `should measure end to end RAG latency`() {

        val question =
            "What was NVIDIA's Data Center revenue growth?"

        val start = System.nanoTime()

        val answer = ragService.generateAnswer(
            question = question
        )

        val end = System.nanoTime()

        val latencyMs =
            (end - start) / 1_000_000

        println("Answer: $answer")
        println("RAG latency: ${latencyMs} ms")

        assertTrue(answer.isNotBlank())
        assertTrue(latencyMs >= 0)
    }

    @Test
    fun `should measure RAG latency across multiple requests`() {

        val question =
            "What was NVIDIA's Data Center revenue growth?"

        // Warm-up request
        ragService.generateAnswer(
            question = question
        )

        val latencies = mutableListOf<Long>()

        repeat(5) { index ->

            val start = System.nanoTime()

            val answer = ragService.generateAnswer(
                question = question
            )

            val end = System.nanoTime()

            val latencyMs =
                (end - start) / 1_000_000

            latencies.add(latencyMs)

            println(
                "Request ${index + 1}: ${latencyMs} ms"
            )

            assertTrue(answer.isNotBlank())
        }

        val averageLatency =
            latencies.average()

        val minLatency =
            latencies.minOrNull() ?: 0L

        val maxLatency =
            latencies.maxOrNull() ?: 0L

        val p50 =
            percentile(latencies, 50.0)

        val p95 =
            percentile(latencies, 95.0)

        val p99 =
            percentile(latencies, 99.0)

        println("P50 latency: $p50 ms")
        println("P95 latency: $p95 ms")
        println("P99 latency: $p99 ms")

        println("Latencies: $latencies")
        println("Average latency: $averageLatency ms")
        println("Minimum latency: $minLatency ms")
        println("Maximum latency: $maxLatency ms")
    }


    private fun percentile(
        values: List<Long>,
        percentile: Double
    ): Double {

        if (values.isEmpty()) {
            return 0.0
        }

        val sorted = values.sorted()

        val index =
            kotlin.math.ceil(
                percentile / 100.0 * sorted.size
            ).toInt() - 1

        return sorted[index.coerceIn(0, sorted.lastIndex)].toDouble()
    }
}