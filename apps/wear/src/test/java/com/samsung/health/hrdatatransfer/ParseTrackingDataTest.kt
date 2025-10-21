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

import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.ValueKey
import com.samsung.health.hrdatatransfer.data.IBIDataParsing
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class ParseTrackingDataTest {

    private var dataPoint: DataPoint = mockk(relaxed = true)

    @Test
    fun parsing_isCorrect() {

        every { dataPoint.getValue(ValueKey.HeartRateSet.IBI_LIST) } returns listOf(
            1000,
            20000,
            334343
        )
        every { dataPoint.getValue(ValueKey.HeartRateSet.IBI_STATUS_LIST) } returns listOf(
            0,
            -1,
            -1
        )

        assertEquals(IBIDataParsing.getValidIbiList(dataPoint), listOf(1000))
    }

    @Test
    fun parsing_isCorrect2() {

        every { dataPoint.getValue(ValueKey.HeartRateSet.IBI_LIST) } returns listOf(
            1000,
            20000,
            334343
        )
        every { dataPoint.getValue(ValueKey.HeartRateSet.IBI_STATUS_LIST) } returns listOf(
            -1,
            -1,
            -1
        )

        assertEquals(IBIDataParsing.getValidIbiList(dataPoint), emptyList<Int>())
    }
}