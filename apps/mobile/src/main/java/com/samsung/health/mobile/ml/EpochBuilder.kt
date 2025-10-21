package com.samsung.health.mobile.ml

import android.util.Log
import kotlin.math.sqrt

private const val TAG = "EpochBuilder"

/**
 * Represents a 30-second epoch with aggregated features
 * Matches the training data structure
 */
data class Epoch(
    val epochId: Int,
    val timestamp: Long,

    // Heart rate features (from 6 samples over 30 seconds)
    val hrMean: Float,
    val hrStd: Float,
    val hrMin: Float,
    val hrMax: Float,
    val hrRmssd: Float,

    // Motion features (from 6 samples over 30 seconds)
    val motionXStd: Float,
    val motionXRange: Float,
    val motionYStd: Float,
    val motionYRange: Float,
    val motionZStd: Float,
    val motionZRange: Float
)

/**
 * Builds 30-second epochs from 5-second samples
 * Matches Python training pipeline: create_30s_epochs()
 */
class EpochBuilder {

    private var epochCounter = 0

    /**
     * Create a 30-second epoch from 6 consecutive 5-second samples
     *
     * @param samples List of exactly 6 BufferedDataPoint samples
     * @return Epoch with aggregated features
     */
    fun createEpoch(samples: List<BufferedDataPoint>): Epoch? {
        if (samples.size != 6) {
            Log.w(TAG, "⚠️ Need exactly 6 samples for 30-second epoch, got ${samples.size}")
            return null
        }

        // Extract values
        val hrValues = samples.map { it.hr }
        val accelXValues = samples.map { it.accelX }
        val accelYValues = samples.map { it.accelY }
        val accelZValues = samples.map { it.accelZ }

        // Calculate heart rate features
        val hrMean = calculateMean(hrValues)
        val hrStd = calculateStd(hrValues)
        val hrMin = hrValues.minOrNull() ?: 0f
        val hrMax = hrValues.maxOrNull() ?: 0f
        val hrRmssd = calculateRmssd(hrValues)

        // Calculate motion features
        val motionXStd = calculateStd(accelXValues)
        val motionXRange = calculateRange(accelXValues)
        val motionYStd = calculateStd(accelYValues)
        val motionYRange = calculateRange(accelYValues)
        val motionZStd = calculateStd(accelZValues)
        val motionZRange = calculateRange(accelZValues)

        val epoch = Epoch(
            epochId = epochCounter++,
            timestamp = samples.last().timestamp,
            hrMean = hrMean,
            hrStd = hrStd,
            hrMin = hrMin,
            hrMax = hrMax,
            hrRmssd = hrRmssd,
            motionXStd = motionXStd,
            motionXRange = motionXRange,
            motionYStd = motionYStd,
            motionYRange = motionYRange,
            motionZStd = motionZStd,
            motionZRange = motionZRange
        )

        Log.d(TAG, "✅ Created epoch ${epoch.epochId}: HR=${hrMean.toInt()} (${hrMin.toInt()}-${hrMax.toInt()})")

        return epoch
    }

    /**
     * Extract all possible 30-second epochs from buffer
     * Creates overlapping epochs for smoother predictions
     */
    fun extractEpochs(buffer: List<BufferedDataPoint>, overlap: Boolean = true): List<Epoch> {
        if (buffer.size < 6) {
            return emptyList()
        }

        val epochs = mutableListOf<Epoch>()
        val step = if (overlap) 3 else 6 // 15-second or 30-second steps

        var i = 0
        while (i + 6 <= buffer.size) {
            val samples = buffer.subList(i, i + 6)
            val epoch = createEpoch(samples)
            if (epoch != null) {
                epochs.add(epoch)
            }
            i += step
        }

        return epochs
    }

    /**
     * Get the most recent complete epoch from buffer
     */
    fun getLatestEpoch(buffer: List<BufferedDataPoint>): Epoch? {
        if (buffer.size < 6) return null

        // Take the last 6 samples
        val lastSixSamples = buffer.takeLast(6)
        return createEpoch(lastSixSamples)
    }

    /**
     * Reset epoch counter (when starting new session)
     */
    fun reset() {
        epochCounter = 0
        Log.i(TAG, "Epoch builder reset")
    }

    // ========== Statistical Helper Functions ==========

    private fun calculateMean(values: List<Float>): Float {
        return if (values.isEmpty()) 0f else values.average().toFloat()
    }

    private fun calculateStd(values: List<Float>): Float {
        if (values.size < 2) return 0f
        val mean = values.average()
        val variance = values.map { (it - mean) * (it - mean) }.average()
        return sqrt(variance).toFloat()
    }

    private fun calculateRange(values: List<Float>): Float {
        if (values.isEmpty()) return 0f
        return (values.maxOrNull() ?: 0f) - (values.minOrNull() ?: 0f)
    }

    /**
     * Root Mean Square of Successive Differences
     * Matches Python: calculate_rmssd()
     */
    private fun calculateRmssd(values: List<Float>): Float {
        if (values.size < 2) return 0f

        // Calculate successive differences
        val diffs = values.zipWithNext { a, b -> (b - a) * (b - a) }

        // RMSSD = sqrt(mean(diffs^2))
        return sqrt(diffs.average()).toFloat()
    }
}