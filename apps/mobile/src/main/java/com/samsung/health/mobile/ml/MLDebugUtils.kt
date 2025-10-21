package com.samsung.health.mobile.ml

import android.util.Log
import com.samsung.health.data.TrackedData
import kotlin.random.Random

private const val TAG = "MLDebugUtils"

/**
 * Debugging and testing utilities for ML pipeline
 * Use these during development to verify the pipeline works correctly
 */
object MLDebugUtils {

    /**
     * Generate synthetic test data for debugging
     * Simulates realistic heart rate and accelerometer data
     */
    fun generateSyntheticData(
        count: Int = 60,
        intervalMs: Long = 5000,
        sleepStage: SyntheticSleepStage = SyntheticSleepStage.LIGHT
    ): List<Pair<TrackedData, Long>> {
        val dataPoints = mutableListOf<Pair<TrackedData, Long>>()
        val startTime = System.currentTimeMillis()

        val (baseHR, hrVariability, motionLevel) = when (sleepStage) {
            SyntheticSleepStage.AWAKE -> Triple(75f, 8f, 0.05f)
            SyntheticSleepStage.LIGHT -> Triple(65f, 5f, 0.02f)
            SyntheticSleepStage.DEEP -> Triple(55f, 3f, 0.01f)
            SyntheticSleepStage.REM -> Triple(70f, 6f, 0.03f)
        }

        repeat(count) { i ->
            val hr = (baseHR + Random.nextFloat() * hrVariability).toInt()
            val accelX = Random.nextFloat() * motionLevel - motionLevel / 2
            val accelY = Random.nextFloat() * motionLevel - motionLevel / 2
            val accelZ = Random.nextFloat() * motionLevel - motionLevel / 2

            val data = TrackedData(
                hr = hr,
                ibi = ArrayList(), // Empty ArrayList for IBI
                accelX = accelX,
                accelY = accelY,
                accelZ = accelZ
            )

            val timestamp = startTime + (i * intervalMs)
            dataPoints.add(Pair(data, timestamp))
        }

        Log.d(TAG, "Generated ${dataPoints.size} synthetic data points for ${sleepStage.name}")
        return dataPoints
    }

    /**
     * Test the complete ML pipeline with synthetic data
     */
    fun testPipeline(mlModel: MLModelInterface): PipelineTestResult {
        Log.i(TAG, "========================================")
        Log.i(TAG, "🧪 Starting Pipeline Test")
        Log.i(TAG, "========================================")

        val startTime = System.currentTimeMillis()

        // Reset model
        mlModel.resetTracking()

        // Generate 2 minutes of data (24 samples = 4 epochs)
        val testData = generateSyntheticData(
            count = 24,
            intervalMs = 5000,
            sleepStage = SyntheticSleepStage.LIGHT
        )

        // Feed data to model
        Log.d(TAG, "Feeding ${testData.size} samples to pipeline...")
        testData.forEach { (data, _) ->
            mlModel.processData(data)
        }

        // Wait for sufficient epochs
        Thread.sleep(1000)

        // Try prediction
        val prediction = try {
            mlModel.predictStage()
        } catch (e: Exception) {
            Log.e(TAG, "Prediction failed", e)
            -1
        }

        val confidence = mlModel.getLastConfidence()
        val bufferSize = mlModel.getBufferSize()
        val epochHistory = mlModel.getEpochHistorySize()
        val interpStats = mlModel.getInterpolationStats()

        val duration = System.currentTimeMillis() - startTime

        val result = PipelineTestResult(
            success = prediction >= 0,
            predictedStage = prediction,
            confidence = confidence,
            bufferSize = bufferSize,
            epochHistory = epochHistory,
            interpolationRate = interpStats.interpolationRate,
            durationMs = duration
        )

        Log.i(TAG, "========================================")
        Log.i(TAG, "🧪 Pipeline Test Complete")
        Log.i(TAG, "Success: ${result.success}")
        Log.i(TAG, "Predicted Stage: ${result.predictedStage}")
        Log.i(TAG, "Confidence: ${"%.1f".format(result.confidence * 100)}%")
        Log.i(TAG, "Buffer: ${result.bufferSize} samples")
        Log.i(TAG, "Epochs: ${result.epochHistory}")
        Log.i(TAG, "Duration: ${result.durationMs}ms")
        Log.i(TAG, "========================================")

        return result
    }

    /**
     * Test data interpolation by simulating missing data
     */
    fun testInterpolation(mlModel: MLModelInterface): InterpolationTestResult {
        Log.i(TAG, "========================================")
        Log.i(TAG, "🧪 Testing Interpolation")
        Log.i(TAG, "========================================")

        mlModel.resetTracking()

        val baseData = TrackedData(
            hr = 65,
            ibi = ArrayList(), // Empty ArrayList
            accelX = 0.01f,
            accelY = 0.01f,
            accelZ = 0.01f
        )

        // Send first data point
        mlModel.processData(baseData)
        Thread.sleep(5000) // Wait 5 seconds

        // Send second data point (should interpolate gap)
        mlModel.processData(baseData.copy(hr = 70))
        Thread.sleep(15000) // Wait 15 seconds (should interpolate 2 points)

        // Send third data point
        mlModel.processData(baseData.copy(hr = 68))
        Thread.sleep(35000) // Wait 35 seconds (should give up on interpolation)

        // Send fourth data point
        mlModel.processData(baseData.copy(hr = 66))

        val stats = mlModel.getInterpolationStats()

        val result = InterpolationTestResult(
            totalSamples = stats.totalSamples,
            interpolatedSamples = stats.interpolatedSamples,
            interpolationRate = stats.interpolationRate
        )

        Log.i(TAG, "========================================")
        Log.i(TAG, "Total samples: ${result.totalSamples}")
        Log.i(TAG, "Interpolated: ${result.interpolatedSamples}")
        Log.i(TAG, "Rate: ${"%.1f".format(result.interpolationRate * 100)}%")
        Log.i(TAG, "========================================")

        return result
    }

    /**
     * Verify feature calculation matches expected dimensions
     */
    fun verifyFeatureDimensions(mlModel: MLModelInterface): Boolean {
        Log.i(TAG, "🔍 Verifying feature dimensions...")

        mlModel.resetTracking()

        // Generate enough data for one prediction
        val testData = generateSyntheticData(count = 30) // 5 epochs
        testData.forEach { (data, _) ->
            mlModel.processData(data)
        }

        try {
            val prediction = mlModel.predictStage()
            Log.i(TAG, "✅ Feature dimensions verified (prediction: $prediction)")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Feature dimension mismatch", e)
            return false
        }
    }

    /**
     * Simulate a complete sleep session
     */
    fun simulateSleepSession(mlModel: MLModelInterface, durationMinutes: Int = 5) {
        Log.i(TAG, "========================================")
        Log.i(TAG, "🌙 Simulating ${durationMinutes}min sleep session")
        Log.i(TAG, "========================================")

        mlModel.resetTracking()

        val samplesPerMinute = 12 // 5-second intervals
        val totalSamples = durationMinutes * samplesPerMinute

        // Simulate sleep stages changing over time
        val stages = listOf(
            SyntheticSleepStage.AWAKE,
            SyntheticSleepStage.LIGHT,
            SyntheticSleepStage.LIGHT,
            SyntheticSleepStage.DEEP,
            SyntheticSleepStage.REM
        )

        var samplesProcessed = 0
        stages.forEach { stage ->
            val samplesForThisStage = totalSamples / stages.size
            val data = generateSyntheticData(
                count = samplesForThisStage,
                sleepStage = stage
            )

            data.forEach { (trackedData, _) ->
                mlModel.processData(trackedData)
                samplesProcessed++

                // Try prediction every 15 seconds (3 samples)
                if (samplesProcessed % 3 == 0) {
                    val prediction = mlModel.predictStage()
                    val confidence = mlModel.getLastConfidence()

                    if (prediction >= 0) {
                        Log.d(TAG, "Sample $samplesProcessed: Stage=$prediction, Confidence=${"%.1f".format(confidence*100)}%")
                    }
                }
            }
        }

        Log.i(TAG, "========================================")
        Log.i(TAG, "✅ Sleep session simulation complete")
        Log.i(TAG, "========================================")
    }

    /**
     * Benchmark prediction latency
     */
    fun benchmarkLatency(mlModel: MLModelInterface, iterations: Int = 100): LatencyBenchmark {
        Log.i(TAG, "⏱️ Benchmarking prediction latency...")

        mlModel.resetTracking()

        // Prepare data
        val testData = generateSyntheticData(count = 60) // 10 epochs
        testData.forEach { (data, _) ->
            mlModel.processData(data)
        }

        // Warmup
        repeat(10) { mlModel.predictStage() }

        // Benchmark
        val latencies = mutableListOf<Long>()
        repeat(iterations) {
            val start = System.nanoTime()
            mlModel.predictStage()
            val end = System.nanoTime()
            latencies.add((end - start) / 1_000_000) // Convert to ms
        }

        val result = LatencyBenchmark(
            iterations = iterations,
            avgLatencyMs = latencies.average(),
            minLatencyMs = latencies.minOrNull()?.toDouble() ?: 0.0,
            maxLatencyMs = latencies.maxOrNull()?.toDouble() ?: 0.0,
            p50LatencyMs = latencies.sorted()[iterations / 2].toDouble(),
            p95LatencyMs = latencies.sorted()[(iterations * 95) / 100].toDouble()
        )

        Log.i(TAG, "Avg: ${"%.2f".format(result.avgLatencyMs)}ms, " +
                "Min: ${"%.2f".format(result.minLatencyMs)}ms, " +
                "Max: ${"%.2f".format(result.maxLatencyMs)}ms, " +
                "P95: ${"%.2f".format(result.p95LatencyMs)}ms")

        return result
    }
}

/**
 * Synthetic sleep stages for testing
 */
enum class SyntheticSleepStage {
    AWAKE, LIGHT, DEEP, REM
}

/**
 * Result of pipeline test
 */
data class PipelineTestResult(
    val success: Boolean,
    val predictedStage: Int,
    val confidence: Float,
    val bufferSize: Int,
    val epochHistory: Int,
    val interpolationRate: Float,
    val durationMs: Long
)

/**
 * Result of interpolation test
 */
data class InterpolationTestResult(
    val totalSamples: Int,
    val interpolatedSamples: Int,
    val interpolationRate: Float
)

/**
 * Latency benchmark results
 */
data class LatencyBenchmark(
    val iterations: Int,
    val avgLatencyMs: Double,
    val minLatencyMs: Double,
    val maxLatencyMs: Double,
    val p50LatencyMs: Double,
    val p95LatencyMs: Double
)