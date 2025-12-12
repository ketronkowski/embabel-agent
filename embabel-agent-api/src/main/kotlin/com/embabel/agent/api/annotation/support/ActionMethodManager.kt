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

import com.embabel.agent.api.annotation.Trigger
import com.embabel.agent.api.common.TransformationActionContext
import com.embabel.agent.core.Action
import com.embabel.agent.core.IoBinding
import org.springframework.ai.tool.ToolCallback
import org.springframework.core.KotlinDetector
import java.lang.reflect.Method
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.kotlinFunction

/**
 * Creates and invokes actions from annotated methods.
 */
interface ActionMethodManager {

    /**
     * Strategies for resolving action method parameters into argument values
     * Handles core types such as Ai and OperationContext, but can be
     * extended to support custom parameter types.
     */
    val argumentResolvers: List<ActionMethodArgumentResolver>

    /**
     * Create an Action from a method
     * @param method the method to create an action from
     * @param instance instance of Agent or AgentCapabilities-annotated class
     * @param toolCallbacksOnInstance tool callbacks to use from instance level
     */
    fun createAction(
        method: Method,
        instance: Any,
        toolCallbacksOnInstance: List<ToolCallback>,
    ): Action

    /**
     * Invoke the action method on the given instance.
     */
    fun <O> invokeActionMethod(
        method: Method,
        instance: Any,
        actionContext: TransformationActionContext<List<Any>, O>,
    ): O
}

/**
 * Find the type of the parameter annotated with @Trigger, if any.
 * Shared between DefaultActionMethodManager and StateActionMethodManager.
 */
internal fun findTriggerType(method: Method): Class<*>? {
    val kotlinFunction = if (KotlinDetector.isKotlinReflectPresent()) method.kotlinFunction else null
    for (i in method.parameters.indices) {
        val javaParameter = method.parameters[i]
        val kotlinParameter = kotlinFunction?.valueParameters?.getOrNull(i)

        // Check Kotlin annotation first
        if (kotlinParameter?.findAnnotation<Trigger>() != null) {
            return javaParameter.type
        }
        // Check Java annotation
        if (javaParameter.getAnnotation(Trigger::class.java) != null) {
            return javaParameter.type
        }
    }
    return null
}

/**
 * Generate the data binding precondition for a @Trigger parameter type.
 * Uses the standard binding format "lastResult:fully.qualified.Type" which is
 * evaluated by BlackboardWorldStateDeterminer.
 */
internal fun triggerPrecondition(triggerType: Class<*>): String =
    "${IoBinding.LAST_RESULT_BINDING}:${triggerType.name}"
