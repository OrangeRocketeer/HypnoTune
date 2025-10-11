package com.yourapp.sleepclassifier.internal

import java.util.ArrayDeque
import kotlin.math.sqrt

/**
 * A stateless utility for calculating all features for a 30-second epoch.
 */
internal object FeatureProcessor {

    /**
     * Main function to generate features from a 30-second (6 samples) window.
     * This replicates the logic from your Python pipeline.
     */
    fun process30SecondWindow(
        window: List<WatchDataPoint>,
        history: ArrayDeque<ProcessedEpoch>
    ): ProcessedEpoch {
        // 1. Basic statistical features (from feature_engineering.py)
        val baseFeatures = calculateBaseFeatures(window)

        // 2. Temporal/Lag features (from temporal_features.py)
        val temporalFeatures = addTemporalFeatures(baseFeatures, history)

        // 3. Time since sleep onset (from temporal_features.py)
        val finalFeatures = addTimeSinceSleepOnset(temporalFeatures, history)

        // Use the last known label (or WAKE if unknown)
        val lastStage = Stage.fromInt(window.last().sleepStageLabel) // Assuming you add a placeholder label

        return ProcessedEpoch(features = finalFeatures, sleepStage = lastStage)
    }

    private fun calculateBaseFeatures(window: List<WatchDataPoint>): Map<String, Float> {
        val features = mutableMapOf<String, Float>()
        val hr = window.map { it.heartRate }

        features["hr_mean"] = hr.average().toFloat()
        features["hr_std"] = calculateStdDev(hr).toFloat()
        features["hr_min"] = hr.minOrNull() ?: 0f
        features["hr_max"] = hr.maxOrNull() ?: 0f
        features["hr_rmssd"] = calculateRmssd(hr).toFloat()

        for (axis in listOf("x", "y", "z")) {
            val motion = window.map {
                when (axis) {
                    "x" -> it.motionX
                    "y" -> it.motionY
                    else -> it.motionZ
                }
            }
            features["motion_${axis}_std"] = calculateStdDev(motion).toFloat()
            features["motion_${axis}_range"] = (motion.maxOrNull() ?: 0f) - (motion.minOrNull() ?: 0f)
        }
        return features
    }

    private fun addTemporalFeatures(
        currentFeatures: Map<String, Float>,
        history: ArrayDeque<ProcessedEpoch>
    ): Map<String, Float> {
        val newFeatures = currentFeatures.toMutableMap()
        val featuresToLag = listOf("hr_mean", "hr_std", "motion_x_std", "motion_y_std", "motion_z_std")

        for (lag in 1..4) {
            val pastEpoch = history.getOrNull(history.size - lag)
            for (featureName in featuresToLag) {
                val value = pastEpoch?.features?.get(featureName) ?: 0f
                newFeatures["${featureName}_lag_$lag"] = value
            }
        }
        // TODO: Add rolling features if needed, similar to the lag logic.
        return newFeatures
    }

    private fun addTimeSinceSleepOnset(
        currentFeatures: Map<String, Float>,
        history: ArrayDeque<ProcessedEpoch>
    ): Map<String, Float> {
        val newFeatures = currentFeatures.toMutableMap()
        var timeSinceOnset = 0
        var onsetDetected = false

        // Check history for the first sleep epoch
        for ((index, epoch) in history.withIndex()) {
            if (epoch.sleepStage != Stage.WAKE) {
                onsetDetected = true
                timeSinceOnset = history.size - index
                break
            }
        }

        // If current epoch is the onset
        val currentIsSleep = (window.last().sleepStageLabel > 0) // Simplified check
        if (!onsetDetected && currentIsSleep) {
            timeSinceOnset = 1
        }
        
        newFeatures["time_since_sleep_onset"] = timeSinceOnset.toFloat()
        return newFeatures
    }

    // Helper math functions
    private fun calculateStdDev(data: List<Float>): Double {
        if (data.size < 2) return 0.0
        val mean = data.average()
        val variance = data.sumOf { (it - mean) * (it - mean) }
        return sqrt(variance / (data.size - 1))
    }

    private fun calculateRmssd(data: List<Float>): Double {
        if (data.size < 2) return 0.0
        val diffs = data.zipWithNext { a, b -> (b - a).toDouble() }
        val squaredDiffs = diffs.sumOf { it * it }
        return sqrt(squaredDiffs / diffs.size)
    }
}