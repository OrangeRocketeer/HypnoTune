package com.samsung.health.mobile.ml

import android.content.Context
import android.util.Log
import ai.onnxruntime.*
import com.samsung.health.data.TrackedData
import java.nio.FloatBuffer
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "MLModelInterface"

/**
 * ONNX Model Interface for Sleep Stage Prediction
 * Complete robust pipeline with buffering, interpolation, and temporal features
 */
@Singleton
class MLModelInterface @Inject constructor() {

    private var ortSession: OrtSession? = null
    private var ortEnvironment: OrtEnvironment? = null
    private var isModelLoaded: Boolean = false

    // === New Robust Pipeline Components ===
    private val dataBuffer = DataBuffer()
    private val epochBuilder = EpochBuilder()
    private val temporalCalculator = TemporalFeatureCalculator()
    private val featureNormalizer = FeatureNormalizer()

    // === Prediction Management ===
    private var lastPredictionTime: Long = 0L
    private val predictionIntervalMs = 15000L // Predict every 15 seconds

    private var lastPredictedStage: Int = 0
    private var lastConfidence: Float = 0f

    // === Configuration ===
    private val CONFIDENCE_THRESHOLD = 0.5f
    private val STAGE_MAPPING = mapOf(
        0 to 0,  // Wake → Rest/Calm music
        1 to 1,  // Light → Light Activity music
        2 to 2,  // Deep → Moderate Activity music
        3 to 3   // REM → High Activity music
    )

    /**
     * Load the ONNX model from assets
     */
    fun loadModel(context: Context) {
        try {
            Log.i(TAG, "========================================")
            Log.i(TAG, "🔄 Loading ONNX model...")
            Log.i(TAG, "========================================")

            // Check if model exists
            val assetsList = context.assets.list("")
            val modelExists = assetsList?.contains("lightgbm_live_model3.onnx") ?: false

            if (!modelExists) {
                Log.e(TAG, "❌ Model file not found in assets!")
                isModelLoaded = false
                return
            }

            // Create ONNX environment
            ortEnvironment = OrtEnvironment.getEnvironment()

            // Read model bytes
            val modelBytes = context.assets.open("lightgbm_live_model3.onnx").use {
                it.readBytes()
            }

            // Create session
            val sessionOptions = OrtSession.SessionOptions()
            sessionOptions.addCPU(true)
            ortSession = ortEnvironment?.createSession(modelBytes, sessionOptions)

            isModelLoaded = true

            Log.i(TAG, "========================================")
            Log.i(TAG, "✅ MODEL LOADED SUCCESSFULLY")
            Log.i(TAG, "========================================")

            logModelInfo()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to load model", e)
            ortSession = null
            isModelLoaded = false
        }
    }

    /**
     * Process incoming sensor data
     * Main entry point - called every time watch sends data
     */
    fun processData(data: TrackedData) {
        // Add to buffer (with automatic interpolation)
        dataBuffer.addDataPoint(data)

        Log.d(TAG, "📥 Data received: HR=${data.hr}, Buffer=${dataBuffer.size()} samples")
    }

    /**
     * Predict sleep stage - called every 15 seconds
     *
     * Returns:
     * - Current predicted stage (0-3 mapped to 0-4 music stages)
     * - Returns previous stage if not enough data or low confidence
     */
    fun predictStage(): Int {
        // Edge case 1: Model not loaded
        if (!isModelLoaded || ortSession == null) {
            Log.w(TAG, "⚠️ Model not loaded, returning stage 0")
            return 0
        }

        // Edge case 2: Not enough time elapsed since last prediction
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastPredictionTime < predictionIntervalMs) {
            Log.d(TAG, "⏳ Waiting for prediction interval, returning last stage: $lastPredictedStage")
            return lastPredictedStage
        }

        // Edge case 3: Data is stale (watch disconnected)
        if (dataBuffer.isDataStale()) {
            val timeSinceData = dataBuffer.getTimeSinceLastRealData() / 1000
            Log.w(TAG, "⚠️ Data stale (${timeSinceData}s), returning last stage: $lastPredictedStage")
            return lastPredictedStage
        }

        // Edge case 4: Not enough data for even one epoch
        if (!dataBuffer.hasMinimumData()) {
            Log.w(TAG, "⚠️ Insufficient data (${dataBuffer.size()}/6 samples), returning stage 0")
            return 0
        }

        try {
            // Step 1: Create 30-second epoch from last 6 samples
            val allData = dataBuffer.getAllData()
            val latestEpoch = epochBuilder.getLatestEpoch(allData)

            if (latestEpoch == null) {
                Log.w(TAG, "⚠️ Could not create epoch, returning last stage: $lastPredictedStage")
                return lastPredictedStage
            }

            // Step 2: Add epoch to temporal calculator
            temporalCalculator.addEpoch(latestEpoch)

            // Edge case 5: Not enough epochs for temporal features
            if (!temporalCalculator.hasSufficientData()) {
                Log.w(TAG, "⚠️ Need 5 epochs for temporal features, have ${temporalCalculator.getHistorySize()}")
                return 0
            }

            // Step 3: Calculate all 36 features with temporal context
            val rawFeatures = temporalCalculator.calculateFeatures()

            if (rawFeatures == null) {
                Log.w(TAG, "⚠️ Feature calculation failed, returning stage 0")
                return 0
            }

            // Step 4: Normalize features using training statistics
            val normalizedFeatures = featureNormalizer.normalize(rawFeatures)

            // Step 5: Run inference
            val (predictedSleepStage, confidence) = runInference(normalizedFeatures)

            // Step 6: Map sleep stage to music stage
            val musicStage = STAGE_MAPPING[predictedSleepStage] ?: 0

            val stageName = when (predictedSleepStage) {
                0 -> "Wake"
                1 -> "Light"
                2 -> "Deep"
                3 -> "REM"
                else -> "Unknown"
            }

            // Log interpolation stats
            val interpStats = dataBuffer.getInterpolationStats()
            Log.d(TAG, "📊 Interpolation: ${interpStats.getPercentageInterpolated()}% (${interpStats.interpolatedSamples}/${interpStats.totalSamples})")

            Log.i(TAG, "🤖 ML Prediction: $stageName (sleep stage: $predictedSleepStage) → Music Stage $musicStage")
            Log.i(TAG, "   Confidence: ${"%.1f".format(confidence * 100)}% | HR=${latestEpoch.hrMean.toInt()} bpm")

            // Edge case 6: Low confidence - use previous stage
            if (confidence < CONFIDENCE_THRESHOLD) {
                Log.w(TAG, "⚠️ Low confidence (${"%.1f".format(confidence * 100)}% < ${"%.0f".format(CONFIDENCE_THRESHOLD * 100)}%), keeping stage: $lastPredictedStage")
                lastPredictionTime = currentTime
                return lastPredictedStage
            }

            // Update tracking
            lastPredictedStage = musicStage
            lastConfidence = confidence
            lastPredictionTime = currentTime

            Log.i(TAG, "✅ Stage updated: $musicStage ($stageName)")

            return musicStage

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during prediction", e)
            return lastPredictedStage
        }
    }

    /**
     * Backward compatible method - processes data and predicts
     */
    fun predictStage(data: TrackedData): Int {
        processData(data)
        return predictStage()
    }

    /**
     * Run ONNX model inference
     */
    private fun runInference(features: FloatArray): Pair<Int, Float> {
        val session = ortSession ?: throw IllegalStateException("Model not loaded")

        // Create input tensor [1, 36]
        val inputName = session.inputNames.iterator().next()
        val shape = longArrayOf(1, features.size.toLong())
        val inputTensor = OnnxTensor.createTensor(
            ortEnvironment,
            FloatBuffer.wrap(features),
            shape
        )

        // Run inference
        val results = session.run(mapOf(inputName to inputTensor))

        try {
            // LightGBM models can output in different formats
            // Try format 1: Array<FloatArray> (probabilities)
            val outputValue = results[0].value

            val probabilities = when (outputValue) {
                is Array<*> -> {
                    // Format: [[prob0, prob1, prob2, prob3]]
                    val firstElement = outputValue[0]
                    when (firstElement) {
                        is FloatArray -> firstElement
                        is DoubleArray -> firstElement.map { it.toFloat() }.toFloatArray()
                        else -> {
                            Log.e(TAG, "Unexpected array element type: ${firstElement?.javaClass?.name}")
                            floatArrayOf(1f, 0f, 0f, 0f) // Default to Wake
                        }
                    }
                }
                is LongArray -> {
                    // Format: [predicted_class] - LightGBM sometimes returns class directly
                    val predictedClass = outputValue[0].toInt()
                    Log.d(TAG, "Model returned class directly: $predictedClass")

                    // Convert class to probability array (high confidence for predicted class)
                    FloatArray(4) { i -> if (i == predictedClass) 0.95f else 0.05f / 3f }
                }
                is FloatArray -> {
                    // Format: [prob0, prob1, prob2, prob3] - already flat
                    outputValue
                }
                else -> {
                    Log.e(TAG, "Unexpected output format: ${outputValue?.javaClass?.name}")
                    floatArrayOf(1f, 0f, 0f, 0f) // Default to Wake
                }
            }

            // Find class with highest probability
            val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
            val confidence = probabilities[maxIndex]

            Log.d(TAG, "Model output probabilities: [Wake: ${"%.3f".format(probabilities.getOrNull(0) ?: 0f)}, " +
                    "Light: ${"%.3f".format(probabilities.getOrNull(1) ?: 0f)}, " +
                    "Deep: ${"%.3f".format(probabilities.getOrNull(2) ?: 0f)}, " +
                    "REM: ${"%.3f".format(probabilities.getOrNull(3) ?: 0f)}]")

            // Clean up
            inputTensor.close()
            results.close()

            return Pair(maxIndex, confidence)

        } catch (e: Exception) {
            Log.e(TAG, "Error processing model output", e)
            inputTensor.close()
            results.close()
            throw e
        }
    }

    /**
     * Get current prediction confidence
     */
    fun getLastConfidence(): Float = lastConfidence

    /**
     * Get last predicted stage
     */
    fun getLastPredictedStage(): Int = lastPredictedStage

    /**
     * Check if model is ready
     */
    fun isModelReady(): Boolean = isModelLoaded

    /**
     * Get buffer statistics
     */
    fun getBufferSize(): Int = dataBuffer.size()

    fun getInterpolationStats(): InterpolationStats = dataBuffer.getInterpolationStats()

    fun getEpochHistorySize(): Int = temporalCalculator.getHistorySize()

    /**
     * Reset everything (when starting new session)
     */
    fun resetTracking() {
        dataBuffer.clear()
        epochBuilder.reset()
        temporalCalculator.reset()
        lastPredictedStage = 0
        lastConfidence = 0f
        lastPredictionTime = 0L
        Log.i(TAG, "🔄 All tracking reset")
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        try {
            ortSession?.close()
            ortEnvironment?.close()
            isModelLoaded = false
            Log.i(TAG, "Model resources cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up", e)
        }
    }

    /**
     * Log model information
     */
    private fun logModelInfo() {
        try {
            val session = ortSession ?: return
            Log.d(TAG, "=== Model Info ===")
            Log.d(TAG, "Inputs: ${session.inputNames}")
            Log.d(TAG, "Outputs: ${session.outputNames}")
            session.inputInfo.forEach { (name, info) ->
                Log.d(TAG, "Input '$name': ${info.info}")
            }
            Log.d(TAG, "==================")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging model info", e)
        }
    }
}