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

package com.samsung.health.mobile.data

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.samsung.health.mobile.ml.MLModelInterface
import com.samsung.health.mobile.presentation.HelpFunctions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "DataListenerService"
private const val MESSAGE_PATH = "/msg"

@AndroidEntryPoint
class DataListenerService : WearableListenerService() {

    @Inject
    lateinit var recordingRepository: RecordingRepository

    @Inject
    lateinit var mlModelInterface: MLModelInterface

    @Inject
    lateinit var stageManager: StageManager

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        val value = messageEvent.data.decodeToString()
        Log.i(TAG, "onMessageReceived(): $value")

        when (messageEvent.path) {
            MESSAGE_PATH -> {
                Log.i(TAG, "Service: message (/msg) received: $value")

                if (value != "") {
                    // Record the data if recording is active
                    try {
                        val measurementResults = HelpFunctions.decodeMessage(value)

                        if (recordingRepository.isRecording()) {
                            measurementResults.forEach { data ->
                                // Record the data to CSV
                                recordingRepository.recordData(data)

                                // ML INTEGRATION: Predict stage based on sensor data
                                val predictedStage = mlModelInterface.predictStage(data)

                                Log.d(TAG, "ML Prediction: Stage $predictedStage for HR=${data.hr}, " +
                                        "Accel=(${data.accelX}, ${data.accelY}, ${data.accelZ})")

                                // Broadcast stage prediction for MusicPlayerService to handle
                                sendBroadcast(Intent("com.samsung.health.mobile.STAGE_PREDICTION").apply {
                                    putExtra("predicted_stage", predictedStage)
                                })
                            }
                            Log.i(TAG, "Data recorded and ML prediction sent")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error recording data or predicting stage", e)
                    }

                    // Update UI with latest data (broadcast to MainActivity)
                    sendBroadcast(Intent("com.samsung.health.mobile.DATA_UPDATED").apply {
                        putExtra("message", value)
                    })
                } else {
                    Log.i(TAG, "value is an empty string")
                }
            }
        }
    }
}