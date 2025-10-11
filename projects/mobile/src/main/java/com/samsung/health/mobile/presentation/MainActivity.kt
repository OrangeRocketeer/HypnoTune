package com.samsung.health.mobile.presentation

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.google.android.gms.wearable.Wearable
import com.samsung.health.data.TrackedData
import com.samsung.health.mobile.presentation.ui.CreateProfileDialog
import com.samsung.health.mobile.presentation.ui.MainScreen
import com.samsung.health.mobile.presentation.ui.ProfileSelectionScreen
import com.samsung.health.mobile.presentation.ui.StageConfigScreen
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val messageClient by lazy { Wearable.getMessageClient(this) }

    private var measurementResults = mutableStateListOf<TrackedData>()
    private var dataUpdateReceiver: BroadcastReceiver? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Log.i(TAG, "All permissions granted")
        } else {
            Log.w(TAG, "Some permissions denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "MainActivity onCreate")

        // Request necessary permissions
        checkAndRequestPermissions()

        // Register music receivers
        viewModel.registerMusicReceivers(this)

        // Initialize stages and profiles
        viewModel.initializeStages(this)

        // Register data update receiver
        registerDataUpdateReceiver()

        setContent {
            var showStageConfig by remember { mutableStateOf(false) }
            var showProfileSelection by remember { mutableStateOf(false) }
            var showCreateProfile by remember { mutableStateOf(false) }

            MaterialTheme {
                val recordingState by viewModel.recordingState.collectAsState()
                val musicState by viewModel.musicState.collectAsState()
                val stageState by viewModel.stageState.collectAsState()
                val profileState by viewModel.profileState.collectAsState()

                when {
                    showProfileSelection -> {
                        ProfileSelectionScreen(
                            profiles = profileState.allProfiles,
                            currentProfileId = profileState.currentProfile?.id,
                            onProfileSelect = { profile ->
                                viewModel.applyProfile(profile)
                                showProfileSelection = false
                            },
                            onCreateCustom = {
                                showCreateProfile = true
                            },
                            onDeleteProfile = { profileId ->
                                viewModel.deleteCustomProfile(profileId)
                            },
                            onBack = {
                                showProfileSelection = false
                            }
                        )
                    }
                    showStageConfig -> {
                        StageConfigScreen(
                            stages = stageState.stages,
                            currentStage = stageState.currentStage,
                            onStageUpdate = { stage ->
                                viewModel.updateStage(stage)
                            },
                            onResetDefaults = {
                                viewModel.resetStagesToDefaults()
                            },
                            onBack = {
                                showStageConfig = false
                            },
                            onMusicSelected = { stageNumber, uri ->
                                viewModel.updateStageMusicUri(stageNumber, uri)
                            }
                        )
                    }
                    else -> {
                        MainScreen(
                            measurementResults = measurementResults,
                            recordingState = recordingState,
                            musicState = musicState,
                            stageState = stageState,
                            currentProfileName = profileState.currentProfile?.name ?: "No Profile",
                            onStartRecording = { viewModel.startRecording(it) },
                            onStopRecording = { viewModel.stopRecording(it) },
                            onSelectMusic = { viewModel.setMusicFile(it) },
                            onUpdateStartDate = { viewModel.updateStartDate(it) },
                            onUpdateStartTime = { viewModel.updateStartTime(it) },
                            onUpdateEndDate = { viewModel.updateEndDate(it) },
                            onUpdateEndTime = { viewModel.updateEndTime(it) },
                            onScheduleMusic = { viewModel.scheduleMusic(it) },
                            onCancelSchedule = { viewModel.cancelSchedule(it) },
                            onPlayNow = { viewModel.playMusicNow(it) },
                            onStopNow = { viewModel.stopMusicNow(it) },
                            onNavigateToStageConfig = {
                                showStageConfig = true
                            },
                            onNavigateToProfileSelection = {
                                showProfileSelection = true
                            },
                            onToggleML = {
                                viewModel.toggleMLEnabled()
                            }
                        )
                    }
                }

                // Create Profile Dialog
                if (showCreateProfile) {
                    CreateProfileDialog(
                        onDismiss = {
                            showCreateProfile = false
                        },
                        onConfirm = { name, description ->
                            viewModel.saveAsCustomProfile(name, description)
                            showCreateProfile = false
                            showProfileSelection = false
                        }
                    )
                }
            }
        }
    }

    private fun registerDataUpdateReceiver() {
        dataUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val message = intent?.getStringExtra("message")
                if (message != null && message.isNotEmpty()) {
                    try {
                        val newResults = HelpFunctions.decodeMessage(message)
                        measurementResults.clear()
                        measurementResults.addAll(newResults)

                        // Update current stage in ViewModel based on latest data
                        newResults.lastOrNull()?.let { data ->
                            // Stage will be updated by DataListenerService via ML prediction
                            Log.d(TAG, "Updated measurement results: HR=${data.hr}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error decoding message", e)
                    }
                }
            }
        }

        val filter = IntentFilter("com.samsung.health.mobile.DATA_UPDATED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(dataUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            ContextCompat.registerReceiver(
                this,
                dataUpdateReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        Log.i(TAG, "Data update receiver registered")
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()

        // Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Audio permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
        }

        // External storage permissions (Android 12 and below)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }

        if (permissions.isNotEmpty()) {
            Log.i(TAG, "Requesting permissions: $permissions")
            requestPermissionLauncher.launch(permissions.toTypedArray())
        } else {
            Log.i(TAG, "All permissions already granted")
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i(TAG, "onNewIntent: ${intent.action}")

        // Handle any intent extras if needed
        intent.getStringExtra("message")?.let { message ->
            if (message.isNotEmpty()) {
                try {
                    val newResults = HelpFunctions.decodeMessage(message)
                    measurementResults.clear()
                    measurementResults.addAll(newResults)
                } catch (e: Exception) {
                    Log.e(TAG, "Error decoding message from intent", e)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "MainActivity onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "MainActivity onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "MainActivity onDestroy")

        // Unregister receivers
        viewModel.unregisterMusicReceivers(this)
        dataUpdateReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering data receiver", e)
            }
        }
    }
}