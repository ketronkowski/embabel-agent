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

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DomainTypeAssignabilityTest {

    // Test class hierarchy for inheritance tests
    abstract class AbstractBase
    open class ConcreteBase : AbstractBase()
    class ConcreteDerived : ConcreteBase()
    class UnrelatedClass

    interface TestInterface
    class ImplementingClass : TestInterface

    @Nested
    inner class DynamicTypeAssignability {

        @Test
        fun `DynamicType isAssignableFrom always returns false`() {
            val dynamicType = DynamicType(name = "TestType")

            assertFalse(dynamicType.isAssignableFrom(String::class.java))
            assertFalse(dynamicType.isAssignableFrom(Int::class.java))
            assertFalse(dynamicType.isAssignableFrom(ConcreteBase::class.java))
            assertFalse(dynamicType.isAssignableFrom(Any::class.java))
        }

        @Test
        fun `DynamicType isAssignableTo always returns false`() {
            val dynamicType = DynamicType(name = "TestType")

            assertFalse(dynamicType.isAssignableTo(String::class.java))
            assertFalse(dynamicType.isAssignableTo(Int::class.java))
            assertFalse(dynamicType.isAssignableTo(ConcreteBase::class.java))
            assertFalse(dynamicType.isAssignableTo(Any::class.java))
        }
    }

    @Nested
    inner class JvmTypeAssignability {

        @Test
        fun `JvmType isAssignableFrom with same class returns true`() {
            val jvmType = JvmType(String::class.java)
            assertTrue(jvmType.isAssignableFrom(String::class.java))
        }

        @Test
        fun `JvmType isAssignableTo with same class returns true`() {
            val jvmType = JvmType(String::class.java)
            assertTrue(jvmType.isAssignableTo(String::class.java))
        }

        @Test
        fun `JvmType isAssignableFrom with subclass returns true`() {
            val jvmType = JvmType(ConcreteBase::class.java)
            assertTrue(jvmType.isAssignableFrom(ConcreteDerived::class.java))
        }

        @Test
        fun `JvmType isAssignableTo with superclass returns true`() {
            val jvmType = JvmType(ConcreteDerived::class.java)
            assertTrue(jvmType.isAssignableTo(ConcreteBase::class.java))
        }

        @Test
        fun `JvmType isAssignableFrom with superclass returns false`() {
            val jvmType = JvmType(ConcreteDerived::class.java)
            assertFalse(jvmType.isAssignableFrom(ConcreteBase::class.java))
        }

        @Test
        fun `JvmType isAssignableTo with subclass returns false`() {
            val jvmType = JvmType(ConcreteBase::class.java)
            assertFalse(jvmType.isAssignableTo(ConcreteDerived::class.java))
        }

        @Test
        fun `JvmType isAssignableFrom with unrelated class returns false`() {
            val jvmType = JvmType(ConcreteBase::class.java)
            assertFalse(jvmType.isAssignableFrom(UnrelatedClass::class.java))
        }

        @Test
        fun `JvmType isAssignableTo with unrelated class returns false`() {
            val jvmType = JvmType(ConcreteBase::class.java)
            assertFalse(jvmType.isAssignableTo(UnrelatedClass::class.java))
        }

        @Test
        fun `JvmType isAssignableFrom with abstract superclass`() {
            val jvmType = JvmType(AbstractBase::class.java)
            assertTrue(jvmType.isAssignableFrom(ConcreteBase::class.java))
            assertTrue(jvmType.isAssignableFrom(ConcreteDerived::class.java))
        }

        @Test
        fun `JvmType isAssignableTo with abstract superclass`() {
            val jvmType = JvmType(ConcreteBase::class.java)
            assertTrue(jvmType.isAssignableTo(AbstractBase::class.java))
        }

        @Test
        fun `JvmType isAssignableFrom with Object superclass`() {
            val jvmType = JvmType(Any::class.java)
            assertTrue(jvmType.isAssignableFrom(String::class.java))
            assertTrue(jvmType.isAssignableFrom(ConcreteBase::class.java))
            assertTrue(jvmType.isAssignableFrom(ConcreteDerived::class.java))
        }

        @Test
        fun `JvmType isAssignableTo with Object superclass`() {
            val jvmType = JvmType(String::class.java)
            assertTrue(jvmType.isAssignableTo(Any::class.java))

            val derivedType = JvmType(ConcreteDerived::class.java)
            assertTrue(derivedType.isAssignableTo(Any::class.java))
        }

        @Test
        fun `JvmType isAssignableFrom with interface`() {
            val jvmType = JvmType(TestInterface::class.java)
            assertTrue(jvmType.isAssignableFrom(ImplementingClass::class.java))
        }

        @Test
        fun `JvmType isAssignableTo with interface`() {
            val jvmType = JvmType(ImplementingClass::class.java)
            assertTrue(jvmType.isAssignableTo(TestInterface::class.java))
        }

        @Test
        fun `JvmType isAssignableFrom with non-implementing class and interface returns false`() {
            val jvmType = JvmType(TestInterface::class.java)
            assertFalse(jvmType.isAssignableFrom(ConcreteBase::class.java))
        }

        @Test
        fun `JvmType isAssignableTo with non-implementing class and interface returns false`() {
            val jvmType = JvmType(ConcreteBase::class.java)
            assertFalse(jvmType.isAssignableTo(TestInterface::class.java))
        }

        @Test
        fun `JvmType isAssignableFrom with multi-level inheritance`() {
            val jvmType = JvmType(AbstractBase::class.java)
            assertTrue(jvmType.isAssignableFrom(ConcreteBase::class.java))
            assertTrue(jvmType.isAssignableFrom(ConcreteDerived::class.java))

            val baseType = JvmType(ConcreteBase::class.java)
            assertTrue(baseType.isAssignableFrom(ConcreteDerived::class.java))
        }

        @Test
        fun `JvmType isAssignableTo with multi-level inheritance`() {
            val jvmType = JvmType(ConcreteDerived::class.java)
            assertTrue(jvmType.isAssignableTo(ConcreteBase::class.java))
            assertTrue(jvmType.isAssignableTo(AbstractBase::class.java))
        }
    }
}
