package com.samsung.health.mobile.presentation.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.samsung.health.data.TrackedData
import com.samsung.health.mobile.presentation.MusicState
import com.samsung.health.mobile.presentation.RecordingState
import com.samsung.health.mobile.presentation.StageState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    measurementResults: List<TrackedData>,
    recordingState: RecordingState,
    musicState: MusicState,
    stageState: StageState,
    currentProfileName: String,
    onStartRecording: (Context) -> Unit,
    onStopRecording: (Context) -> Unit,
    onSelectMusic: (Uri) -> Unit,
    onUpdateStartDate: (Calendar) -> Unit,
    onUpdateStartTime: (Calendar) -> Unit,
    onUpdateEndDate: (Calendar) -> Unit,
    onUpdateEndTime: (Calendar) -> Unit,
    onScheduleMusic: (Context) -> Unit,
    onCancelSchedule: (Context) -> Unit,
    onPlayNow: (Context) -> Unit,
    onStopNow: (Context) -> Unit,
    onNavigateToStageConfig: () -> Unit,
    onNavigateToProfileSelection: () -> Unit,
    onToggleML: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Music file picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                onSelectMusic(uri)
            }
        }
    }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Title
        Text(
            text = "Music + Data Collection",
            style = MaterialTheme.typography.headlineMedium
        )

        // === PROFILE SELECTION SECTION ===
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF3E5F5)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToProfileSelection)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🎼 Music Profile",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = currentProfileName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF6A1B9A),
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Select Profile",
                    tint = Color(0xFF6A1B9A)
                )
            }
        }

        // === STAGE STATUS SECTION ===
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE1F5FE)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🎚️ Current Stage",
                            style = MaterialTheme.typography.titleMedium
                        )
                        val currentStageConfig = stageState.stages.getOrNull(stageState.currentStage)
                        Text(
                            text = "Stage ${stageState.currentStage}: ${currentStageConfig?.stageName ?: "N/A"}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF0277BD)
                        )
                        currentStageConfig?.let {
                            Text(
                                text = "Volume: ${it.targetVolume}% | ${it.musicType}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onNavigateToStageConfig) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Configure Stages",
                            tint = Color(0xFF0277BD)
                        )
                    }
                }

                HorizontalDivider()

                // ML Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ML-Based Stage Control",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = stageState.isMLEnabled,
                        onCheckedChange = { onToggleML() }
                    )
                }

                if (stageState.isMLEnabled) {
                    Text(
                        "✓ ML model is analyzing your activity",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2E7D32)
                    )
                } else {
                    Text(
                        "⚠ Stage changes disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD84315)
                    )
                }
            }
        }

        // === MUSIC SECTION ===
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🎵 Music Player",
                    style = MaterialTheme.typography.titleLarge
                )

                // Music file selection
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "audio/*"
                        }
                        filePickerLauncher.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Music File")
                }

                musicState.selectedMusicUri?.let { uri ->
                    Text(
                        text = "Selected: ${uri.lastPathSegment ?: "Unknown"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1976D2)
                    )
                }

                HorizontalDivider()

                // Start time
                Text("Start Time:", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showStartDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(formatDate(musicState.startDateTime))
                    }

                    OutlinedButton(
                        onClick = { showStartTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(formatTime(musicState.startDateTime))
                    }
                }

                // End time
                Text("End Time:", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showEndDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(formatDate(musicState.endDateTime))
                    }

                    OutlinedButton(
                        onClick = { showEndTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(formatTime(musicState.endDateTime))
                    }
                }

                // Error message
                musicState.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                HorizontalDivider()

                // Music controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onPlayNow(context) },
                        modifier = Modifier.weight(1f),
                        enabled = musicState.selectedMusicUri != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Play Now")
                    }

                    Button(
                        onClick = { onStopNow(context) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5722)
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stop")
                    }
                }

                // Schedule button
                if (!musicState.isScheduled) {
                    Button(
                        onClick = { onScheduleMusic(context) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = musicState.selectedMusicUri != null
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Schedule Music")
                    }
                } else {
                    Column {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFC8E6C9)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Music scheduled!")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { onCancelSchedule(context) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cancel Schedule")
                        }
                    }
                }
            }
        }

        // === RECORDING SECTION ===
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (recordingState.isRecording)
                    Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (recordingState.isRecording) "🔴 Recording Data" else "⚪ Not Recording",
                    style = MaterialTheme.typography.titleLarge
                )

                recordingState.fileName?.let { fileName ->
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onStartRecording(context) },
                        enabled = !recordingState.isRecording,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Start Recording")
                    }

                    Button(
                        onClick = { onStopRecording(context) },
                        enabled = recordingState.isRecording,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336)
                        )
                    ) {
                        Text("Stop & Save")
                    }
                }
            }
        }

        // === LIVE DATA DISPLAY ===
        measurementResults.lastOrNull()?.let { data ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📊 Latest Sensor Data",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("❤️ Heart Rate: ${if (data.hr > 0) "${data.hr} bpm" else "N/A"}")

                    if (data.ibi.isNotEmpty()) {
                        Text("💓 IBI: ${data.ibi.joinToString(", ")}")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("📱 Accelerometer:")
                    Text("  X: ${"%.3f".format(data.accelX)}")
                    Text("  Y: ${"%.3f".format(data.accelY)}")
                    Text("  Z: ${"%.3f".format(data.accelZ)}")
                }
            }
        }

        // === INSTRUCTIONS ===
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF9C4)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "📖 How to Use:",
                    style = MaterialTheme.typography.titleMedium
                )
                Text("1. Tap '🎼 Music Profile' to choose a profile")
                Text("2. Enable 'ML-Based Stage Control' (toggle above)")
                Text("3. Tap ⚙️ to configure stages and add music files")
                Text("4. Select a music file or use profile defaults")
                Text("5. Set start and end times")
                Text("6. Either:")
                Text("   • Click 'Play Now' for immediate playback")
                Text("   • Click 'Schedule Music' for timed playback")
                Text("7. Music will auto-start data recording")
                Text("8. ML model analyzes your HR & activity")
                Text("9. Volume adjusts automatically to match stage")
                Text("10. Music switches based on your profile")
                Text("11. When done, music will auto-stop recording")
                Text("12. CSV file will be saved and shareable")
            }
        }
    }

    // Date/Time pickers
    if (showStartDatePicker) {
        DatePickerDialog(
            initialDate = musicState.startDateTime,
            onDateSelected = { calendar ->
                onUpdateStartDate(calendar)
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showStartTimePicker) {
        TimePickerDialog(
            initialTime = musicState.startDateTime,
            onTimeSelected = { calendar ->
                onUpdateStartTime(calendar)
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            initialDate = musicState.endDateTime,
            onDateSelected = { calendar ->
                onUpdateEndDate(calendar)
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            initialTime = musicState.endDateTime,
            onTimeSelected = { calendar ->
                onUpdateEndTime(calendar)
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    initialDate: Calendar,
    onDateSelected: (Calendar) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.timeInMillis
    )

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = millis
                    }
                    onDateSelected(calendar)
                }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: Calendar,
    onTimeSelected: (Calendar) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = initialTime.get(Calendar.MINUTE)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    set(Calendar.MINUTE, timePickerState.minute)
                }
                onTimeSelected(calendar)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}

fun formatDate(calendar: Calendar): String {
    val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return format.format(calendar.time)
}

fun formatTime(calendar: Calendar): String {
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(calendar.time)
}