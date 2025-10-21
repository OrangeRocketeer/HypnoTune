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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samsung.health.data.TrackedData

import com.samsung.health.mobile.R

@Composable
fun ListView(
    results: List<TrackedData>
) {
    LazyColumn {
        items(results) { result ->
            ResultRow(result)
        }
    }
}

@Composable
fun ResultRow(result: TrackedData) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Column(
            modifier = Modifier.width(220.dp),
            verticalArrangement = Arrangement.spacedBy((-14).dp),
            horizontalAlignment = Alignment.Start,
        ) {

            Text(
                textAlign = TextAlign.Start,
                fontSize = 20.sp,
                text = stringResource(R.string.hr_name),
                color = Color.Gray,
            )
            Text(
                textAlign = TextAlign.Start,
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