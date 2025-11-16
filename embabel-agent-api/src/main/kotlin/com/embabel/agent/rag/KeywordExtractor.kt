/*
 * Copyright 2024-2025 Embabel Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.embabel.agent.rag

import com.embabel.common.core.types.ZeroToOne

/**
 * Can extract keywords from text
 */
interface KeywordExtractor {

    val keywords: Set<String>

    fun extractKeywords(text: String): Set<String>

    /**
     * Converts a match count to a similarity score between 0 and 1.
     *
     * This default implementation uses a square root function to provide nonlinear scoring that is more generous
     * to partial matches. For example, matching 3 out of 10 keywords yields ~0.548
     * rather than 0.3, reflecting that partial matches are quite valuable.
     *
     * The formula is: sqrt(matchCount / totalKeywords)
     *
     * This approach gives diminishing returns as more keywords match, which aligns
     * with information retrieval principles where early matches are most significant.
     *
     * @param matchCount the number of keywords that matched
     * @return a similarity score from 0.0 to 1.0
     */
    fun matchCountToScore(matchCount: Int): ZeroToOne {
        val fraction = matchCount.toDouble() / keywords.size.toDouble()
        return kotlin.math.sqrt(fraction).coerceAtMost(1.0)
    }

}
