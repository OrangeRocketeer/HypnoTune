package com.samsung.health.mobile.data

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.samsung.health.mobile.ml.MLModelInterface
import com.samsung.health.mobile.presentation.HelpFunctions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "DataListenerService"
private const val MESSAGE_PATH = "/msg"

@AndroidEntryPoint
class DataListenerService : WearableListenerService() {

    @Inject
    lateinit var recordingRepository: RecordingRepository

    @Inject
    lateinit var mlModelInterface: MLModelInterface

    @Inject
    lateinit var stageManager: StageManager

    @Inject
    lateinit var liveDataRepository: LiveDataRepository

    // Track last predicted stage to avoid redundant broadcasts
    private var lastBroadcastedStage: Int = -1

    // Handler for periodic predictions (every 15 seconds)
    private val predictionHandler = Handler(Looper.getMainLooper())
    private var predictionRunnable: Runnable? = null
    private var isPredictionScheduled = false

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        val value = messageEvent.data.decodeToString()
        Log.i(TAG, "📨 Message received from watch")

        when (messageEvent.path) {
            MESSAGE_PATH -> {
                if (value.isNotEmpty()) {
                    try {
                        val measurementResults = HelpFunctions.decodeMessage(value)

                        measurementResults.forEach { data ->
                            // === Step 1: Process data through ML pipeline ===
                            // This adds data to buffer with automatic interpolation
                            mlModelInterface.processData(data)

                            // Log buffer status
                            val bufferSize = mlModelInterface.getBufferSize()
                            val epochHistory = mlModelInterface.getEpochHistorySize()
                            Log.d(TAG, "📊 Pipeline status: Buffer=${bufferSize}, Epochs=${epochHistory}")

                            // === Step 2: Record to CSV if recording ===
                            if (recordingRepository.isRecording()) {
                                recordingRepository.recordData(data)
                                Log.d(TAG, "📝 Data recorded: HR=${data.hr}")
                            }

                            // === Step 3: Update live UI (always) ===
                            updateLiveUI(data)
                        }

                        // === Step 4: Schedule periodic predictions if not already scheduled ===
                        if (!isPredictionScheduled && recordingRepository.isRecording()) {
                            schedulePredictions()
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error processing data", e)

                        // Fallback to stage 0 on error
                        if (recordingRepository.isRecording() && lastBroadcastedStage == -1) {
                            broadcastStagePrediction(0, 0f)
                        }
                    }

                    // Backward compatibility - broadcast raw data update
                    sendBroadcast(Intent("com.samsung.health.mobile.DATA_UPDATED").apply {
                        putExtra("message", value)
                    })
                }
            }
        }
    }

    /**
     * Schedule predictions every 15 seconds
     */
    private fun schedulePredictions() {
        if (isPredictionScheduled) return

        predictionRunnable = object : Runnable {
            override fun run() {
                if (recordingRepository.isRecording()) {
                    runPrediction()
                    // Schedule next prediction
                    predictionHandler.postDelayed(this, 15000) // 15 seconds
                } else {
                    // Stop scheduling if not recording
                    isPredictionScheduled = false
                }
            }
        }

        // Start first prediction immediately
        predictionHandler.post(predictionRunnable!!)
        isPredictionScheduled = true

        Log.i(TAG, "⏰ Prediction scheduling started (every 15 seconds)")
    }

    /**
     * Run ML prediction
     */
    private fun runPrediction() {
        if (!mlModelInterface.isModelReady()) {
            Log.w(TAG, "⚠️ Model not ready")
            // Broadcast stage 0 if this is the first prediction
            if (lastBroadcastedStage == -1) {
                broadcastStagePrediction(0, 0f)
            }
            return
        }

        try {
            // Get prediction from ML model
            val predictedStage = mlModelInterface.predictStage()
            val confidence = mlModelInterface.getLastConfidence()

            // Log interpolation statistics
            val interpStats = mlModelInterface.getInterpolationStats()
            Log.d(TAG, "📈 Interpolation rate: ${interpStats.getPercentageInterpolated()}%")

            // Broadcast only if stage changed
            if (predictedStage != lastBroadcastedStage) {
                broadcastStagePrediction(predictedStage, confidence)
            } else {
                Log.d(TAG, "Stage unchanged: $predictedStage (skipping broadcast)")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Prediction failed", e)
        }
    }

    /**
     * Broadcast stage prediction to MusicPlayerService
     */
    private fun broadcastStagePrediction(stage: Int, confidence: Float) {
        sendBroadcast(Intent("com.samsung.health.mobile.STAGE_PREDICTION").apply {
            putExtra("predicted_stage", stage)
            putExtra("confidence", confidence)
        })

        lastBroadcastedStage = stage
        Log.i(TAG, "🎵 Stage broadcast: $stage (confidence: ${"%.1f".format(confidence * 100)}%)")
    }

    /**
     * Update live UI with latest data
     */
    private fun updateLiveUI(data: com.samsung.health.data.TrackedData) {
        // Get current prediction info (without triggering new prediction)
        val currentStage = mlModelInterface.getLastPredictedStage()
        val currentConfidence = mlModelInterface.getLastConfidence()

        // Update live data repository for UI
        liveDataRepository.updateLiveData(
            trackedData = data,
            predictedStage = currentStage,
            confidence = currentConfidence
        )

        Log.d(TAG, "📱 UI updated: HR=${data.hr}, Stage=$currentStage")
    }

    /**
     * Stop prediction scheduling
     */
    private fun stopPredictions() {
        predictionRunnable?.let {
            predictionHandler.removeCallbacks(it)
        }
        isPredictionScheduled = false
        Log.i(TAG, "⏹️ Prediction scheduling stopped")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPredictions()
        lastBroadcastedStage = -1
        Log.i(TAG, "DataListenerService destroyed")
    }
}