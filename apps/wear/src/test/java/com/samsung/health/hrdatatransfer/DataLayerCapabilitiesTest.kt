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

package com.samsung.health.hrdatatransfer

import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import com.samsung.health.hrdatatransfer.data.CapabilityRepository
import com.samsung.health.hrdatatransfer.data.CapabilityRepositoryImpl
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DataLayerCapabilitiesTest {

    private val capabilityClient: CapabilityClient = mockk(relaxed = true)
    private lateinit var capabilityRepository: CapabilityRepository

    @Before
    fun before() {
        capabilityRepository = CapabilityRepositoryImpl(capabilityClient)
    }

    @Test
    fun caps_isCorrect() = runTest {
        val node1: Node = mockk()
        every { node1.id } returns "node1"
        every { node1.displayName } returns "node1"
        every { node1.isNearby } returns true
        val node2: Node = mockk()
        every { node2.id } returns "node2"
        every { node2.displayName } returns "node2"
        every { node2.isNearby } returns true

        val allCaps = mapOf(node1 to setOf("a", "b"), node2 to setOf("a"))

        assertEquals(capabilityRepository.getNodesForCapability("a", allCaps), setOf(node1, node2))
    }

    @Test
    fun caps_isCorrect2() = runTest {
        val node1: Node = mockk()
        every { node1.id } returns "node1"
        every { node1.displayName } returns "node1"
        every { node1.isNearby } returns true
        val node2: Node = mockk()
        every { node2.id } returns "node2"
        every { node2.displayName } returns "node2"
        every { node2.isNearby } returns true

        val allCaps = mapOf(node1 to setOf("a", "b"), node2 to setOf("a"))

        assertEquals(capabilityRepository.getNodesForCapability("c", allCaps), setOf<Node>())
    }
}