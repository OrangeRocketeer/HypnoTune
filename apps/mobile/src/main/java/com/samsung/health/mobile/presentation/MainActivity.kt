package com.samsung.health.mobile.presentation

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.Wearable
import com.samsung.health.data.TrackedData
import com.samsung.health.mobile.ml.MLDebugUtils
import com.samsung.health.mobile.ml.MLModelInterface
import com.samsung.health.mobile.presentation.ui.CreateProfileDialog
import com.samsung.health.mobile.presentation.ui.MainScreen
import com.samsung.health.mobile.presentation.ui.ProfileSelectionScreen
import com.samsung.health.mobile.presentation.ui.StageConfigScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.samsung.health.mobile.presentation.ui.SleepMusicQuestionnaireScreen
import com.samsung.health.mobile.presentation.ui.MusicRecommendationResultScreen
import com.samsung.health.mobile.presentation.ui.QuestionnaireAnswers

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val messageClient by lazy { Wearable.getMessageClient(this) }

    // ========================================
    // 🔥 NEW: Inject ML Model Interface
    // ========================================
    @Inject
    lateinit var mlModelInterface: MLModelInterface
    // ========================================

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

        // ========================================
        // 🔥 NEW: Initialize ML Model
        // ========================================
        initializeMLModel()
        // ========================================

        // Initialize stages and profiles
        viewModel.initializeStages(this)

        // Test ML model (optional, for debugging)
        viewModel.testMLModel(this)

        // Register data update receiver (backward compatibility - can be removed later)
        registerDataUpdateReceiver()

        Log.i(TAG, "✅ MainActivity initialized - LiveData uses StateFlow (no broadcasts needed)")

        setContent {
            var showStageConfig by remember { mutableStateOf(false) }
            var showProfileSelection by remember {
                mutableStateOf(intent.getBooleanExtra("open_profile_selection", false))
            }
            var showCreateProfile by remember { mutableStateOf(false) }
            var showQuestionnaire by remember { mutableStateOf(false) }
            var showQuestionnaireResults by remember { mutableStateOf(false) }
            var questionnaireAnswers by remember { mutableStateOf<QuestionnaireAnswers?>(null) }

            MaterialTheme {
                val recordingState by viewModel.recordingState.collectAsState()
                val stageState by viewModel.stageState.collectAsState()
                val profileState by viewModel.profileState.collectAsState()
                val reminderState by viewModel.reminderState.collectAsState()
                val liveWatchData by viewModel.liveDataState.collectAsState()

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
                    showQuestionnaire -> {
                        SleepMusicQuestionnaireScreen(
                            onNavigateToResults = { answers ->
                                questionnaireAnswers = answers
                                showQuestionnaire = false
                                showQuestionnaireResults = true
                            },
                            onBack = {
                                showQuestionnaire = false
                            }
                        )
                    }
                    showQuestionnaireResults -> {
                        questionnaireAnswers?.let { answers ->
                            MusicRecommendationResultScreen(
                                answers = answers,
                                onStartListening = {
                                    // Go back to main screen
                                    showQuestionnaireResults = false
                                    questionnaireAnswers = null
                                },
                                onRetakeQuestionnaire = {
                                    showQuestionnaireResults = false
                                    showQuestionnaire = true
                                },
                                onBack = {
                                    showQuestionnaireResults = false
                                    questionnaireAnswers = null
                                }
                            )
                        }
                    }
                    else -> {
                        MainScreen(
                            recordingState = recordingState,
                            stageState = stageState,
                            reminderState = reminderState,
                            liveDataState = liveWatchData,
                            currentProfileName = profileState.currentProfile?.name ?: "No Profile",
                            onStartRecordingAndMusic = { viewModel.startRecordingAndMusic(it) },
                            onStopRecordingAndMusic = { viewModel.stopRecordingAndMusic(it) },
                            onNavigateToStageConfig = {
                                showStageConfig = true
                            },
                            onNavigateToProfileSelection = {
                                showProfileSelection = true
                            },
                            onNavigateToQuestionnaire = {
                                showQuestionnaire = true
                            },
                            onDismissReminder = {
                                viewModel.dismissReminder()
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

    // ========================================
    // 🔥 NEW: ML Model Initialization
    // ========================================
    private fun initializeMLModel() {
        Log.i(TAG, "========================================")
        Log.i(TAG, "🚀 Initializing ML Model...")
        Log.i(TAG, "========================================")

        try {
            // Load the ONNX model
            mlModelInterface.loadModel(this)

            // Check if model loaded successfully
            if (mlModelInterface.isModelReady()) {
                Log.i(TAG, "✅ ML Model loaded and ready!")

                // Optional: Run test pipeline (comment out for production)
                testMLPipelineOptional()

            } else {
                Log.e(TAG, "❌ ML Model failed to load!")
                Toast.makeText(
                    this,
                    "ML Model failed to load. Check logs.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error initializing ML model", e)
            Toast.makeText(
                this,
                "Error loading ML model: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }

        Log.i(TAG, "========================================")
    }

    /**
     * Optional: Test ML pipeline with synthetic data
     * Remove or comment this out in production
     */
    private fun testMLPipelineOptional() {
        lifecycleScope.launch {
            try {
                Log.i(TAG, "🧪 Running ML Pipeline test...")

                // Run the test
                val testResult = MLDebugUtils.testPipeline(mlModelInterface)

                if (testResult.success) {
                    Log.i(TAG, "========================================")
                    Log.i(TAG, "✅ PIPELINE TEST SUCCESS!")
                    Log.i(TAG, "========================================")
                    Log.i(TAG, "Predicted Stage: ${testResult.predictedStage}")
                    Log.i(TAG, "Confidence: ${(testResult.confidence * 100).toInt()}%")
                    Log.i(TAG, "Buffer Size: ${testResult.bufferSize} samples")
                    Log.i(TAG, "Epoch History: ${testResult.epochHistory} epochs")
                    Log.i(TAG, "Interpolation: ${(testResult.interpolationRate * 100).toInt()}%")
                    Log.i(TAG, "Duration: ${testResult.durationMs}ms")
                    Log.i(TAG, "========================================")

                    // Optional: Show success toast
                    Toast.makeText(
                        this@MainActivity,
                        "✅ ML Pipeline Test Passed!",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    Log.e(TAG, "❌ Pipeline test failed!")
                    Toast.makeText(
                        this@MainActivity,
                        "❌ ML Pipeline Test Failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Pipeline test error", e)
            }
        }
    }
    // ========================================

    private fun registerDataUpdateReceiver() {
        dataUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val message = intent?.getStringExtra("message")
                if (message != null && message.isNotEmpty()) {
                    try {
                        val newResults = HelpFunctions.decodeMessage(message)
                        measurementResults.clear()
                        measurementResults.addAll(newResults)

                        Log.d(TAG, "Updated measurement results (${newResults.size} items)")
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
        setIntent(intent)
        Log.i(TAG, "onNewIntent: ${intent.action}")

        // Handle intent extras if needed
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

        // Handle reminder notification click
        if (intent.getBooleanExtra("open_profile_selection", false)) {
            Log.i(TAG, "Opened from profile reminder notification")
            recreate()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "MainActivity onResume")

        // Check for reminder when app is resumed
        viewModel.checkAndShowReminder(this)
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "MainActivity onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "MainActivity onDestroy")

        // ========================================
        // 🔥 NEW: Clean up ML resources
        // ========================================
        try {
            mlModelInterface.cleanup()
            Log.i(TAG, "ML model resources cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up ML model", e)
        }
        // ========================================

        // Unregister data receiver
        dataUpdateReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering data receiver", e)
            }
        }
    }
}