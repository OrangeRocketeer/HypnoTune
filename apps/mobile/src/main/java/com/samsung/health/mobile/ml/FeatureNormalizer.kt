package com.samsung.health.mobile.ml

import android.util.Log

private const val TAG = "FeatureNormalizer"

/**
 * Normalizes features using training statistics
 * Stats from lightgbm_live_model3_stats.json
 */
class FeatureNormalizer {

    // Feature means from training data
    private val featureMeans = floatArrayOf(
        65.12563f, 1.513927f, 63.43720f, 67.00959f, 1.489238f,
        0.011297f, 0.028338f, 0.017323f, 0.043144f, 0.015890f,
        0.038762f, 65.04655f, 1.512443f, 0.011227f, 0.017279f,
        0.015852f, 64.96801f, 1.510485f, 0.011221f, 0.017274f,
        0.015848f, 64.88921f, 1.508884f, 0.011204f, 0.017229f,
        0.015840f, 64.80930f, 1.506395f, 0.011190f, 0.017184f,
        0.015823f, 64.32950f, 2.212068f, 1.493387f, 1.220352f,
        0.501279f
    )

    // Feature standard deviations from training data
    private val featureStds = floatArrayOf(
        10.123146f, 1.914362f, 9.763583f, 10.921787f, 1.840885f,
        0.051693f, 0.128271f, 0.072134f, 0.178139f, 0.076200f,
        0.180370f, 10.361588f, 1.914734f, 0.051368f, 0.072078f,
        0.076114f, 10.594675f, 1.914778f, 0.051366f, 0.072079f,
        0.076115f, 10.822111f, 1.915026f, 0.051327f, 0.071867f,
        0.076111f, 11.044294f, 1.913166f, 0.051320f, 0.071798f,
        0.076098f, 11.871069f, 2.192346f, 0.939436f, 1.131393f,
        3.435120f
    )

    // Feature names for debugging
    private val featureNames = arrayOf(
        "hr_mean", "hr_std", "hr_min", "hr_max", "hr_rmssd",
        "motion_x_std", "motion_x_range", "motion_y_std", "motion_y_range",
        "motion_z_std", "motion_z_range",
        "hr_mean_lag_1", "hr_std_lag_1", "motion_x_std_lag_1",
        "motion_y_std_lag_1", "motion_z_std_lag_1",
        "hr_mean_lag_2", "hr_std_lag_2", "motion_x_std_lag_2",
        "motion_y_std_lag_2", "motion_z_std_lag_2",
        "hr_mean_lag_3", "hr_std_lag_3", "motion_x_std_lag_3",
        "motion_y_std_lag_3", "motion_z_std_lag_3",
        "hr_mean_lag_4", "hr_std_lag_4", "motion_x_std_lag_4",
        "motion_y_std_lag_4", "motion_z_std_lag_4",
        "hr_mean_rolling_mean_25min", "hr_mean_rolling_std_25min",
        "hr_std_rolling_mean_25min", "hr_std_rolling_std_25min",
        "time_since_sleep_onset"
    )

    /**
     * Normalize features using z-score normalization
     * Formula: (x - mean) / std
     */
    fun normalize(features: FloatArray): FloatArray {
        if (features.size != 36) {
            Log.e(TAG, "❌ Expected 36 features, got ${features.size}")
            throw IllegalArgumentException("Expected 36 features, got ${features.size}")
        }

        val normalized = FloatArray(36)

        for (i in features.indices) {
            // Z-score normalization with epsilon for numerical stability
            val std = if (featureStds[i] < 1e-6f) 1e-6f else featureStds[i]
            normalized[i] = (features[i] - featureMeans[i]) / std

            // Check for NaN or Inf
            if (normalized[i].isNaN() || normalized[i].isInfinite()) {
                Log.w(TAG, "⚠️ Invalid normalized value for ${featureNames[i]}: ${normalized[i]} (raw: ${features[i]})")
                normalized[i] = 0f
            }
        }

        Log.d(TAG, "✅ Features normalized")
        return normalized
    }

    /**
     * Log feature values for debugging
     */
    fun logFeatures(features: FloatArray, prefix: String = "Features") {
        if (features.size != 36) {
            Log.w(TAG, "$prefix: Invalid size ${features.size}")
            return
        }

        Log.d(TAG, "=== $prefix ===")
        for (i in features.indices) {
            Log.d(TAG, "${featureNames[i]}: ${"%.4f".format(features[i])}")
        }
        Log.d(TAG, "===============")
    }
}