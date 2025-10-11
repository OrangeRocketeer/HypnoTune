package com.yourapp.sleepclassifier.internal

import kotlinx.serialization.Serializable

// Input: The raw data received from the watch every 5 seconds
data class WatchDataPoint(
    val timestamp: Long,
    val heartRate: Float,
    val motionX: Float,
    val motionY: Float,
    val motionZ: Float
)

// Output: The final prediction result
sealed class PredictionResult {
    data class SleepStage(val stage: Stage, val confidence: Float) : PredictionResult()
    object InsufficientData : PredictionResult() // Used until the first 30s window is full
    object Error : PredictionResult()
}

// A type-safe enum for the sleep stages
enum class Stage {
    WAKE, LIGHT, DEEP, REM, UNKNOWN;

    companion object {
        fun fromInt(value: Int) = when (value) {
            0 -> WAKE
            1 -> LIGHT
            2 -> DEEP
            3 -> REM
            else -> UNKNOWN
        }
    }
}

// Internal model for holding the normalization stats from the JSON file
@Serializable
internal data class NormalizationStats(
    val mean: Map<String, Double>,
    val std: Map<String, Double>,
    val features: List<String> // This list preserves the exact feature order
)

// Internal model for a processed epoch before normalization
internal data class ProcessedEpoch(
    val features: Map<String, Float>,
    val sleepStage: Stage // The label, useful for the history buffer
)