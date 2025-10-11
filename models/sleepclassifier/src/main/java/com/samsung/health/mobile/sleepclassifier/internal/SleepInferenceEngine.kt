package com.yourapp.sleepclassifier.internal

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.ArrayDeque

/**
 * The core engine that manages the live classification pipeline.
 */
internal class SleepInferenceEngine(context: Context) {

    private val normalizer = FeatureNormalizer(context, "lightgbm_live_model3_stats.json")
    private val modelHandler = OnnxModelHandler(context, "lightgbm_live_model3.onnx")

    // Buffer for incoming 5-second data points
    private val dataBuffer = mutableListOf<WatchDataPoint>()

    // Buffer for the history of *processed* epochs for temporal features
    private val processedHistory = ArrayDeque<ProcessedEpoch>(50) // Keep last 50 epochs

    private val _predictionFlow = MutableStateFlow<PredictionResult>(PredictionResult.InsufficientData)
    val predictionFlow: StateFlow<PredictionResult> = _predictionFlow

    /**
     * This is the main entry point for new data.
     * It buffers data and triggers a prediction every 30 seconds.
     */
    fun processNewSample(dataPoint: WatchDataPoint) {
        dataBuffer.add(dataPoint)

        if (dataBuffer.size == 6) {
            // We have a full 30-second window
            val window = dataBuffer.toList() // Create a copy for processing
            dataBuffer.clear() // Clear buffer for the next window

            // --- The Full Pipeline ---
            // 1. Calculate all features using the window and history
            val processedEpoch = FeatureProcessor.process30SecondWindow(window, processedHistory)

            // 2. Normalize the features into a FloatArray
            val normalizedFeatures = normalizer.normalize(processedEpoch.features)

            // 3. Run inference with the ONNX model
            val (predictedLabel, confidenceMap) = modelHandler.predict(normalizedFeatures)

            // 4. Update the history buffer with the *newly processed epoch*
            // The predicted stage is used for the *next* epoch's calculation.
            val resultStage = Stage.fromInt(predictedLabel)
            processedHistory.add(ProcessedEpoch(processedEpoch.features, resultStage))
            if (processedHistory.size > 50) {
                processedHistory.removeFirst()
            }
            
            // 5. Emit the result
            val confidence = confidenceMap[predictedLabel] ?: 0f
            _predictionFlow.value = PredictionResult.SleepStage(resultStage, confidence)
        }
    }

    fun close() {
        modelHandler.close()
    }
}