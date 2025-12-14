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
import com.embabel.agent.core.support.safelyGetToolCallbacks
import com.embabel.common.util.loggerFor
import org.springframework.ai.tool.ToolCallback
import org.springframework.ai.tool.definition.ToolDefinition

/**
 * Convenient way to programmatically export MCP tools
 * from Embabel ToolObject or LlmReference.
 */
interface McpToolExport : McpExportToolCallbackPublisher {

    companion object {

        /**
         * Export the tools on this ToolObject.
         */
        @JvmStatic
        fun fromToolObject(toolObject: ToolObject): McpToolExport =
            fromToolObjects(listOf(toolObject))

        @JvmStatic
        fun fromToolObjects(toolObjects: List<ToolObject>): McpToolExport {
            return McpToolExportImpl(
                toolCallbacks = safelyGetToolCallbacks(toolObjects)
                    .map { decorate(it) },
            )
        }

        /**
         * Export the tools on this LlmReference.
         * Note that the LlmReference prompt elements won't be exported,
         * so you will need to consider that when prompting downstream
         * LLMs to use the exported tools.
         */
        @JvmStatic
        fun fromLlmReference(llmReference: LlmReference): McpToolExport {
            return fromToolObject(llmReference.toolObject())
        }

        /**
         * Convenience method to export tools from multiple LlmReferences.
         */
        @JvmStatic
        fun fromLlmReferences(llmReferences: List<LlmReference>): McpToolExport {
            return fromToolObjects(llmReferences.map { it.toolObject() })
        }

        private fun decorate(toolCallback: ToolCallback): ToolCallback {
            return LoggingToolCallback(delegate = toolCallback)
        }

        private class LoggingToolCallback(private var delegate: ToolCallback) : ToolCallback {

            override fun getToolDefinition(): ToolDefinition = delegate.toolDefinition

            override fun call(toolInput: String): String {
                loggerFor<McpToolExport>().info("Calling tool ${delegate.toolDefinition.name()}($toolInput)")
                return delegate.call(toolInput)
            }
        }
    }
}

private class McpToolExportImpl(
    override val toolCallbacks: List<ToolCallback>,
) : McpToolExport {
    override fun infoString(
        verbose: Boolean?,
        indent: Int,
    ): String {
        return "McpToolExportCallbackPublisher with ${toolCallbacks.size} tool callbacks"
    }
}
