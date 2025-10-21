package com.samsung.health.mobile.ml

import android.util.Log
import java.util.LinkedList
import kotlin.math.sqrt

private const val TAG = "TemporalFeatureCalc"

/**
 * Calculates temporal features (lag and rolling window) for ML model
 * Matches Python training: add_temporal_features() and add_time_since_sleep_onset()
 */
class TemporalFeatureCalculator {

    // Store last 50 epochs (25 minutes) for rolling window features
    private val epochHistory = LinkedList<Epoch>()
    private val maxHistorySize = 50

    // Track sleep onset
    private var sleepOnsetEpochId: Int? = null
    private var sessionStartTime: Long = 0L

    /**
     * Add epoch to history buffer
     */
    fun addEpoch(epoch: Epoch) {
        epochHistory.addLast(epoch)

        // Initialize sleep onset time on first epoch
        if (sessionStartTime == 0L) {
            sessionStartTime = epoch.timestamp
        }

        // Maintain buffer size
        while (epochHistory.size > maxHistorySize) {
            epochHistory.removeFirst()
        }

        Log.d(TAG, "Epoch added to history. Total epochs: ${epochHistory.size}")
    }

    /**
     * Calculate all 36 features for the latest epoch
     * Returns null if insufficient history (need at least 5 epochs for lag features)
     */
    fun calculateFeatures(): FloatArray? {
        if (epochHistory.size < 5) {
            Log.w(TAG, "⚠️ Need at least 5 epochs for temporal features, have ${epochHistory.size}")
            return null
        }

        val features = FloatArray(36)
        val currentEpoch = epochHistory.last()

        // === Base features (0-10) ===
        features[0] = currentEpoch.hrMean
        features[1] = currentEpoch.hrStd
        features[2] = currentEpoch.hrMin
        features[3] = currentEpoch.hrMax
        features[4] = currentEpoch.hrRmssd
        features[5] = currentEpoch.motionXStd
        features[6] = currentEpoch.motionXRange
        features[7] = currentEpoch.motionYStd
        features[8] = currentEpoch.motionYRange
        features[9] = currentEpoch.motionZStd
        features[10] = currentEpoch.motionZRange

        // === Lag features (11-30) ===
        // Lag 1 (30 seconds ago) - indices 11-15
        val lag1 = getLaggedEpoch(1)
        features[11] = lag1?.hrMean ?: currentEpoch.hrMean
        features[12] = lag1?.hrStd ?: currentEpoch.hrStd
        features[13] = lag1?.motionXStd ?: currentEpoch.motionXStd
        features[14] = lag1?.motionYStd ?: currentEpoch.motionYStd
        features[15] = lag1?.motionZStd ?: currentEpoch.motionZStd

        // Lag 2 (60 seconds ago) - indices 16-20
        val lag2 = getLaggedEpoch(2)
        features[16] = lag2?.hrMean ?: features[11]
        features[17] = lag2?.hrStd ?: features[12]
        features[18] = lag2?.motionXStd ?: features[13]
        features[19] = lag2?.motionYStd ?: features[14]
        features[20] = lag2?.motionZStd ?: features[15]

        // Lag 3 (90 seconds ago) - indices 21-25
        val lag3 = getLaggedEpoch(3)
        features[21] = lag3?.hrMean ?: features[16]
        features[22] = lag3?.hrStd ?: features[17]
        features[23] = lag3?.motionXStd ?: features[18]
        features[24] = lag3?.motionYStd ?: features[19]
        features[25] = lag3?.motionZStd ?: features[20]

        // Lag 4 (120 seconds ago) - indices 26-30
        val lag4 = getLaggedEpoch(4)
        features[26] = lag4?.hrMean ?: features[21]
        features[27] = lag4?.hrStd ?: features[22]
        features[28] = lag4?.motionXStd ?: features[23]
        features[29] = lag4?.motionYStd ?: features[24]
        features[30] = lag4?.motionZStd ?: features[25]

        // === Rolling window features (31-34) ===
        // Use all available epochs (up to 50 = 25 minutes)
        val allEpochs = epochHistory.toList()

        // hr_mean_rolling_mean_25min
        val hrMeans = allEpochs.map { it.hrMean }
        features[31] = calculateMean(hrMeans)

        // hr_mean_rolling_std_25min
        features[32] = calculateStd(hrMeans)

        // hr_std_rolling_mean_25min (mean of hr_std over time)
        val hrStds = allEpochs.map { it.hrStd }
        features[33] = calculateMean(hrStds)

        // hr_std_rolling_std_25min (std of hr_std over time)
        features[34] = calculateStd(hrStds)

        // === Time since sleep onset (35) ===
        features[35] = calculateTimeSinceSleepOnset()

        Log.d(TAG, "✅ Calculated 36 features (${epochHistory.size} epochs in history)")

        return features
    }

    /**
     * Get lagged epoch (1 = 30 seconds ago, 2 = 60 seconds ago, etc.)
     */
    private fun getLaggedEpoch(lag: Int): Epoch? {
        val index = epochHistory.size - 1 - lag
        return if (index >= 0) epochHistory[index] else null
    }

    /**
     * Calculate time since sleep onset in hours
     * Sleep onset = first epoch after session start
     */
    private fun calculateTimeSinceSleepOnset(): Float {
        if (sessionStartTime == 0L) return 0f

        val currentTime = epochHistory.last().timestamp
        val elapsedMs = currentTime - sessionStartTime

        // Convert to hours
        return (elapsedMs / 1000f / 3600f)
    }

    /**
     * Mark sleep onset (called when first non-wake stage detected)
     */
    fun markSleepOnset(epochId: Int) {
        if (sleepOnsetEpochId == null) {
            sleepOnsetEpochId = epochId
            Log.i(TAG, "🛌 Sleep onset marked at epoch $epochId")
        }
    }

    /**
     * Check if we have sufficient data for temporal features
     */
    fun hasSufficientData(): Boolean {
        return epochHistory.size >= 5
    }

    /**
     * Get history size
     */
    fun getHistorySize(): Int = epochHistory.size

    /**
     * Reset calculator (when starting new session)
     */
    fun reset() {
        epochHistory.clear()
        sleepOnsetEpochId = null
        sessionStartTime = 0L
        Log.i(TAG, "Temporal feature calculator reset")
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
}