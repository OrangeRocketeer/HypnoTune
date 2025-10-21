package com.samsung.health.mobile.ml

import android.util.Log
import com.samsung.health.data.TrackedData
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.math.abs

private const val TAG = "DataBuffer"

/**
 * Buffered data point with timestamp for interpolation
 */
data class BufferedDataPoint(
    val timestamp: Long,
    val hr: Float,
    val accelX: Float,
    val accelY: Float,
    val accelZ: Float,
    val isInterpolated: Boolean = false
)

/**
 * Robust data buffer with interpolation for missing samples
 *
 * Handles:
 * - Missing data with interpolation (up to 30 seconds)
 * - Irregular sampling intervals
 * - Watch disconnection/reconnection
 * - Buffer overflow protection
 */
class DataBuffer {

    // Store 30 minutes of 5-second samples (360 samples)
    // Needed for 25-minute rolling windows + safety margin
    private val buffer = ConcurrentLinkedDeque<BufferedDataPoint>()
    private val maxBufferSize = 360 // 30 minutes at 5-second intervals

    private var lastReceivedTimestamp: Long = 0L
    private var lastReceivedData: BufferedDataPoint? = null

    // Configuration
    private val expectedIntervalMs = 5000L // 5 seconds
    private val maxInterpolationGapMs = 30000L // 30 seconds max gap
    private val interpolationTolerance = 2000L // 2 seconds tolerance

    /**
     * Add new data point from watch
     * Automatically interpolates missing samples if needed
     */
    fun addDataPoint(data: TrackedData) {
        val currentTime = System.currentTimeMillis()

        val newPoint = BufferedDataPoint(
            timestamp = currentTime,
            hr = data.hr.toFloat(),
            accelX = data.accelX,
            accelY = data.accelY,
            accelZ = data.accelZ,
            isInterpolated = false
        )

        // Check if we need to interpolate missing samples
        if (lastReceivedTimestamp > 0L) {
            val timeSinceLastSample = currentTime - lastReceivedTimestamp

            // If gap is significant, interpolate
            if (timeSinceLastSample > expectedIntervalMs + interpolationTolerance) {
                handleMissingData(lastReceivedData!!, newPoint, timeSinceLastSample)
            }
        }

        // Add the real data point
        buffer.addLast(newPoint)
        lastReceivedTimestamp = currentTime
        lastReceivedData = newPoint

        // Maintain buffer size
        while (buffer.size > maxBufferSize) {
            buffer.removeFirst()
        }

        Log.d(TAG, "Added data point: HR=${data.hr}, Buffer size=${buffer.size}")
    }

    /**
     * Interpolate missing data points when watch fails to send data
     */
    private fun handleMissingData(
        lastPoint: BufferedDataPoint,
        currentPoint: BufferedDataPoint,
        gapMs: Long
    ) {
        // Don't interpolate if gap is too large
        if (gapMs > maxInterpolationGapMs) {
            Log.w(TAG, "⚠️ Gap too large (${gapMs}ms), skipping interpolation. Using last known value.")
            // Just repeat last value once to maintain continuity
            val fillerPoint = lastPoint.copy(
                timestamp = lastPoint.timestamp + expectedIntervalMs,
                isInterpolated = true
            )
            buffer.addLast(fillerPoint)
            return
        }

        // Calculate number of missing samples
        val numMissingSamples = (gapMs / expectedIntervalMs).toInt() - 1

        if (numMissingSamples <= 0) return

        Log.w(TAG, "⚠️ Interpolating $numMissingSamples missing samples (gap: ${gapMs}ms)")

        // Linear interpolation for heart rate and accelerometer
        for (i in 1..numMissingSamples) {
            val ratio = i.toFloat() / (numMissingSamples + 1)

            val interpolatedPoint = BufferedDataPoint(
                timestamp = lastPoint.timestamp + (expectedIntervalMs * i),
                hr = lastPoint.hr + ratio * (currentPoint.hr - lastPoint.hr),
                accelX = lastPoint.accelX + ratio * (currentPoint.accelX - lastPoint.accelX),
                accelY = lastPoint.accelY + ratio * (currentPoint.accelY - lastPoint.accelY),
                accelZ = lastPoint.accelZ + ratio * (currentPoint.accelZ - lastPoint.accelZ),
                isInterpolated = true
            )

            buffer.addLast(interpolatedPoint)
        }

        Log.d(TAG, "✅ Interpolated $numMissingSamples points successfully")
    }

    /**
     * Get all buffered data points
     */
    fun getAllData(): List<BufferedDataPoint> {
        return buffer.toList()
    }

    /**
     * Get last N samples
     */
    fun getLastNSamples(n: Int): List<BufferedDataPoint> {
        val size = buffer.size
        if (size <= n) {
            return buffer.toList()
        }
        return buffer.toList().takeLast(n)
    }

    /**
     * Check if we have enough data for predictions
     * Need at least 6 samples for one 30-second epoch
     */
    fun hasMinimumData(): Boolean {
        return buffer.size >= 6
    }

    /**
     * Check if we have enough data for temporal features
     * Need at least 5 epochs (30 samples) for lag features
     */
    fun hasSufficientTemporalData(): Boolean {
        return buffer.size >= 30
    }

    /**
     * Get buffer size
     */
    fun size(): Int = buffer.size

    /**
     * Clear buffer (when stopping session)
     */
    fun clear() {
        buffer.clear()
        lastReceivedTimestamp = 0L
        lastReceivedData = null
        Log.i(TAG, "Buffer cleared")
    }

    /**
     * Check if data is stale (no updates in 30+ seconds)
     */
    fun isDataStale(): Boolean {
        if (lastReceivedTimestamp == 0L) return false
        val timeSinceLastUpdate = System.currentTimeMillis() - lastReceivedTimestamp
        return timeSinceLastUpdate > maxInterpolationGapMs
    }

    /**
     * Get time since last real (non-interpolated) data point
     */
    fun getTimeSinceLastRealData(): Long {
        if (lastReceivedTimestamp == 0L) return 0L
        return System.currentTimeMillis() - lastReceivedTimestamp
    }

    /**
     * Get statistics about interpolated data
     */
    fun getInterpolationStats(): InterpolationStats {
        val total = buffer.size
        val interpolated = buffer.count { it.isInterpolated }
        return InterpolationStats(
            totalSamples = total,
            interpolatedSamples = interpolated,
            realSamples = total - interpolated,
            interpolationRate = if (total > 0) interpolated.toFloat() / total else 0f
        )
    }
}

/**
 * Statistics about interpolated data
 */
data class InterpolationStats(
    val totalSamples: Int,
    val interpolatedSamples: Int,
    val realSamples: Int,
    val interpolationRate: Float
) {
    fun getPercentageInterpolated(): Int = (interpolationRate * 100).toInt()
}