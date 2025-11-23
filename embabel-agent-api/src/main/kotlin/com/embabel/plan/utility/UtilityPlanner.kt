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
import com.embabel.plan.common.condition.AbstractConditionPlanner
import com.embabel.plan.common.condition.ConditionPlan
import com.embabel.plan.common.condition.ConditionPlanningSystem
import com.embabel.plan.common.condition.WorldStateDeterminer
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
        TODO("Not yet implemented")
    }

    override fun prune(planningSystem: ConditionPlanningSystem): ConditionPlanningSystem {
        return planningSystem
    }
}
