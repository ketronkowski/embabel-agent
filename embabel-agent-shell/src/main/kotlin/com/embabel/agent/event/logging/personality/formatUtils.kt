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
package com.embabel.agent.event.logging.personality

import com.embabel.common.util.bold
import com.embabel.common.util.color
import com.embabel.common.util.italic

/**
 * Format a saying of a character
 */
fun character(name: String, text: String, color: Int): String {
    val namePart = if (name.isNotBlank()) {
        "${name.bold()}: "
    } else {
        ""
    }
    return "$namePart${text.italic().color(color)}"
}
