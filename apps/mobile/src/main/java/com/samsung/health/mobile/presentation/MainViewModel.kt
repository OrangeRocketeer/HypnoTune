package com.samsung.health.mobile.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samsung.health.mobile.data.LiveDataRepository
import com.samsung.health.mobile.data.LiveWatchData
import com.samsung.health.mobile.data.MusicProfile
import com.samsung.health.mobile.data.MusicProfileManager
import com.samsung.health.mobile.data.ProfileReminderManager
import com.samsung.health.mobile.data.RecordingRepository
import com.samsung.health.mobile.data.StageConfig
import com.samsung.health.mobile.data.StageConfigManager
import com.samsung.health.mobile.data.StageManager
import com.samsung.health.mobile.ml.MLModelInterface
import com.samsung.health.mobile.service.MusicPlayerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val recordingRepository: RecordingRepository,
    val stageConfigManager: StageConfigManager,
    val stageManager: StageManager,
    private val mlModelInterface: MLModelInterface,
    val musicProfileManager: MusicProfileManager,
    val profileReminderManager: ProfileReminderManager,
    val liveDataRepository: LiveDataRepository
) : ViewModel() {

    private val _recordingState = MutableStateFlow(RecordingState())
    val recordingState: StateFlow<RecordingState> = _recordingState

    private val _stageState = MutableStateFlow(StageState())
    val stageState: StateFlow<StageState> = _stageState

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState

    private val _reminderState = MutableStateFlow(ReminderState())
    val reminderState: StateFlow<ReminderState> = _reminderState

    // Expose live data from repository (no more broadcasts!)
    val liveDataState: StateFlow<LiveWatchData> = liveDataRepository.liveData

    private var isSystemActive = false

    init {
        // Check if recording was already in progress
        _recordingState.value = RecordingState(
            isRecording = recordingRepository.isRecording(),
            fileName = recordingRepository.getCurrentFileName()
        )

        Log.i(TAG, "MainViewModel initialized")
    }

    // Stage Management functions
    fun initializeStages(context: Context) {
        stageConfigManager.initialize(context)
        musicProfileManager.initialize(context)
        profileReminderManager.initialize(context)

        _stageState.value = _stageState.value.copy(
            stages = stageConfigManager.getStages()
        )

        // Load profile state
        _profileState.value = ProfileState(
            currentProfile = musicProfileManager.getCurrentProfile(),
            allProfiles = musicProfileManager.getAllProfiles()
        )

        // Check if reminder should be shown
        checkAndShowReminder(context)

        // Load ML model
        mlModelInterface.loadModel(context)
        Log.i(TAG, "Stages and profiles initialized, ML model loaded")
    }

    fun updateStage(stage: StageConfig) {
        stageConfigManager.updateStage(stage)
        _stageState.value = _stageState.value.copy(
            stages = stageConfigManager.getStages()
        )

        // AUTO-SAVE: Update the current profile with the changes
        autoSaveProfileChanges()

        Log.i(TAG, "Stage ${stage.stageNumber} updated")
    }

    fun resetStagesToDefaults() {
        stageConfigManager.resetToDefaults()
        _stageState.value = _stageState.value.copy(
            stages = stageConfigManager.getStages()
        )
        Log.i(TAG, "Stages reset to defaults")
    }

    fun toggleMLEnabled() {
        _stageState.value = _stageState.value.copy(
            isMLEnabled = !_stageState.value.isMLEnabled
        )
        Log.i(TAG, "ML enabled: ${_stageState.value.isMLEnabled}")
    }

    fun updateStageMusicUri(stageNumber: Int, musicUri: Uri) {
        stageConfigManager.updateStageMusicUri(stageNumber, musicUri)
        _stageState.value = _stageState.value.copy(
            stages = stageConfigManager.getStages()
        )

        // AUTO-SAVE: Update the current profile with the new music
        autoSaveProfileChanges()

        Log.i(TAG, "Music URI updated for stage $stageNumber: $musicUri")
    }

    /**
     * Auto-save current stage configuration to the active profile
     */
    private fun autoSaveProfileChanges() {
        val currentProfile = _profileState.value.currentProfile
        if (currentProfile != null) {
            val updatedStages = stageConfigManager.getStages()
            val updatedProfile = currentProfile.copy(stages = updatedStages)

            if (currentProfile.isCustom) {
                // Update existing custom profile
                musicProfileManager.updateCustomProfile(updatedProfile)
                Log.i(TAG, "Updated custom profile: ${currentProfile.name}")
            } else {
                // Pre-built profile modified, create a modified version
                val modifiedProfile = updatedProfile.copy(
                    id = "custom_${currentProfile.id}_${System.currentTimeMillis()}",
                    name = "${currentProfile.name} (Modified)",
                    isCustom = true,
                    createdTimestamp = System.currentTimeMillis()
                )
                musicProfileManager.saveCustomProfile(modifiedProfile)
                musicProfileManager.applyProfile(modifiedProfile)
                Log.i(TAG, "Created modified profile: ${modifiedProfile.name}")
            }

            // Refresh profile state
            _profileState.value = _profileState.value.copy(
                currentProfile = musicProfileManager.getCurrentProfile(),
                allProfiles = musicProfileManager.getAllProfiles()
            )
        }
    }

    fun updateCurrentStage(stage: Int) {
        _stageState.value = _stageState.value.copy(currentStage = stage)
    }

    // Profile Management functions
    fun applyProfile(profile: MusicProfile) {
        musicProfileManager.applyProfile(profile)
        _stageState.value = _stageState.value.copy(
            stages = stageConfigManager.getStages()
        )
        _profileState.value = _profileState.value.copy(
            currentProfile = profile,
            allProfiles = musicProfileManager.getAllProfiles()
        )

        // Record profile change to reset 21-day timer
        profileReminderManager.recordProfileChange()
        updateReminderState()

        Log.i(TAG, "Applied profile: ${profile.name}")
    }

    fun saveAsCustomProfile(name: String, description: String) {
        val newProfile = musicProfileManager.saveAsCustomProfile(name, description)
        _profileState.value = _profileState.value.copy(
            currentProfile = newProfile,
            allProfiles = musicProfileManager.getAllProfiles()
        )

        // Record profile change to reset 21-day timer
        profileReminderManager.recordProfileChange()
        updateReminderState()

        Log.i(TAG, "Saved custom profile: $name")
    }

    fun deleteCustomProfile(profileId: String) {
        musicProfileManager.deleteCustomProfile(profileId)
        _profileState.value = _profileState.value.copy(
            currentProfile = musicProfileManager.getCurrentProfile(),
            allProfiles = musicProfileManager.getAllProfiles()
        )
        Log.i(TAG, "Deleted custom profile: $profileId")
    }

    // Reminder Management functions
    fun checkAndShowReminder(context: Context) {
        if (profileReminderManager.shouldShowReminder()) {
            profileReminderManager.showReminderNotification(context)
        }
        updateReminderState()
    }

    fun dismissReminder() {
        profileReminderManager.dismissReminder()
        updateReminderState()
    }

    private fun updateReminderState() {
        val stats = profileReminderManager.getReminderStats()
        _reminderState.value = ReminderState(
            daysSinceLastChange = stats.daysSinceLastChange,
            daysUntilReminder = stats.daysUntilReminder,
            shouldShowReminder = profileReminderManager.shouldShowReminder()
        )
    }

    // ============================================
    // RECORDING + MUSIC CONTROL (SIMPLIFIED)
    // ============================================

    /**
     * Start recording AND music playback simultaneously
     * This is the single button that activates the entire system
     */
    fun startRecordingAndMusic(context: Context) {
        viewModelScope.launch {
            // Start recording
            val recordingSuccess = recordingRepository.startRecording(context)

            if (recordingSuccess) {
                _recordingState.value = RecordingState(
                    isRecording = true,
                    fileName = recordingRepository.getCurrentFileName(),
                    message = "Recording started"
                )

                // Start music service
                startMusicService(context)

                // Reset ML tracking
                mlModelInterface.resetTracking()

                isSystemActive = true

                Log.i(TAG, "✅ System activated: Recording + Music started")
            } else {
                _recordingState.value = RecordingState(
                    isRecording = false,
                    fileName = null,
                    message = "Failed to start recording"
                )
                Log.e(TAG, "Failed to start recording")
            }
        }
    }

    /**
     * Stop recording AND music playback simultaneously
     */
    fun stopRecordingAndMusic(context: Context) {
        viewModelScope.launch {
            // Stop music first
            stopMusicService(context)

            // Stop recording
            val file = recordingRepository.stopRecording()

            if (file != null && file.exists()) {
                _recordingState.value = RecordingState(
                    isRecording = false,
                    fileName = null,
                    savedFile = file,
                    message = "Recording stopped. File saved."
                )
                Log.i(TAG, "Recording stopped. File: ${file.absolutePath}")

                // Automatically share/download the file
                shareFile(context, file)
            } else {
                _recordingState.value = RecordingState(
                    isRecording = false,
                    fileName = null,
                    message = "Failed to save recording"
                )
                Log.e(TAG, "Failed to stop recording or file not found")
            }

            // Reset live data state
            liveDataRepository.clearLiveData()
            isSystemActive = false

            Log.i(TAG, "✅ System deactivated: Recording + Music stopped")
        }
    }

    /**
     * Start music player service
     */
    private fun startMusicService(context: Context) {
        // Get Stage 0 music or use default
        val stage0Config = stageConfigManager.getStage(0)
        val musicUri = stage0Config?.musicUri

        if (musicUri == null) {
            Log.w(TAG, "No music configured for Stage 0, music will not play")
            return
        }

        // Calculate end time (default 1 hour from now)
        val endTime = System.currentTimeMillis() + (60 * 60 * 1000)

        val serviceIntent = Intent(context, MusicPlayerService::class.java).apply {
            action = MusicPlayerService.ACTION_START
            putExtra(MusicPlayerService.EXTRA_MUSIC_URI, musicUri)
            putExtra(MusicPlayerService.EXTRA_END_TIME, endTime)
            putExtra(MusicPlayerService.EXTRA_START_RECORDING, false) // We already started recording
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        Log.i(TAG, "Music service started with Stage 0 music")
    }

    /**
     * Stop music player service
     */
    private fun stopMusicService(context: Context) {
        val serviceIntent = Intent(context, MusicPlayerService::class.java).apply {
            action = MusicPlayerService.ACTION_STOP
        }
        context.startService(serviceIntent)
        Log.i(TAG, "Music service stopped")
    }

    private fun shareFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Health Data Recording")
                putExtra(Intent.EXTRA_TEXT, "Health data recorded from ${file.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(intent, "Save or Share CSV File").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing file", e)
        }
    }

    fun clearMessage() {
        _recordingState.value = _recordingState.value.copy(message = null)
    }

    /**
     * DEBUG FUNCTION: Test ML model loading and prediction
     */
    fun testMLModel(context: Context) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "STARTING ML MODEL TEST")
        Log.d(TAG, "========================================")

        try {
            // Step 1: Check if assets folder has the model
            Log.d(TAG, "Step 1: Checking assets folder...")
            val assetsList = context.assets.list("")
            Log.d(TAG, "Assets found: ${assetsList?.joinToString(", ")}")

            val hasModel = assetsList?.contains("lightgbm_live_model3.onnx") ?: false
            Log.d(TAG, "Model file exists: $hasModel")

            if (!hasModel) {
                Log.e(TAG, "❌ MODEL FILE NOT FOUND IN ASSETS!")
                Log.e(TAG, "Please add lightgbm_live_model3.onnx to src/main/assets/")
                return
            }

            // Step 2: Load the model
            Log.d(TAG, "Step 2: Loading ML model...")
            mlModelInterface.loadModel(context)

            // Step 3: Check if model loaded
            Log.d(TAG, "Step 3: Checking if model is ready...")
            val isReady = mlModelInterface.isModelReady()
            Log.d(TAG, "Model ready: $isReady")

            if (!isReady) {
                Log.e(TAG, "❌ MODEL FAILED TO LOAD!")
                return
            }

            // Step 4: Create test data
            Log.d(TAG, "Step 4: Creating test sensor data...")
            val testData = com.samsung.health.data.TrackedData(
                hr = 75,
                ibi = ArrayList<Int>(),
                accelX = 0.5f,
                accelY = 0.3f,
                accelZ = 9.8f
            )
            Log.d(TAG, "Test data: HR=${testData.hr}, Accel=(${testData.accelX}, ${testData.accelY}, ${testData.accelZ})")

            // Step 5: Run prediction
            Log.d(TAG, "Step 5: Running prediction...")
            val predictedStage = mlModelInterface.predictStage(testData)
            val confidence = mlModelInterface.getLastConfidence()

            Log.d(TAG, "✅ PREDICTION SUCCESS!")
            Log.d(TAG, "Predicted Stage: $predictedStage")
            Log.d(TAG, "Confidence: ${"%.2f".format(confidence)}")

            Log.d(TAG, "========================================")
            Log.d(TAG, "ML MODEL TEST COMPLETED SUCCESSFULLY!")
            Log.d(TAG, "========================================")

        } catch (e: Exception) {
            Log.e(TAG, "❌ ML MODEL TEST FAILED!", e)
            Log.e(TAG, "Error: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Clean up resources when ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()

        // Clean up ML model resources
        mlModelInterface.cleanup()

        Log.i(TAG, "ViewModel cleared, ML model and resources cleaned up")
    }
}

// Recording State
data class RecordingState(
    val isRecording: Boolean = false,
    val fileName: String? = null,
    val savedFile: File? = null,
    val message: String? = null
)

// Stage State
data class StageState(
    val currentStage: Int = 0,
    val stages: List<StageConfig> = emptyList(),
    val isMLEnabled: Boolean = true
)

// Profile State
data class ProfileState(
    val currentProfile: MusicProfile? = null,
    val allProfiles: List<MusicProfile> = emptyList()
)

// Reminder State
data class ReminderState(
    val daysSinceLastChange: Int = 0,
    val daysUntilReminder: Int = 21,
    val shouldShowReminder: Boolean = false
)