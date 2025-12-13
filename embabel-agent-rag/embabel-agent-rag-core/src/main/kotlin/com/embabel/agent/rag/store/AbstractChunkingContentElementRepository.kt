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
package com.embabel.agent.rag.store

import com.embabel.agent.rag.ingestion.ContentChunker
import com.embabel.agent.rag.model.NavigableDocument
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Convenience base class for ChunkingContentElementRepository implementations.
 */
abstract class AbstractChunkingContentElementRepository(
    private val chunkerConfig: ContentChunker.Config,
) : ChunkingContentElementRepository {

    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * Will call save on the root and all descendants.
     * The database only needs to store each descendant and link by id,
     * rather than otherwise consider the entire structure.
     */
    final override fun writeAndChunkDocument(root: NavigableDocument): List<String> {
        val chunker = ContentChunker(chunkerConfig)
        val chunks = chunker.chunk(root)
            .map { enhance(it) }

        save(root)
        root.descendants().forEach { save(it) }
        chunks.forEach { save(it) }
        onNewRetrievables(chunks)
        createRelationships(root)
        commit()
        logger.info("Wrote and chunked document ${root.id} with ${chunks.size} chunks")
        return chunks.map { it.id }
    }

    /**
     * Create relationships between the structural elements in this content.
     * For example, in a graph database, create relationships between documents, sections, and chunks
     * based on their ids.
     * Not all implementations support this.
     */
    protected abstract fun createRelationships(root: NavigableDocument)

    /**
     * Commit after a write operation if needed.
     */
    protected abstract fun commit()

}
