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
package com.embabel.agent.rag.ingestion

import com.embabel.agent.rag.model.ContainerSection
import com.embabel.agent.rag.model.ContentRoot
import com.embabel.agent.rag.model.LeafSection
import com.embabel.agent.rag.model.NavigableSection

interface NavigableContainerSection : ContainerSection, NavigableSection {

    fun descendants(): List<NavigableSection> =
        children + children.filterIsInstance<NavigableContainerSection>().flatMap { containerChild ->
            containerChild.descendants()
        }

    fun leaves(): List<LeafSection> =
        children.filterIsInstance<LeafSection>() +
                children.filterIsInstance<NavigableContainerSection>().flatMap { containerChild ->
                    containerChild.leaves()
                }
}

data class DefaultMaterializedContainerSection(
    override val id: String,
    override val uri: String? = null,
    override val title: String,
    override val children: List<NavigableSection>,
    override val parentId: String? = null,
    override val metadata: Map<String, Any?> = emptyMap(),
) : NavigableContainerSection

/**
 * Document we can navigate through descendants of
 */
interface NavigableDocument : ContentRoot, NavigableContainerSection {

    override fun labels(): Set<String> = super<ContentRoot>.labels() + super<NavigableContainerSection>.labels()

}

/**
 * In-memory representation of a document with sections.
 */
data class MaterializedDocument(
    override val id: String,
    override val uri: String,
    override val title: String,
    override val children: List<NavigableSection>,
    override val metadata: Map<String, Any?> = emptyMap(),
) : NavigableDocument
