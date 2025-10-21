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

package com.samsung.health.hrdatatransfer.presentation.ui

import android.health.connect.HealthPermissions
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.wear.compose.material.Text
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.samsung.health.hrdatatransfer.R


private const val TAG = "Permission"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Permission(
    onPermissionGranted: @Composable () -> Unit,
) {
    val permissionList: MutableList<String> = ArrayList()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        permissionList.add(HealthPermissions.READ_HEART_RATE)
    } else {
        permissionList.add(android.Manifest.permission.BODY_SENSORS)
    }
    val bodySensorPermissionState = rememberMultiplePermissionsState(permissionList)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    Log.i(TAG, "Lifecycle.Event.ON_START")
                    bodySensorPermissionState.launchMultiplePermissionRequest()
                }

                else -> {
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    })

    if (bodySensorPermissionState.allPermissionsGranted) {
        onPermissionGranted()
    } else {
        Row(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val textToShow = if (bodySensorPermissionState.shouldShowRationale) {
                stringResource(R.string.permission_should_show_rationale)
            } else {
                stringResource(R.string.permission_permanently_denied)
            }
            Text(
                modifier = Modifier.width(180.dp),
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                text = textToShow
            )
        }
    }
}