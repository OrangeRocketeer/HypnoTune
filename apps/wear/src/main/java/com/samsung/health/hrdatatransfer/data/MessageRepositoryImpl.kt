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
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Node
import kotlinx.coroutines.tasks.await
import java.nio.charset.Charset
import javax.inject.Inject

private const val TAG = "MessageRepositoryImpl"

class MessageRepositoryImpl @Inject constructor(
    private val messageClient: MessageClient,
) : MessageRepository {

    override suspend fun sendMessage(message: String, node: Node, messagePath: String): Boolean {
        val nodeId = node.id
        var result = false
        nodeId.also { id ->
            messageClient
                .sendMessage(
                    id,
                    messagePath,
                    message.toByteArray(charset = Charset.defaultCharset())
                ).apply {
                    addOnSuccessListener {
                        Log.i(TAG, "sendMessage OnSuccessListener")
                        result = true
                    }
                    addOnFailureListener {
                        Log.i(TAG, "sendMessage OnFailureListener")
                        result = false
                    }
                }.await()
            Log.i(TAG, "result: $result")
            return result
        }
    }
}