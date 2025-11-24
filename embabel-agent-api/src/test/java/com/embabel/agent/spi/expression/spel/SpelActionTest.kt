package com.embabel.agent.spi.expression.spel

import com.embabel.agent.api.annotation.support.AgentMetadataReader
import com.embabel.agent.api.common.PlannerType
import com.embabel.agent.core.AgentProcessStatusCode
import com.embabel.agent.core.ProcessOptions
import com.embabel.agent.test.integration.IntegrationTestUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import com.embabel.agent.core.Agent as CoreAgent

/**
 * Tests for SPEL-based action preconditions.
 */
class SpelActionTest {

    @Test
    fun `invoke two actions where second fires`() {
        val reader = AgentMetadataReader()
        val metadata =
            reader.createAgentMetadata(
                Spel2ActionsNoGoal()
            )
        assertNotNull(metadata)
        assertEquals(2, metadata!!.actions.size)

        val spelLogicalExpressionParser = SpelLogicalExpressionParser()

        val ap = IntegrationTestUtils.dummyAgentPlatform(
            logicalExpressionParser = spelLogicalExpressionParser
        )
        val agent = metadata as CoreAgent
        val agentProcess =
            ap.runAgentFrom(
                agent,
                ProcessOptions(plannerType = PlannerType.UTILITY),
                emptyMap(),
            )

        assertEquals(
            AgentProcessStatusCode.STUCK, agentProcess.status,
            "Should be stuck, not finished: status=${agentProcess.status}",
        )
        assertTrue(
            agentProcess.objects.any { it == Elephant("Zaboya", 30) },
            "Should have an elephant: blackboard=${agentProcess.objects}"
        )
        assertTrue(
            agentProcess.objects.any { it is Zoo },
            "Should have a zoo: blackboard=${agentProcess.objects}",
        )
    }

    @Test
    fun `invoke two actions where second does not fire`() {
        val reader = AgentMetadataReader()
        val metadata =
            reader.createAgentMetadata(
                Spel2ActionsYoungElephant()
            )
        assertNotNull(metadata)
        assertEquals(2, metadata!!.actions.size)
        
        val spelParser = SpelLogicalExpressionParser()

        val ap = IntegrationTestUtils.dummyAgentPlatform(
            logicalExpressionParser = spelParser
        )
        val agent = metadata as CoreAgent
        val agentProcess =
            ap.runAgentFrom(
                agent,
                ProcessOptions(plannerType = PlannerType.UTILITY),
                emptyMap(),
            )

        assertEquals(
            AgentProcessStatusCode.STUCK, agentProcess.status,
            "Should be stuck, not finished: status=${agentProcess.status}",
        )
        assertTrue(
            agentProcess.objects.any { it == Elephant("Dumbo", 15) },
            "Should have an elephant: blackboard=${agentProcess.objects}"
        )
        assertFalse(
            agentProcess.objects.any { it is Zoo },
            "Should have a zoo: blackboard=${agentProcess.objects}",
        )
    }

}