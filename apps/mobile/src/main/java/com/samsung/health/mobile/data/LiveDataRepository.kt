package com.samsung.health.mobile.data

import android.util.Log
import com.samsung.health.data.TrackedData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "LiveDataRepository"

/**
 * Data class for live watch data and ML predictions
 */
data class LiveWatchData(
    // Latest sensor data from watch
    val trackedData: TrackedData? = null,

    // ML prediction results
    val predictedStage: Int = 0,
    val confidence: Float = 0f,

    // Timestamp
    val timestamp: Long = 0L,

    // Status
    val isReceivingData: Boolean = false
) {
    fun getSecondsSinceUpdate(): Long {
        if (timestamp == 0L) return 0L
        return (System.currentTimeMillis() - timestamp) / 1000
    }

    fun isDataStale(): Boolean {
        return getSecondsSinceUpdate() > 10
    }
}

/**
 * Repository for managing live watch data
 * Uses StateFlow for reactive UI updates
 */
@Singleton
class LiveDataRepository @Inject constructor() {

    private val _liveData = MutableStateFlow(LiveWatchData())
    val liveData: StateFlow<LiveWatchData> = _liveData.asStateFlow()

    /**
     * Update live data with new sensor readings and ML prediction
     */
    fun updateLiveData(
        trackedData: TrackedData,
        predictedStage: Int,
        confidence: Float
    ) {
        _liveData.value = LiveWatchData(
            trackedData = trackedData,
            predictedStage = predictedStage,
            confidence = confidence,
            timestamp = System.currentTimeMillis(),
            isReceivingData = true
        )

        Log.d(TAG, "✅ Live data updated: HR=${trackedData.hr}, Stage=$predictedStage, Confidence=${(confidence * 100).toInt()}%")
    }

    /**
     * Clear live data (call when stopping)
     */
    fun clearLiveData() {
        _liveData.value = LiveWatchData()
        Log.d(TAG, "Live data cleared")
    }

    /**
     * Get current live data value
     */
    fun getCurrentData(): LiveWatchData {
        return _liveData.value
    }
}