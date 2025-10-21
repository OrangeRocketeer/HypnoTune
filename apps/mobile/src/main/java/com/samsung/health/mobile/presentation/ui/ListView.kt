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

package com.samsung.health.mobile.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samsung.health.data.TrackedData
import com.samsung.health.mobile.R

private val DarkBackground = Color(0xFF0F0F0F)
private val CardBackground = Color(0xFF1C1C1E)
private val AccentRed = Color(0xFFFF453A)
private val AccentBlue = Color(0xFF5E5CE6)
private val AccentOrange = Color(0xFFFF9500)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF8E8E93)

@Composable
fun ListView(
    results: List<TrackedData>
) {
    if (results.isEmpty()) {
        // Empty state
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = TextSecondary.copy(alpha = 0.3f),
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    "No health data yet",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Text(
                    "Start recording to see your data",
                    fontSize = 14.sp,
                    color = TextSecondary.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(results) { result ->
                ModernResultCard(result)
            }
        }
    }
}

@Composable
fun ModernResultCard(result: TrackedData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Heart Rate Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(AccentRed.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = AccentRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.hr_name),
                            fontSize = 13.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (result.hr == 0) "-" else "${result.hr}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                // BPM Label
                if (result.hr > 0) {
                    Box(
                        modifier = Modifier
                            .background(AccentRed.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "BPM",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentRed
                        )
                    }
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(TextSecondary.copy(alpha = 0.15f))
            )

            // IBI Section
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(AccentBlue.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Timeline,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.ibi_name),
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (result.ibi.isEmpty()) {
                        Text(
                            text = "-",
                            fontSize = 16.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = result.ibi.take(10).joinToString(", ") { it.toString() } +
                                    if (result.ibi.size > 10) "..." else "",
                            fontSize = 14.sp,
                            color = TextPrimary,
                            lineHeight = 20.sp
                        )
                        if (result.ibi.size > 10) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "+${result.ibi.size - 10} more values",
                                fontSize = 11.sp,
                                color = AccentBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Accelerometer Section
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(AccentOrange.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Speed,
                        contentDescription = null,
                        tint = AccentOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Accelerometer",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AccelValue("X", result.accelX)
                        AccelValue("Y", result.accelY)
                        AccelValue("Z", result.accelZ)
                    }
                }
            }
        }
    }
}

@Composable
fun AccelValue(axis: String, value: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = axis,
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = String.format("%.2f", value),
            fontSize = 14.sp,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ResultRow(result: TrackedData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Column(
            modifier = Modifier.width(220.dp),
            verticalArrangement = Arrangement.spacedBy((-14).dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                fontSize = 20.sp,
                text = stringResource(R.string.hr_name),
                color = Color.Gray,
            )
            Text(
                fontSize = 80.sp,
                text = if (result.hr == 0) "-" else result.hr.toString(),
                color = Color.White,
            )
            Spacer(modifier = Modifier.size(20.dp))
            Row {
                Text(
                    fontSize = 30.sp,
                    text = stringResource(R.string.ibi_name) + ": ",
                    color = Color.Gray
                )
                Text(
                    fontSize = 30.sp,
                    text = if (result.ibi.isEmpty()) "-" else result.ibi.joinToString { it.toString() },
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.size(30.dp))
        }
    }
}