package com.samsung.health.hrdatatransfer.domain

import android.util.Log
import com.samsung.health.hrdatatransfer.data.TrackingRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

private const val TAG = "AutoSendDataUseCase"

class AutoSendDataUseCase @Inject constructor(
    private val trackingRepository: TrackingRepository,
    private val sendMessageUseCase: SendMessageUseCase
) {
    operator fun invoke(intervalMillis: Long = 5000): Flow<Boolean> = flow {
        while (true) {
            delay(intervalMillis)

            val validHrData = trackingRepository.getValidHrData()
            if (validHrData.isNotEmpty()) {
                val success = sendMessageUseCase()
                Log.i(TAG, "Auto-send result: $success")
                emit(success)
            } else {
                Log.i(TAG, "No valid HR data to send yet")
                emit(false)
            }
        }
    }
}