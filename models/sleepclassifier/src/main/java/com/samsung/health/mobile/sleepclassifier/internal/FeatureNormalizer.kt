package com.yourapp.sleepclassifier.internal

import android.content.Context
import kotlinx.serialization.json.Json

/**
 * Loads stats from JSON and normalizes feature maps into FloatArrays for the model.
 */
internal class FeatureNormalizer(context: Context, statsFileName: String) {

    private val stats: NormalizationStats

    init {
        val jsonString = context.assets.open(statsFileName).bufferedReader().use { it.readText() }
        stats = Json.decodeFromString(NormalizationStats.serializer(), jsonString)
    }

    /**
     * Takes a map of features, orders them correctly, normalizes them,
     * and returns a FloatArray ready for the ONNX model.
     */
    fun normalize(features: Map<String, Float>): FloatArray {
        val floatArray = FloatArray(stats.features.size)

        for (i in stats.features.indices) {
            val featureName = stats.features[i]
            val value = features[featureName] ?: 0f // Default to 0 if a feature is missing
            val mean = stats.mean[featureName] ?: 0.0
            val std = stats.std[featureName] ?: 1.0

            // Apply normalization: (value - mean) / (std + epsilon)
            floatArray[i] = ((value - mean) / (std + 1e-6)).toFloat()
        }
        return floatArray
    }
}