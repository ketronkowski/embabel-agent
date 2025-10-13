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
package com.embabel.agent.core

import com.embabel.common.core.types.HasInfoString
import com.embabel.common.core.types.NamedAndDescribed
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * Type known to the Embabel agent platform.
 * May be backed by a domain object or by a map.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.DEDUCTION,
)
@JsonSubTypes(
    JsonSubTypes.Type(value = DynamicType::class, name = "dynamic"),
    JsonSubTypes.Type(value = JvmType::class, name = "jvm"),
)
sealed interface DomainType : HasInfoString, NamedAndDescribed {

    /**
     * Exposed even for JvmTypes, for consistency
     */
    val properties: List<PropertyDefinition>

    fun isAssignableFrom(other: Class<*>): Boolean

    fun isAssignableFrom(other: DomainType): Boolean

    fun isAssignableTo(other: Class<*>): Boolean

    fun isAssignableTo(other: DomainType): Boolean

}

data class PropertyDefinition(
    val name: String,
    val type: String = "string",
    val description: String? = name,
)