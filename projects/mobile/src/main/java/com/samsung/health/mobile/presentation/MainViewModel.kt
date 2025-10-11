package com.samsung.health.mobile.presentation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samsung.health.mobile.data.MusicProfile
import com.samsung.health.mobile.data.MusicProfileManager
import com.samsung.health.mobile.data.RecordingRepository
import com.samsung.health.mobile.data.StageConfig
import com.samsung.health.mobile.data.StageConfigManager
import com.samsung.health.mobile.data.StageManager
import com.samsung.health.mobile.ml.MLModelInterface
import com.samsung.health.mobile.receiver.ScheduleReceiver
import com.samsung.health.mobile.service.MusicPlayerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import javax.inject.Inject

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val recordingRepository: RecordingRepository,
    val stageConfigManager: StageConfigManager,
    val stageManager: StageManager,
    private val mlModelInterface: MLModelInterface,
    val musicProfileManager: MusicProfileManager
) : ViewModel() {

    private val _recordingState = MutableStateFlow(RecordingState())
    val recordingState: StateFlow<RecordingState> = _recordingState

    private val _musicState = MutableStateFlow(MusicState())
    val musicState: StateFlow<MusicState> = _musicState

    private val _stageState = MutableStateFlow(StageState())
    val stageState: StateFlow<StageState> = _stageState

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState

    private var musicStartReceiver: BroadcastReceiver? = null
    private var musicStopReceiver: BroadcastReceiver? = null

    init {
        // Check if recording was already in progress
        _recordingState.value = RecordingState(
            isRecording = recordingRepository.isRecording(),
            fileName = recordingRepository.getCurrentFileName()
        )
    }

    fun registerMusicReceivers(context: Context) {
        // Register receiver for music started
        musicStartReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.i(TAG, "Music started - auto-starting recording")
                startRecording(context!!)
            }
        }

        // Register receiver for music stopped
        musicStopReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.i(TAG, "Music stopped - auto-stopping recording")
                stopRecording(context!!)
            }
        }

        // Since minSdk = 30, we can always use RECEIVER_NOT_EXPORTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                musicStartReceiver,
                IntentFilter("com.samsung.health.mobile.MUSIC_STARTED"),
                Context.RECEIVER_NOT_EXPORTED
            )
            context.registerReceiver(
                musicStopReceiver,
                IntentFilter("com.samsung.health.mobile.MUSIC_STOPPED"),
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            // For API 26-32, use ContextCompat which handles the flag correctly
            androidx.core.content.ContextCompat.registerReceiver(
                context,
                musicStartReceiver,
                IntentFilter("com.samsung.health.mobile.MUSIC_STARTED"),
                androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
            )
            androidx.core.content.ContextCompat.registerReceiver(
                context,
                musicStopReceiver,
                IntentFilter("com.samsung.health.mobile.MUSIC_STOPPED"),
                androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
    }

    fun unregisterMusicReceivers(context: Context) {
        musicStartReceiver?.let { context.unregisterReceiver(it) }
        musicStopReceiver?.let { context.unregisterReceiver(it) }
    }

    // Music functions
    fun setMusicFile(uri: Uri) {
        _musicState.value = _musicState.value.copy(
            selectedMusicUri = uri,
            errorMessage = null
        )
        Log.i(TAG, "Music file selected: $uri")
    }

    fun updateStartDate(calendar: Calendar) {
        val updated = _musicState.value.startDateTime.clone() as Calendar
        updated.set(Calendar.YEAR, calendar.get(Calendar.YEAR))
        updated.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
        updated.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
        _musicState.value = _musicState.value.copy(startDateTime = updated, errorMessage = null)
    }

    fun updateStartTime(calendar: Calendar) {
        val updated = _musicState.value.startDateTime.clone() as Calendar
        updated.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
        updated.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
        updated.set(Calendar.SECOND, 0)
        _musicState.value = _musicState.value.copy(startDateTime = updated, errorMessage = null)
    }

    fun updateEndDate(calendar: Calendar) {
        val updated = _musicState.value.endDateTime.clone() as Calendar
        updated.set(Calendar.YEAR, calendar.get(Calendar.YEAR))
        updated.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
        updated.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
        _musicState.value = _musicState.value.copy(endDateTime = updated, errorMessage = null)
    }

    fun updateEndTime(calendar: Calendar) {
        val updated = _musicState.value.endDateTime.clone() as Calendar
        updated.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
        updated.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
        updated.set(Calendar.SECOND, 0)
        _musicState.value = _musicState.value.copy(endDateTime = updated, errorMessage = null)
    }

    fun scheduleMusic(context: Context) {
        val state = _musicState.value

        // Validation
        if (state.selectedMusicUri == null) {
            _musicState.value = state.copy(errorMessage = "Please select a music file")
            return
        }

        val now = Calendar.getInstance()
        if (state.startDateTime.before(now)) {
            _musicState.value = state.copy(errorMessage = "Start time must be in the future")
            return
        }

        if (state.endDateTime.before(state.startDateTime)) {
            _musicState.value = state.copy(errorMessage = "End time must be after start time")
            return
        }

        // Check exact alarm permission
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                _musicState.value = state.copy(errorMessage = "Please allow exact alarms in settings")
                return
            }
        }

        // Create intent for the broadcast receiver
        val intent = Intent(context, ScheduleReceiver::class.java).apply {
            action = "com.samsung.health.mobile.START_MUSIC"
            putExtra(MusicPlayerService.EXTRA_MUSIC_URI, state.selectedMusicUri.toString())
            putExtra(MusicPlayerService.EXTRA_END_TIME, state.endDateTime.timeInMillis)
            putExtra(MusicPlayerService.EXTRA_START_RECORDING, true) // Auto-start recording
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule exact alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                state.startDateTime.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                state.startDateTime.timeInMillis,
                pendingIntent
            )
        }

        _musicState.value = state.copy(
            isScheduled = true,
            errorMessage = null
        )

        Log.i(TAG, "Music scheduled successfully")
    }

    fun cancelSchedule(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ScheduleReceiver::class.java).apply {
            action = "com.samsung.health.mobile.START_MUSIC"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)

        // Stop the service if it's running
        val serviceIntent = Intent(context, MusicPlayerService::class.java)
        context.stopService(serviceIntent)

        _musicState.value = _musicState.value.copy(isScheduled = false)

        Log.i(TAG, "Music schedule cancelled")
    }

    fun playMusicNow(context: Context) {
        val state = _musicState.value

        if (state.selectedMusicUri == null) {
            _musicState.value = state.copy(errorMessage = "Please select a music file")
            return
        }

        // Set end time to 1 hour from now (or use scheduled end time if available)
        val endTime = if (state.endDateTime.after(Calendar.getInstance())) {
            state.endDateTime.timeInMillis
        } else {
            System.currentTimeMillis() + (60 * 60 * 1000) // 1 hour
        }

        val serviceIntent = Intent(context, MusicPlayerService::class.java).apply {
            action = MusicPlayerService.ACTION_START
            putExtra(MusicPlayerService.EXTRA_MUSIC_URI, state.selectedMusicUri.toString())
            putExtra(MusicPlayerService.EXTRA_END_TIME, endTime)
            putExtra(MusicPlayerService.EXTRA_START_RECORDING, true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        Log.i(TAG, "Playing music now")
    }

    fun stopMusicNow(context: Context) {
        val serviceIntent = Intent(context, MusicPlayerService::class.java).apply {
            action = MusicPlayerService.ACTION_STOP
        }
        context.startService(serviceIntent)

        Log.i(TAG, "Stopping music now")
    }

    // Stage Management functions
    fun initializeStages(context: Context) {
        stageConfigManager.initialize(context)
        musicProfileManager.initialize(context)

        _stageState.value = _stageState.value.copy(
            stages = stageConfigManager.getStages()
        )

        // Load profile state
        _profileState.value = ProfileState(
            currentProfile = musicProfileManager.getCurrentProfile(),
            allProfiles = musicProfileManager.getAllProfiles()
        )

        mlModelInterface.loadModel()
        Log.i(TAG, "Stages and profiles initialized")
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
        Log.i(TAG, "Applied profile: ${profile.name}")
    }

    fun saveAsCustomProfile(name: String, description: String) {
        val newProfile = musicProfileManager.saveAsCustomProfile(name, description)
        _profileState.value = _profileState.value.copy(
            currentProfile = newProfile,
            allProfiles = musicProfileManager.getAllProfiles()
        )
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

    // Recording functions
    fun startRecording(context: Context) {
        viewModelScope.launch {
            val success = recordingRepository.startRecording(context)
            if (success) {
                _recordingState.value = RecordingState(
                    isRecording = true,
                    fileName = recordingRepository.getCurrentFileName(),
                    message = "Recording started"
                )
                Log.i(TAG, "Recording started successfully")
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

    fun stopRecording(context: Context) {
        viewModelScope.launch {
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
        }
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
        _musicState.value = _musicState.value.copy(errorMessage = null)
    }
}

// Recording State
data class RecordingState(
    val isRecording: Boolean = false,
    val fileName: String? = null,
    val savedFile: File? = null,
    val message: String? = null
)

// Music State
data class MusicState(
    val selectedMusicUri: Uri? = null,
    val startDateTime: Calendar = Calendar.getInstance().apply { add(Calendar.MINUTE, 5) },
    val endDateTime: Calendar = Calendar.getInstance().apply { add(Calendar.HOUR, 1) },
    val isScheduled: Boolean = false,
    val errorMessage: String? = null
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