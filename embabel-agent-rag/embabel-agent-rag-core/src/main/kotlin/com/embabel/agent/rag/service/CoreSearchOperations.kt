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
package com.embabel.agent.rag.service

import com.embabel.agent.rag.model.Retrievable
import com.embabel.common.core.types.SimilarityResult
import com.embabel.common.core.types.TextSimilaritySearchRequest

/**
 * Tag interface for earch operations
 */
sealed interface SearchOperations

/**
 * RAG building blocks
 * Implemented by types that can search for chunks or other retrievables
 * Ease to expose to LLMs via tools
 */
interface VectorSearch : SearchOperations {

    /**
     * Perform classic vector search
     */
    fun <T : Retrievable> vectorSearch(
        request: TextSimilaritySearchRequest,
        clazz: Class<T>,
    ): List<SimilarityResult<T>>

}

interface TextSearch : SearchOperations {
    /**
     * Performs full-text search using Lucene query syntax.
     * Not all implementations will support all capabilities (such as fuzzy matching).
     * However, the use of quotes for phrases and + / - for required / excluded terms should be widely supported.
     *
     * The "query" field of request supports the following syntax:
     *
     * ## Basic queries
     * - `machine learning` - matches documents containing either term (implicit OR)
     * - `+machine +learning` - both terms required (AND)
     * - `"machine learning"` - exact phrase match
     *
     * ## Modifiers
     * - `+term` - term must appear
     * - `-term` - term must not appear
     * - `term*` - prefix wildcard
     * - `term~` - fuzzy match (edit distance)
     * - `term~0.8` - fuzzy match with similarity threshold
     *
     * ## Query Field Examples
     * ```
     * // Find chunks mentioning either kotlin or java
     * "kotlin java"
     *
     * // Find chunks with both "error" and "handling"
     * "+error +handling"
     *
     * // Find exact phrase
     * "\"null pointer exception\""
     *
     * // Find "test" but exclude "unit"
     * "+test -unit"
     * ```
     *
     * @param request the text similarity search request
     * @param clazz the type of [Retrievable] to search
     * @return matching results ranked by BM25 relevance score
     */
    fun <T : Retrievable> textSearch(
        request: TextSimilaritySearchRequest,
        clazz: Class<T>,
    ): List<SimilarityResult<T>>

    fun <T : Retrievable> regexSearch(
        regex: Regex,
        topK: Int,
        clazz: Class<T>,
    ): List<SimilarityResult<T>>
}

/**
 * Commonly implemented set of search functionality
 */
interface CoreSearchOperations : VectorSearch, TextSearch
