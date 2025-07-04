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
package com.embabel.agent.event.logging.personality.hitchhiker

import com.embabel.agent.shell.MessageGeneratorPromptProvider
import com.embabel.common.util.RandomFromFileMessageGenerator
import com.embabel.common.util.bold
import com.embabel.common.util.color
import com.embabel.common.util.italic
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

fun guide(text: String) = "📕 ${" Guide ".bold()} ${text.italic().color(HitchhikerColorPalette.BABEL_GREEN)}"


val GuideEntries = listOf(
    "DON'T PANIC",
    "Mostly Harmless",
    "The Restaurant at the End of the Universe",
    "Life, the Universe and Everything",
    "So Long, and Thanks for All the Fish",
    "Milliways",
    "Infinite Improbability Drive",
    "Vogon Constructor Fleet",
    "Magrathea",
    "Heart of Gold",
    "Babel Fish",
    "Pan Galactic Gargle Blaster",
    "Trillian",
    "Zaphod Beeblebrox",
    "Arthur Dent",
    "Ford Prefect",
    "Marvin the Paranoid Android",
    "Deep Thought",
    "42",
)

@Component
@Profile("hh")
class HitchhikerPromptProvider : MessageGeneratorPromptProvider(
    color = HitchhikerColorPalette.BABEL_GREEN,
    prompt = GuideEntries.random(),
    messageGenerator = RandomFromFileMessageGenerator(
        url = "logging/hitchhiker.txt"
    ),
)
