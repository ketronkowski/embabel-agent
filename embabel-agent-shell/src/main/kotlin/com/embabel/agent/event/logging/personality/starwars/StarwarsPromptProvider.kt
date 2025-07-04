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
package com.embabel.agent.event.logging.personality.starwars

import com.embabel.agent.event.logging.personality.ColorPalette
import com.embabel.agent.shell.MessageGeneratorPromptProvider
import com.embabel.common.util.RandomFromFileMessageGenerator
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("starwars")
object StarWarsColorPalette : ColorPalette {
    const val YELLOW_ACCENT: Int = 0xFFD100
    const val TATOOINE_ORANGE: Int = 0xAD7D37

    override val highlight: Int
        get() = YELLOW_ACCENT
    override val color2: Int
        get() = TATOOINE_ORANGE
}


@Component
@Profile("starwars")
class StarWarsPromptProvider : MessageGeneratorPromptProvider(
    color = StarWarsColorPalette.YELLOW_ACCENT,
    prompt = "starwars",
    messageGenerator = RandomFromFileMessageGenerator(
        url = "logging/starwars.txt"
    ),
)
