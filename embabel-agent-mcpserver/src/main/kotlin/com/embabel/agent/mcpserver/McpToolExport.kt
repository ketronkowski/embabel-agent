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
package com.embabel.agent.mcpserver

import com.embabel.agent.api.common.LlmReference
import com.embabel.agent.api.common.ToolObject
import com.embabel.agent.core.support.safelyGetToolCallbacksFrom
import org.springframework.ai.tool.ToolCallback

/**
 * Convenient way to programmatically export MCP tools
 * from Embabel ToolObject or LlmReference.
 */
object McpToolExport {

    /**
     * Export the tools on this ToolObject.
     */
    @JvmStatic
    fun fromToolObject(toolObject: ToolObject): McpToolExportCallbackPublisher {
        return McpToolExportCallbackPublisherImpl(
            toolCallbacks = safelyGetToolCallbacksFrom(toolObject),
        )
    }

    /**
     * Export the tools on this LlmReference.
     * Note that the LlmReference prompt elements won't be exported,
     * so you will need to consider that when prompting downstream
     * LLMs to use the exported tools.
     */
    @JvmStatic
    fun fromLlmReference(llmReference: LlmReference): McpToolExportCallbackPublisher {
        return fromToolObject(llmReference.toolObject())
    }
}

private class McpToolExportCallbackPublisherImpl(
    override val toolCallbacks: List<ToolCallback>,
) : McpToolExportCallbackPublisher {
    override fun infoString(
        verbose: Boolean?,
        indent: Int,
    ): String {
        return "McpToolExportCallbackPublisher with ${toolCallbacks.size} tool callbacks"
    }
}
