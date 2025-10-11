package com.samsung.health.hrdatatransfer.data

import kotlinx.coroutines.flow.Flow

interface AccelerometerRepository {
    fun startTracking(): Flow<AccelerometerData>
    fun stopTracking()
}

data class AccelerometerData(
    val x: Float,
    val y: Float,
    val z: Float
)