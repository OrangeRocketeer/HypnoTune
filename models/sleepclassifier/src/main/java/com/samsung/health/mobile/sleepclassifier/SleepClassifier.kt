package com.yourapp.sleepclassifier

import android.content.Context
import com.yourapp.sleepclassifier.internal.PredictionResult
import com.yourapp.sleepclassifier.internal.SleepInferenceEngine
import com.yourapp.sleepclassifier.internal.WatchDataPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Public interface for the Sleep Classifier module.
 * Your main application will interact only with this class.
 */
class SleepClassifier(context: Context) {

    private val engine = SleepInferenceEngine(context.applicationContext)
    private val scope = CoroutineScope(Job() + Dispatchers.Default) // Use a background thread

    /**
     * A flow that emits the latest sleep stage prediction.
     * Collect this in your ViewModel or Service to get live updates.
     */
    val predictionResult: StateFlow<PredictionResult> = engine.predictionFlow

    /**
     * Call this method every time you receive new data from the watch.
     * This method is thread-safe and offloads processing to a background thread.
     *
     * @param timestamp The timestamp of the reading.
     * @param heartRate The heart rate value.
     * @param motionX The motion value on the X axis.
     * @param motionY The motion value on the Y axis.
     * @param motionZ The motion value on the Z axis.
     */
    fun onNewDataReceived(
        timestamp: Long,
        heartRate: Float,
        motionX: Float,
        motionY: Float,
        motionZ: Float
    ) {
        scope.launch {
            val dataPoint = WatchDataPoint(timestamp, heartRate, motionX, motionY, motionZ)
            engine.processNewSample(dataPoint)
        }
    }

    /**
     * Call this when the classifier is no longer needed to release resources.
     */
    fun destroy() {
        engine.close()
        // Cancel the coroutine scope
        scope.coroutineContext.cancel()
    }
}