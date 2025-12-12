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
package com.embabel.agent.api.annotation.support

import com.embabel.agent.api.annotation.AchievesGoal
import com.embabel.agent.api.annotation.Action
import com.embabel.agent.api.annotation.Agent
import com.embabel.agent.api.annotation.Trigger
import com.embabel.agent.core.AgentProcessStatusCode
import com.embabel.agent.core.ProcessOptions
import com.embabel.agent.test.integration.IntegrationTestUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import com.embabel.agent.core.Agent as CoreAgent

/**
 * Domain types for trigger testing
 */
data class UserMessage(val content: String)
data class SystemContext(val sessionId: String)
data class Response(val reply: String)

data class EventA(val data: String)
data class EventB(val data: String)
data class ProcessedResult(val source: String, val processed: String)

/**
 * Agent that uses @Trigger to react only when UserMessage is the last result.
 * Both UserMessage and SystemContext must be present, but the action only fires
 * when UserMessage was just added.
 */
@Agent(description = "Chat agent with trigger-based message handling")
class TriggerChatAgent {

    @AchievesGoal(description = "Respond to user message")
    @Action
    fun handleMessage(
        @Trigger userMessage: UserMessage,
        systemContext: SystemContext
    ): Response {
        return Response("Session ${systemContext.sessionId}: Received '${userMessage.content}'")
    }
}

/**
 * Agent without @Trigger - action fires as soon as both parameters are available,
 * regardless of which was added last.
 */
@Agent(description = "Chat agent without trigger")
class NonTriggerChatAgent {

    @AchievesGoal(description = "Respond to user message")
    @Action
    fun handleMessage(
        userMessage: UserMessage,
        systemContext: SystemContext
    ): Response {
        return Response("Session ${systemContext.sessionId}: Received '${userMessage.content}'")
    }
}

/**
 * Agent with multiple potential triggers - demonstrates selective reaction.
 */
@Agent(description = "Multi-event processor")
class MultiEventAgent {

    @Action
    fun processEventA(
        @Trigger eventA: EventA,
        eventB: EventB
    ): ProcessedResult {
        return ProcessedResult("A", "Processed A: ${eventA.data} with B: ${eventB.data}")
    }

    @AchievesGoal(description = "Process event B")
    @Action
    fun processEventB(
        eventA: EventA,
        @Trigger eventB: EventB
    ): ProcessedResult {
        return ProcessedResult("B", "Processed B: ${eventB.data} with A: ${eventA.data}")
    }
}

class TriggerAnnotationTest {

    private val reader = AgentMetadataReader()

    @Nested
    inner class BasicTriggerBehavior {

        @Test
        fun `action with trigger fires when trigger parameter is last result`() {
            val agent = reader.createAgentMetadata(TriggerChatAgent()) as CoreAgent
            val ap = IntegrationTestUtils.dummyAgentPlatform()

            // Add SystemContext first, then UserMessage (trigger)
            val process = ap.runAgentFrom(
                agent,
                ProcessOptions(),
                linkedMapOf(
                    "systemContext" to SystemContext("session-123"),
                    "it" to UserMessage("Hello!")
                )
            )

            assertEquals(AgentProcessStatusCode.COMPLETED, process.status)
            val result = process.getValue("it", Response::class.java.name) as? Response
            assertNotNull(result)
            assertTrue(result!!.reply.contains("Hello!"))
            assertTrue(result.reply.contains("session-123"))
        }

        @Test
        fun `action without trigger fires regardless of parameter order`() {
            val agent = reader.createAgentMetadata(NonTriggerChatAgent()) as CoreAgent
            val ap = IntegrationTestUtils.dummyAgentPlatform()

            // Add UserMessage first, then SystemContext
            val process = ap.runAgentFrom(
                agent,
                ProcessOptions(),
                mapOf(
                    "userMessage" to UserMessage("Hello!"),
                    "it" to SystemContext("session-456")
                )
            )

            assertEquals(AgentProcessStatusCode.COMPLETED, process.status)
            val result = process.getValue("it", Response::class.java.name) as? Response
            assertNotNull(result)
            assertTrue(result!!.reply.contains("Hello!"))
        }
    }

    @Nested
    inner class MultiEventTrigger {

        @Test
        fun `correct action fires based on which event is last result`() {
            val agent = reader.createAgentMetadata(MultiEventAgent()) as CoreAgent
            val ap = IntegrationTestUtils.dummyAgentPlatform()

            // Add EventA first, then EventB (trigger for processEventB)
            val process = ap.runAgentFrom(
                agent,
                ProcessOptions(),
                mapOf(
                    "eventA" to EventA("dataA"),
                    "it" to EventB("dataB")
                )
            )

            assertEquals(AgentProcessStatusCode.COMPLETED, process.status)
            val result = process.getValue("it", ProcessedResult::class.java.name) as? ProcessedResult
            assertNotNull(result)
            // Should have processed via processEventB since EventB was the trigger
            assertEquals("B", result!!.source)
            assertTrue(result.processed.contains("Processed B"))
        }
    }

    @Nested
    inner class MetadataReading {

        @Test
        fun `agent with trigger has correct action structure`() {
            val agent = reader.createAgentMetadata(TriggerChatAgent()) as CoreAgent
            val actionNames = agent.actions.map { it.name }
            println("Actions: $actionNames")
            assertTrue(actionNames.any { it.contains("handleMessage") })
        }

        @Test
        fun `multi-event agent has both actions`() {
            val agent = reader.createAgentMetadata(MultiEventAgent()) as CoreAgent
            val actionNames = agent.actions.map { it.name }
            println("Actions: $actionNames")
            assertTrue(actionNames.any { it.contains("processEventA") })
            assertTrue(actionNames.any { it.contains("processEventB") })
        }
    }
}
