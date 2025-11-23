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
package com.embabel.plan.utility

import com.embabel.plan.Action
import com.embabel.plan.Goal
import com.embabel.plan.common.condition.*
import org.jetbrains.annotations.ApiStatus

/**
 * Planner using utility AI
 */
@ApiStatus.Experimental
class UtilityPlanner(
    worldStateDeterminer: WorldStateDeterminer,
) : AbstractConditionPlanner(worldStateDeterminer) {

    override fun planToGoal(
        actions: Collection<Action>,
        goal: Goal,
    ): ConditionPlan? {
        val currentState = worldStateDeterminer.determineWorldState()
        val availableActions = actions
            .filterIsInstance<ConditionAction>()
            .filter { it.isAchievable(currentState) }
            .map { Pair(it, it.netValue(currentState)) }
            .sortedByDescending { it.second }
        logger.info("${availableActions.size}/${actions.size} available: ${availableActions.map { "${it.first.name} - ${it.second}" }}")
        if (goal.name == NIRVANA && availableActions.isEmpty()) {
            // Special case. No available actions for Nirvana goal
            return null
        }
        return ConditionPlan(
            actions = listOfNotNull(availableActions.map { it.first }.firstOrNull()),
            goal = goal,
            worldState = currentState,
        )
    }

    override fun prune(planningSystem: ConditionPlanningSystem): ConditionPlanningSystem {
        return planningSystem
    }

    companion object {
        const val NIRVANA = "Nirvana"
    }
}
