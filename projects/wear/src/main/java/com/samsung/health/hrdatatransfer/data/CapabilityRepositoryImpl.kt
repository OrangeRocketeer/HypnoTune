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

import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "CapabilitiesRepositoryImpl"

@Singleton
class CapabilityRepositoryImpl @Inject constructor(
    private val capabilityClient: CapabilityClient
) : CapabilityRepository {

    override suspend fun getCapabilitiesForReachableNodes(): Map<Node, Set<String>> {
        Log.i(TAG, "getCapabilities()")

        val allCapabilities =
            capabilityClient.getAllCapabilities(CapabilityClient.FILTER_REACHABLE).await()

        return allCapabilities.flatMap { (capability, capabilityInfo) ->
            capabilityInfo.nodes.map {
                it to capability
            }
        }
            .groupBy(
                keySelector = { it.first },
                valueTransform = { it.second }
            )
            .mapValues { it.value.toSet() }
    }

    /**************************************************************************************
     * [Practice 2] Check capabilities for reachable remote nodes (devices)
     *  - return a Set of Node objects out of all capabilities represented by 2nd function
     *    argument, having the capability represented by 1st function argument.
     *  - return empty Set if no node has the capability
    --------------------------------------------------------------------------------------
     *  - (Hints)
     *  You might want to use filterValues function on the given allCapabilities Map
     * ***********************************************************************************/

    override suspend fun getNodesForCapability(
        capability: String,
        allCapabilities: Map<Node, Set<String>>
    ): Set<Node> {
        //TODO 2
        return allCapabilities.filterValues { capability in it }.keys
    }
}