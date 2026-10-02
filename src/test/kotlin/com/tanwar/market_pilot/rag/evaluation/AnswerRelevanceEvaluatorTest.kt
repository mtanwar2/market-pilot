package com.tanwar.market_pilot.rag.evaluation

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.test.StepVerifier

@SpringBootTest
class AnswerRelevanceEvaluatorTest {

    @Autowired
    lateinit var evaluator: AnswerRelevanceEvaluator

    @Test
    fun `should evaluate answer relevance`() {

        val question =
            "What drove NVIDIA's Data Center revenue growth?"

        val generatedAnswer =
            "NVIDIA's Data Center revenue growth was driven primarily by the ramp of Blackwell Ultra infrastructure, with strong Hyperscale demand also contributing."

        StepVerifier.create(
            evaluator.evaluate(
                question = question,
                generatedAnswer = generatedAnswer,
                expectedAnswer =
                    "NVIDIA's Data Center revenue growth was driven by the ramp of Blackwell Ultra infrastructure and strong Hyperscale demand."
            )
        )
            .assertNext { result ->

                println("Score: ${result.score}")
                println("Reason: ${result.reason}")

                assert(result.score in 0.0..1.0)
                assert(result.reason.isNotBlank())
            }
            .verifyComplete()
    }

    @Test
    fun `should give low relevance score for irrelevant answer`() {

        val question =
            "What drove NVIDIA's Data Center revenue growth?"

        val generatedAnswer =
            "NVIDIA's Edge Computing revenue was $7.2 billion in the second quarter."

        StepVerifier.create(
            evaluator.evaluate(
                question = question,
                generatedAnswer = generatedAnswer,
                expectedAnswer =
                    "NVIDIA's Data Center revenue growth was driven by the ramp of Blackwell Ultra infrastructure and strong Hyperscale demand."
            )
        )
            .assertNext { result ->

                println("Score: ${result.score}")
                println("Reason: ${result.reason}")

                assert(result.score < 0.5)
            }
            .verifyComplete()
    }
}