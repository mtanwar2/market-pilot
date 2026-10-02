object RetrievalMetrics {

    fun precision(
        retrievedTexts: List<String>,
        relevantTexts: List<String>
    ): Double {

        if (retrievedTexts.isEmpty()) {
            return 0.0
        }

        val relevantCount = retrievedTexts.count { retrieved ->
            relevantTexts.any { relevant ->
                retrieved.contains(relevant, ignoreCase = true)
            }
        }

        return relevantCount.toDouble() / retrievedTexts.size
    }

    fun recall(
        retrievedTexts: List<String>,
        relevantTexts: List<String>
    ): Double {

        if (relevantTexts.isEmpty()) {
            return 0.0
        }

        val retrievedRelevantCount = relevantTexts.count { relevant ->
            retrievedTexts.any { retrieved ->
                retrieved.contains(relevant, ignoreCase = true)
            }
        }

        return retrievedRelevantCount.toDouble() / relevantTexts.size
    }

    fun reciprocalRank(
        retrievedTexts: List<String>,
        relevantTexts: List<String>
    ): Double {

        val firstRelevantIndex = retrievedTexts.indexOfFirst { retrieved ->
            relevantTexts.any { relevant ->
                retrieved.contains(relevant, ignoreCase = true)
            }
        }

        if (firstRelevantIndex == -1) {
            return 0.0
        }

        return 1.0 / (firstRelevantIndex + 1)
    }

    fun mrr(
        results: List<Pair<List<String>, List<String>>>
    ): Double {

        if (results.isEmpty()) {
            return 0.0
        }

        val reciprocalRanks = results.map { (retrieved, relevant) ->
            reciprocalRank(
                retrievedTexts = retrieved,
                relevantTexts = relevant
            )
        }

        return reciprocalRanks.average()
    }
}