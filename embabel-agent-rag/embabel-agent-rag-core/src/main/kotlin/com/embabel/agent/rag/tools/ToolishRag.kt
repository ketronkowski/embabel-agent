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
package com.embabel.agent.rag.tools

import com.embabel.agent.api.common.LlmReference
import com.embabel.agent.rag.model.Chunk
import com.embabel.agent.rag.service.*
import com.embabel.common.core.types.ZeroToOne
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam

/**
 * Reference for fine-grained RAG tools, allowing the LLM to
 * control individual search operations directly.
 */
class ToolishRag @JvmOverloads constructor(
    override val name: String,
    override val description: String,
    private val searchOperations: SearchOperations,
    val goal: String = DEFAULT_GOAL,
    val formatter: RagResponseFormatter = SimpleRagResponseFormatter,
) : LlmReference {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun toolInstances(): List<Any> =
        buildList {
            if (searchOperations is VectorSearch) {
                logger.info("Adding VectorSearchTools to ToolishRag tools {}", name)
                add(VectorSearchTools(searchOperations))
            }
            if (searchOperations is TextSearch) {
                logger.info("Adding TextSearchTools to ToolishRag tools {}", name)
                add(TextSearchTools(searchOperations))
            }
            if (searchOperations is ChunkExpander) {
                logger.info("Adding ChunkExpansionTools to ToolishRag tools {}", name)
                add(ChunkExpansionTools(searchOperations))
            }
        }

    override fun notes() = """
        ${
        (searchOperations as? TextSearch)?.let {
            "Lucene search syntax support: ${searchOperations.luceneSyntaxNotes}\n"
        }
    }
        Search acceptance criteria:
        $goal
      """.trimIndent()

    companion object {
        val DEFAULT_GOAL = """
            Continue search until the question is answered, or you have to give up.
            Be creative, try different types of queries. Generate HyDE queries if needed.
            Be thorough and try different approaches.
            If nothing works, report that you could not find the answer.
        """.trimIndent()
    }
}

/**
 * Classic vector search
 */
class VectorSearchTools(
    private val vectorSearch: VectorSearch,
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Tool(description = "Perform vector search. Specify topK and similarity threshold from 0-1")
    fun vectorSearch(
        query: String,
        topK: Int,
        @ToolParam(description = "similarity threshold from 0-1") threshold: ZeroToOne,
    ): String {
        logger.info("Performing vector search with query='{}', topK={}, threshold={}", query, topK, threshold)
        val results = vectorSearch.vectorSearch(
            RagRequest.query(query).withTopK(topK).withSimilarityThreshold(threshold),
            Chunk::class.java
        )
        return SimpleRagResponseFormatter.formatResults(SimilarityResults.fromList(results))
    }
}

/**
 * Tools to expand chunks around an anchor chunk that has already been retrieved
 */
class ChunkExpansionTools(
    private val chunkExpander: ChunkExpander,
) {

    @Tool(description = "given a chunk ID, expand to surrounding chunks")
    fun broadenChunk(
        @ToolParam(description = "id of the chunk to expand") chunkId: String,
        @ToolParam(description = "chunksToAdd", required = false) chunksToAdd: Int = 2,
    ): String {
        val expandedChunks = chunkExpander.expandChunk(chunkId, ChunkExpander.Method.SEQUENCE, chunksToAdd)
        return expandedChunks.joinToString("\n") { chunk ->
            "Chunk ID: ${chunk.id}\nContent: ${chunk.text}\n"
        }
    }

    // TODO related chunk expansion based on vector similarity

}

/**
 * Tools to perform text search operations with Lucene syntax
 */
class TextSearchTools(
    private val textSearch: TextSearch,
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Tool(
        description = """
        Perform BMI25 search. Specify topK and similarity threshold from 0-1
        Query follows Lucene syntax, e.g. +term for required terms, -term for negative terms,
        "quoted phrases", wildcards (*), fuzzy (~).
    """
    )
    fun textSearch(
        @ToolParam(
            description = """"
            Query in Lucene syntax,
            e.g. +term for required terms, -term for negative terms,
            quoted phrases", wildcards (*), fuzzy (~).
        """
        )
        query: String,
        topK: Int,
        @ToolParam(description = "similarity threshold from 0-1") threshold: ZeroToOne,
    ): String {
        logger.info("Performing text search with query='{}', topK={}, threshold={}", query, topK, threshold)
        val results = textSearch.textSearch(
            RagRequest.query(query).withTopK(topK).withSimilarityThreshold(threshold),
            Chunk::class.java
        )
        return SimpleRagResponseFormatter.formatResults(SimilarityResults.fromList(results))
    }

    @Tool(description = "Perform regex search across chunks. Specify topK")
    fun regexSearch(
        regex: String,
        topK: Int,
    ): String {
        logger.info("Performing regex search with regex='{}', topK={}", regex, topK)
        val results = textSearch.regexSearch(Regex(regex), topK, Chunk::class.java)
        return SimpleRagResponseFormatter.formatResults(SimilarityResults.Companion.fromList(results))
    }

    // entity search

    // get entity

    // expand entity

}
