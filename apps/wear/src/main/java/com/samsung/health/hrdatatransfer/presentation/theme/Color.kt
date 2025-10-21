/*
 * Copyright 2023 Samsung Electronics Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.samsung.health.hrdatatransfer.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors

val Teal200 = Color(0xFF03DAC5)
val Red400 = Color(0xFFCF6679)
val Red = Color(0xFFbe4d25)
val Green = Color(0xFF96be25)
val GreyAAA = Color(0xFFAAAAAA)

internal val wearColorPalette: Colors = Colors(
    primary = Green,
    primaryVariant = Red,
    secondary = Teal200,
    error = Red400,
    onPrimary = GreyAAA,
    onSecondary = Color.Black,
    onError = Color.Black
)