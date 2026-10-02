package com.tanwar.market_pilot.rag.evaluation

object RagEvaluationDataset {

    val cases = listOf(

        EvaluationCase(
            question = "What is driving NVIDIA's Data Center revenue growth?",
            relevantTexts = listOf(
                "Data Center revenue for the second quarter was a record $89.0 billion, up 117% from a year ago and up 18% sequentially, driven by the ramp of our Blackwell Ultra infrastructure.",
                "Hyperscale revenue more than doubled from a year ago and increased 13% sequentially on the strength of Blackwell Ultra."
            ),
            expectedAnswer =
                "NVIDIA's Data Center revenue growth was driven primarily by the ramp of Blackwell Ultra infrastructure, with strong Hyperscale demand also contributing."
        ),

        EvaluationCase(
            question = "What drove NVIDIA's Edge Computing revenue increase?",
            relevantTexts = listOf(
                "Edge Computing revenue for the second quarter was $7.2 billion, up 27% from a year ago and up 13% sequentially. The increases were driven by strong sales of Blackwell workstations, partially offset by slower consumer PC sales that were tempered by elevated memory and systems prices."
            ),
            expectedAnswer =
                "Edge Computing revenue increased due to strong sales of Blackwell workstations, partially offset by slower consumer PC sales caused by elevated memory and systems prices."
        ),

        EvaluationCase(
            question = "Why did NVIDIA's operating expenses increase?",
            relevantTexts = listOf(
                "These increases were driven by higher compute infrastructure and compensation and benefits costs."
            ),
            expectedAnswer =
                "NVIDIA's operating expenses increased because of higher compute infrastructure costs and higher compensation and benefits costs."
        ),

        EvaluationCase(
            question = "What commitments has NVIDIA made to secure critical components?",
            relevantTexts = listOf(
                "We’ve partnered with our extensive network of suppliers to secure the critical components needed to meet demand for the next several years. Our commitments increased from $119 billion last quarter to $279 billion, primarily related to the procurement of memory."
            ),
            expectedAnswer =
                "NVIDIA has worked with suppliers to secure critical components for the next several years, increasing its commitments from $119 billion to $279 billion, primarily for memory procurement."
        ),

        EvaluationCase(
            question = "What risks could cause NVIDIA's actual results to differ from its expectations?",
            relevantTexts = listOf(
                "Important factors that could cause actual results to differ materially include: global economic and political conditions; our reliance on third parties to manufacture, assemble, package and test our products; the impact of technological development and competition; development of new products and technologies or enhancements to our existing products and technologies; market acceptance of our products or our partners’ products; design, manufacturing or software defects; changes in consumer preferences or demands; changes in industry standards and interfaces; unexpected loss of performance of our products or technologies when integrated into systems; our ability to realize the potential benefits of business investments or acquisitions; and changes in applicable laws and regulations"
            ),
            expectedAnswer =
                "NVIDIA identifies risks including global economic and political conditions, reliance on third-party manufacturing, technological competition, product acceptance, product defects, changing consumer demand, industry standards, business investments and acquisitions, and changes in laws and regulations."
        )
    )
}