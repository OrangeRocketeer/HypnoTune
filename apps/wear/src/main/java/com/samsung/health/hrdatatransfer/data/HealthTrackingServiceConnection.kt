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

package com.samsung.health.hrdatatransfer.data

import android.content.Context
import android.util.Log
import com.samsung.android.service.health.tracking.ConnectionListener
import com.samsung.android.service.health.tracking.HealthTrackerException
import com.samsung.android.service.health.tracking.HealthTrackingService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "HealthTrackingServiceConnection"

@Singleton
@ExperimentalCoroutinesApi
class HealthTrackingServiceConnection @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private var connected: Boolean = false
    private var healthTrackingService: HealthTrackingService? = null

    val connectionFlow = callbackFlow {
        val connectionListener = object : ConnectionListener {
            override fun onConnectionSuccess() {
                connected = true
                Log.i(TAG, "onConnectionSuccess()")
                coroutineScope.runCatching {
                    trySendBlocking(
                        ConnectionMessage.ConnectionSuccessMessage
                    )
                }
            }

            override fun onConnectionFailed(connectionException: HealthTrackerException?) {
                Log.i(TAG, "onConnectionFailed()")
                connected = false
                coroutineScope.runCatching {
                    trySendBlocking(ConnectionMessage.ConnectionFailedMessage(connectionException))
                }
            }

            override fun onConnectionEnded() {
                Log.i(TAG, "onConnectionEnded()")
                connected = false
                coroutineScope.runCatching {
                    trySendBlocking(ConnectionMessage.ConnectionEndedMessage)
                }
                Log.i(TAG, "before close()")
                close()
            }
        }
        Log.i(TAG, "healthTrackingService = HealthTrackingService(connectionListener, context)")
        healthTrackingService = HealthTrackingService(connectionListener, context)
        healthTrackingService!!.connectService()

        awaitClose {
            Log.i(TAG, "awaitClose: disconnect()")
            disconnect()
        }
    }

    private fun disconnect() {
        Log.i(TAG, "disconnect()")
        checkNotNull(healthTrackingService).disconnectService()
        connected = false
    }

    fun getHealthTrackingService(): HealthTrackingService? {
        return healthTrackingService
    }
}

sealed class ConnectionMessage {
    object ConnectionSuccessMessage : ConnectionMessage()
    class ConnectionFailedMessage(val exception: HealthTrackerException?) : ConnectionMessage()
    object ConnectionEndedMessage : ConnectionMessage()
}