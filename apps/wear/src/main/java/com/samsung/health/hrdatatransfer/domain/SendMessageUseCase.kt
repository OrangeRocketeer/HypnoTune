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

package com.samsung.health.hrdatatransfer.domain

import android.util.Log
import com.samsung.health.data.TrackedData
import com.samsung.health.hrdatatransfer.data.MessageRepository
import com.samsung.health.hrdatatransfer.data.TrackingRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject

private const val TAG = "SendMessageUseCase"

private const val MESSAGE_PATH = "/msg"

class SendMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val trackingRepository: TrackingRepository,
    private val getCapableNodes: GetCapableNodes
) {
    suspend operator fun invoke(): Boolean {

        val nodes = getCapableNodes()

        return if (nodes.isNotEmpty()) {

            val node = nodes.first()
            val message =
                encodeMessage(trackingRepository.getValidHrData())
            messageRepository.sendMessage(message, node, MESSAGE_PATH)

            true

        } else {
            Log.i(TAG, "Ain't no nodes around")
            false
        }
    }

    /***********************************************************************
     * [Practice 3] - Encode Heart Rate & Inter-beat interval into String
     *  - encode function argument (trackedData) into Json format.
     *  - Return the encoded string
    -----------------------------------------------------------------------
     *  - (Hint)
     *  Use Json.encodeToString function
     **********************************************************************/

    fun encodeMessage(trackedData: ArrayList<TrackedData>): String {

        //TODO 3
        return Json.encodeToString(trackedData)
    }
}