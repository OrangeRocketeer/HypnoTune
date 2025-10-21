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
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.HealthTrackingService
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.HealthTrackerType
import com.samsung.android.service.health.tracking.data.ValueKey
import com.samsung.health.data.TrackedData
import com.samsung.health.hrdatatransfer.R
import com.samsung.health.hrdatatransfer.data.IBIDataParsing.Companion.getValidIbiList
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "TrackingRepositoryImpl"

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class TrackingRepositoryImpl
@Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val healthTrackingServiceConnection: HealthTrackingServiceConnection,
    @ApplicationContext private val context: Context,
) : TrackingRepository {

    private val trackingType = HealthTrackerType.HEART_RATE_CONTINUOUS
    private var listenerSet = false
    private var healthTrackingService: HealthTrackingService? = null

    var errors: HashMap<String, Int> = hashMapOf(
        "0" to R.string.error_initial_state,
        "-2" to R.string.error_wearable_movement_detected,
        "-3" to R.string.error_wearable_detached,
        "-8" to R.string.error_low_ppg_signal,
        "-10" to R.string.error_low_ppg_signal_even_more,
        "-999" to R.string.error_other_sensor_running,
        "SDK_POLICY_ERROR" to R.string.SDK_POLICY_ERROR,
        "PERMISSION_ERROR" to R.string.PERMISSION_ERROR
    )

    private val maxValuesToKeep = 40
    private var heartRateTracker: HealthTracker? = null
    private var validHrData = ArrayList<TrackedData>()

    override fun getValidHrData(): ArrayList<TrackedData> {
        return validHrData
    }

    private fun isHRValid(hrStatus: Int): Boolean {
        return hrStatus == 1
    }

    private fun trimDataList() {
        val howManyElementsToRemove = validHrData.size - maxValuesToKeep
        repeat(howManyElementsToRemove) { validHrData.removeFirstOrNull() }
    }

    @ExperimentalCoroutinesApi
    override suspend fun track(): Flow<TrackerMessage> = callbackFlow {
        val updateListener = object : HealthTracker.TrackerEventListener {
            override fun onDataReceived(dataPoints: MutableList<DataPoint>) {

                for (dataPoint in dataPoints) {

                    var trackedData: TrackedData? = null
                    val hrValue = dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE)
                    val hrStatus = dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE_STATUS)

                    if (isHRValid(hrStatus)) {
                        trackedData = TrackedData()
                        trackedData.hr = hrValue
                        Log.i(TAG, "valid HR: $hrValue")
                    } else {
                        coroutineScope.runCatching {
                            trySendBlocking(TrackerMessage.TrackerWarningMessage(getError(hrStatus.toString())))
                        }
                    }

                    val validIbiList = getValidIbiList(dataPoint)
                    if (validIbiList.isNotEmpty()) {
                        if (trackedData == null) trackedData = TrackedData()
                        trackedData.ibi.addAll(validIbiList)
                    }

                    if ((isHRValid(hrStatus) || validIbiList.isNotEmpty()) && trackedData != null) {
                        coroutineScope.runCatching {
                            trySendBlocking(TrackerMessage.DataMessage(trackedData))
                        }
                    }
                    if (trackedData != null) {
                        validHrData.add(trackedData)
                    }
                }
                trimDataList()
            }

            fun getError(errorKeyFromTracker: String): String {
                val str = errors.getValue(errorKeyFromTracker)
                return context.resources.getString(str)
            }

            override fun onFlushCompleted() {

                Log.i(TAG, "onFlushCompleted()")
                coroutineScope.runCatching {
                    trySendBlocking(TrackerMessage.FlushCompletedMessage)
                }
            }

            override fun onError(trackerError: HealthTracker.TrackerError?) {

                Log.i(TAG, "onError()")
                coroutineScope.runCatching {
                    trySendBlocking(TrackerMessage.TrackerErrorMessage(getError(trackerError.toString())))
                }
            }
        }

        heartRateTracker =
            healthTrackingService!!.getHealthTracker(trackingType)

        setListener(updateListener)

        awaitClose {
            Log.i(TAG, "Tracking flow awaitClose()")
            stopTracking()
        }
    }

    override fun stopTracking() {
        unsetListener()
    }

    private fun unsetListener() {
        if (listenerSet) {
            heartRateTracker?.unsetEventListener()
            listenerSet = false
        }
    }

    private fun setListener(listener: HealthTracker.TrackerEventListener) {
        if (!listenerSet) {
            heartRateTracker?.setEventListener(listener)
            listenerSet = true
        }
    }

    override fun hasCapabilities(): Boolean {
        Log.i(TAG, "hasCapabilities()")
        healthTrackingService = healthTrackingServiceConnection.getHealthTrackingService()
        val trackers: List<HealthTrackerType> =
            healthTrackingService!!.trackingCapability.supportHealthTrackerTypes
        return trackers.contains(trackingType)
    }
}

sealed class TrackerMessage {
    class DataMessage(val trackedData: TrackedData) : TrackerMessage()
    object FlushCompletedMessage : TrackerMessage()
    class TrackerErrorMessage(val trackerError: String) : TrackerMessage()
    class TrackerWarningMessage(val trackerWarning: String) : TrackerMessage()
}