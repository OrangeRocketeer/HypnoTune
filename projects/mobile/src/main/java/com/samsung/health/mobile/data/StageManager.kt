package com.samsung.health.mobile.data

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "StageManager"

/**
 * Manages stage transitions with smooth volume fading and music switching
 */
@Singleton
class StageManager @Inject constructor(
    private val stageConfigManager: StageConfigManager
) {
    private var currentStage: Int = 0
    private var targetStage: Int = 0
    private var fadeJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentVolumeTracked: Float = 1f

    fun getCurrentStage(): Int = currentStage

    fun getTargetStage(): Int = targetStage

    /**
     * Transition to a new stage with volume fading and music switching
     */
    fun transitionToStage(
        stageNumber: Int,
        mediaPlayer: MediaPlayer?,
        onTransitionComplete: ((StageConfig) -> Unit)? = null,
        onMusicSwitchNeeded: ((Uri) -> Unit)? = null
    ) {
        if (stageNumber !in 0..4) {
            Log.w(TAG, "Invalid stage number: $stageNumber")
            return
        }

        if (stageNumber == currentStage) {
            Log.d(TAG, "Already in stage $stageNumber")
            return
        }

        val newStageConfig = stageConfigManager.getStage(stageNumber)
        if (newStageConfig == null) {
            Log.e(TAG, "Stage config not found for stage $stageNumber")
            return
        }

        targetStage = stageNumber
        Log.i(TAG, "Transitioning from stage $currentStage to $targetStage")

        // Cancel any ongoing fade
        fadeJob?.cancel()

        // Check if music file needs to be switched
        val currentStageConfig = stageConfigManager.getStage(currentStage)
        val needsMusicSwitch = newStageConfig.musicUri != null &&
                newStageConfig.musicUri != currentStageConfig?.musicUri

        if (needsMusicSwitch && newStageConfig.musicUri != null) {
            Log.i(TAG, "Music switch needed for stage $stageNumber")
            onMusicSwitchNeeded?.invoke(Uri.parse(newStageConfig.musicUri))
            currentStage = stageNumber
            currentVolumeTracked = newStageConfig.targetVolume / 100f
            onTransitionComplete?.invoke(newStageConfig)
            return
        }

        if (mediaPlayer == null || !mediaPlayer.isPlaying) {
            currentStage = stageNumber
            Log.w(TAG, "MediaPlayer not available, stage set without fade")
            onTransitionComplete?.invoke(newStageConfig)
            return
        }

        // Start fade transition (same music file, just volume change)
        fadeJob = scope.launch {
            try {
                fadeVolume(
                    mediaPlayer = mediaPlayer,
                    targetVolumePercent = newStageConfig.targetVolume,
                    durationMs = newStageConfig.fadeDuration
                )
                currentStage = stageNumber
                currentVolumeTracked = newStageConfig.targetVolume / 100f
                Log.i(TAG, "Stage transition complete: Now in stage $currentStage (${newStageConfig.stageName})")
                onTransitionComplete?.invoke(newStageConfig)
            } catch (e: CancellationException) {
                Log.i(TAG, "Fade cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "Error during fade", e)
            }
        }
    }

    /**
     * Smoothly fade volume from current to target
     */
    private suspend fun fadeVolume(
        mediaPlayer: MediaPlayer,
        targetVolumePercent: Int,
        durationMs: Int
    ) = withContext(Dispatchers.Main) {
        val currentVolumeFloat = currentVolumeTracked
        val targetVolumeFloat = targetVolumePercent / 100f

        Log.d(TAG, "Fading volume from ${(currentVolumeFloat * 100).toInt()}% to $targetVolumePercent% over ${durationMs}ms")

        val steps = 50 // Number of steps in the fade
        val stepDelay = durationMs / steps
        val volumeStep = (targetVolumeFloat - currentVolumeFloat) / steps

        repeat(steps) { step ->
            if (!isActive) return@withContext

            val newVolume = (currentVolumeFloat + (volumeStep * (step + 1))).coerceIn(0f, 1f)
            try {
                mediaPlayer.setVolume(newVolume, newVolume)
                currentVolumeTracked = newVolume
            } catch (e: Exception) {
                Log.e(TAG, "Error setting volume", e)
                return@withContext
            }
            delay(stepDelay.toLong())
        }

        // Ensure final volume is exact
        try {
            mediaPlayer.setVolume(targetVolumeFloat, targetVolumeFloat)
            currentVolumeTracked = targetVolumeFloat
        } catch (e: Exception) {
            Log.e(TAG, "Error setting final volume", e)
        }
    }

    /**
     * Set initial stage (call when music starts)
     */
    fun initializeStage(stageNumber: Int, mediaPlayer: MediaPlayer?) {
        currentStage = stageNumber
        targetStage = stageNumber

        val stageConfig = stageConfigManager.getStage(stageNumber)
        if (stageConfig != null && mediaPlayer != null) {
            val volumeFloat = stageConfig.targetVolume / 100f
            currentVolumeTracked = volumeFloat
            try {
                mediaPlayer.setVolume(volumeFloat, volumeFloat)
                Log.i(TAG, "Initialized stage $stageNumber with volume ${stageConfig.targetVolume}%")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing volume", e)
            }
        }
    }

    /**
     * Maintain volume tracking when music is switched externally
     */
    fun updateVolumeAfterMusicSwitch(mediaPlayer: MediaPlayer?, targetVolumePercent: Int) {
        if (mediaPlayer == null) return

        val volumeFloat = targetVolumePercent / 100f
        currentVolumeTracked = volumeFloat
        try {
            mediaPlayer.setVolume(volumeFloat, volumeFloat)
            Log.i(TAG, "Updated volume after music switch to $targetVolumePercent%")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating volume after music switch", e)
        }
    }

    /**
     * Get current volume as percentage
     */
    fun getCurrentVolumePercent(): Int {
        return (currentVolumeTracked * 100).toInt()
    }

    fun cleanup() {
        fadeJob?.cancel()
        scope.cancel()
    }
}