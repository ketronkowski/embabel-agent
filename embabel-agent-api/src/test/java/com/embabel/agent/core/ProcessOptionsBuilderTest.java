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
package com.embabel.agent.core;

import com.embabel.agent.api.channel.DevNullOutputChannel;
import com.embabel.agent.api.channel.OutputChannel;
import com.embabel.agent.api.common.PlannerType;
import com.embabel.agent.api.event.AgenticEventListener;
import com.embabel.agent.core.support.InMemoryBlackboard;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProcessOptionsBuilderTest {

    @Test
    void withers() {
        var identities = new Identities();
        var blackboard = new InMemoryBlackboard();
        var listener = AgenticEventListener.DevNull;
        var verbosity = new Verbosity(true, true, true, true);
        var budget = new Budget(1, 2, 3);
        var processControl = new ProcessControl(
                Delay.MEDIUM,
                Delay.LONG,
                budget.earlyTerminationPolicy()
        );

        var po = ProcessOptions.DEFAULT
                .withContextId("42")
                .withIdentities(identities)
                .withBlackboard(blackboard)
                .withVerbosity(verbosity)
                .withBudget(budget)
                .withProcessControl(processControl)
                .withPrune(true)
                .withListener(listener);

        assertEquals("42", po.getContextIdString());
        assertEquals(identities, po.getIdentities());
        assertEquals(blackboard, po.getBlackboard());

        assertTrue(po.getVerbosity().getShowPrompts());
        assertTrue(po.getVerbosity().getShowLlmResponses());
        assertTrue(po.getVerbosity().getDebug());
        assertTrue(po.getVerbosity().getShowPlanning());

        assertEquals(1, po.getBudget().getCost());
        assertEquals(2, po.getBudget().getActions());
        assertEquals(3, po.getBudget().getTokens());

        assertEquals(Delay.MEDIUM, po.getControl().getToolDelay());
        assertEquals(Delay.LONG, po.getControl().getOperationDelay());
        assertTrue(po.getPrune());
        assertEquals(List.of(listener), po.getListeners());
    }

    @Test
    void withContextIdUsingContextIdType() {
        // ContextId is a value class, so we use the String overload which creates the ContextId
        var po = ProcessOptions.DEFAULT.withContextId("test-context");

        assertEquals("test-context", po.getContextIdString());
    }

    @Test
    void withContextIdUsingString() {
        var po = ProcessOptions.DEFAULT.withContextId("string-context");

        assertEquals("string-context", po.getContextIdString());
    }

    @Test
    void withContextIdNull() {
        var po = ProcessOptions.DEFAULT
                .withContextId("initial")
                .withContextId((String) null);

        assertNull(po.getContextIdString());
    }

    @Test
    void getContextIdStringReturnsNullWhenNotSet() {
        assertNull(ProcessOptions.DEFAULT.getContextIdString());
    }

    @Test
    void correctProcessControlDefault() {
        var identities = new Identities();
        var blackboard = new InMemoryBlackboard();
        var listener = AgenticEventListener.DevNull;
        var verbosity = new Verbosity(true, true, true, true);
        var budget = new Budget(1, 2, 3);

        var po = ProcessOptions.DEFAULT
                .withContextId("42")
                .withIdentities(identities)
                .withBlackboard(blackboard)
                .withVerbosity(verbosity)
                .withBudget(budget)
                .withListener(listener);

        assertEquals(Delay.NONE, po.getControl().getToolDelay());
        assertEquals(Delay.NONE, po.getControl().getOperationDelay());
        assertEquals(po.getBudget().earlyTerminationPolicy(), po.getControl().getEarlyTerminationPolicy(),
                "Should have default budget-based early termination policy");
        assertEquals(List.of(listener), po.getListeners());
    }

    @Test
    void processControlWithers() {
        var budget = Budget.DEFAULT;
        var newPolicy = EarlyTerminationPolicy.maxActions(100);
        var control = new ProcessControl(budget.earlyTerminationPolicy())
                .withToolDelay(Delay.MEDIUM)
                .withOperationDelay(Delay.LONG)
                .withEarlyTerminationPolicy(newPolicy);

        assertEquals(Delay.MEDIUM, control.getToolDelay());
        assertEquals(Delay.LONG, control.getOperationDelay());
        assertEquals(newPolicy, control.getEarlyTerminationPolicy());
    }

    @Test
    void identitiesWithers() {
        var identities = Identities.DEFAULT
                .withForUser(null)
                .withRunAs(null);

        assertEquals(Identities.DEFAULT, identities);
    }

    @Test
    void withListeners() {
        var listener1 = AgenticEventListener.DevNull;
        var listener2 = AgenticEventListener.DevNull;

        var po = ProcessOptions.DEFAULT
                .withListeners(List.of(listener1, listener2));

        assertEquals(List.of(listener1, listener2), po.getListeners());
    }

    @Test
    void withListener() {
        var listener = AgenticEventListener.DevNull;

        var po = ProcessOptions.DEFAULT.withListener(listener);

        assertEquals(List.of(listener), po.getListeners());
    }

    @Test
    void withListenerAccumulates() {
        var listener1 = AgenticEventListener.DevNull;
        var listener2 = AgenticEventListener.DevNull;

        var po = ProcessOptions.DEFAULT
                .withListener(listener1)
                .withListener(listener2);

        assertEquals(List.of(listener1, listener2), po.getListeners());
    }

    @Test
    void withOutputChannel() {
        OutputChannel customChannel = DevNullOutputChannel.INSTANCE;

        var po = ProcessOptions.DEFAULT.withOutputChannel(customChannel);

        assertEquals(customChannel, po.getOutputChannel());
    }

    @Test
    void withPlannerType() {
        var po = ProcessOptions.DEFAULT.withPlannerType(PlannerType.UTILITY);

        assertEquals(PlannerType.UTILITY, po.getPlannerType());
    }

    @Test
    void withBlackboardNull() {
        var blackboard = new InMemoryBlackboard();
        var po = ProcessOptions.DEFAULT
                .withBlackboard(blackboard)
                .withBlackboard(null);

        assertNull(po.getBlackboard());
    }

    @Test
    void withIdentities() {
        var identities = new Identities();

        var po = ProcessOptions.DEFAULT.withIdentities(identities);

        assertEquals(identities, po.getIdentities());
    }

    @Test
    void withVerbosity() {
        var verbosity = new Verbosity(true, false, true, false);

        var po = ProcessOptions.DEFAULT.withVerbosity(verbosity);

        assertEquals(verbosity, po.getVerbosity());
    }

    @Test
    void withBudget() {
        var budget = new Budget(5.0, 100, 500000);

        var po = ProcessOptions.DEFAULT.withBudget(budget);

        assertEquals(budget, po.getBudget());
    }

    @Test
    void withProcessControl() {
        var budget = Budget.DEFAULT;
        var control = new ProcessControl(Delay.LONG, Delay.MEDIUM, budget.earlyTerminationPolicy());

        var po = ProcessOptions.DEFAULT.withProcessControl(control);

        assertEquals(control, po.getControl());
    }

    @Test
    void withProcessControlNull() {
        var budget = Budget.DEFAULT;
        var control = new ProcessControl(Delay.LONG, Delay.MEDIUM, budget.earlyTerminationPolicy());

        var po = ProcessOptions.DEFAULT
                .withProcessControl(control)
                .withProcessControl(null);

        // When processControl is null, control property should return default based on budget
        assertEquals(Delay.NONE, po.getControl().getToolDelay());
        assertEquals(Delay.NONE, po.getControl().getOperationDelay());
    }

    @Test
    void withPrune() {
        var po = ProcessOptions.DEFAULT.withPrune(true);

        assertTrue(po.getPrune());
    }

    @Test
    void withPruneFalse() {
        var po = ProcessOptions.DEFAULT
                .withPrune(true)
                .withPrune(false);

        assertFalse(po.getPrune());
    }

}
