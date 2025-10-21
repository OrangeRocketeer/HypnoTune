package com.samsung.health.mobile.presentation.ui

import android.content.Context
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
import com.samsung.health.mobile.data.LiveWatchData
import com.samsung.health.mobile.presentation.RecordingState
import com.samsung.health.mobile.presentation.ReminderState
import com.samsung.health.mobile.presentation.StageState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    recordingState: RecordingState,
    stageState: StageState,
    reminderState: ReminderState,
    liveDataState: LiveWatchData,
    currentProfileName: String,
    onStartRecordingAndMusic: (Context) -> Unit,
    onStopRecordingAndMusic: (Context) -> Unit,
    onNavigateToStageConfig: () -> Unit,
    onNavigateToProfileSelection: () -> Unit,
    onDismissReminder: () -> Unit,
    onToggleML: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

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
            text = "🎵 Watch-Controlled Music",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
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

        // === REMINDER BANNER ===
        if (reminderState.shouldShowReminder) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFE082)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "⚠️ CHANGE PROFILE TO AVOID EARWORM",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "It's been ${reminderState.daysSinceLastChange} days since you changed profiles!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6D4C41)
                        )
                    }
                    IconButton(onClick = onDismissReminder) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color(0xFFE65100)
                        )
                    }
                }
            }
        } else {
            // Show progress towards next reminder
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📅 Profile Freshness",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${reminderState.daysUntilReminder} days until reminder",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    LinearProgressIndicator(
                        progress = 1f - (reminderState.daysUntilReminder / 21f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        color = Color(0xFF4CAF50),
                        trackColor = Color(0xFFC8E6C9)
                    )
                    Text(
                        text = "Day ${reminderState.daysSinceLastChange} of 21",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF558B2F),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // === LIVE WATCH DATA & ML PREDICTION CARD ===
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (liveDataState.isReceivingData)
                    Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (liveDataState.isReceivingData) "📊 Live Watch Data" else "📊 Waiting for Watch...",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // Connection status indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Blinking dot
                            if (liveDataState.isReceivingData && !liveDataState.isDataStale()) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = Color(0xFF4CAF50),
                                    modifier = Modifier.size(8.dp)
                                ) {}
                            } else {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = Color(0xFFFF9800),
                                    modifier = Modifier.size(8.dp)
                                ) {}
                            }
                        }
                        Text(
                            text = if (liveDataState.isDataStale()) "Stale" else "Live",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (liveDataState.isDataStale()) Color(0xFFFF9800) else Color(0xFF4CAF50)
                        )
                    }
                }

                HorizontalDivider()

                // Sensor data section
                if (liveDataState.trackedData != null) {
                    val data = liveDataState.trackedData

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Sensor Readings",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("❤️ Heart Rate:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                if (data.hr > 0) "${data.hr} bpm" else "N/A",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("📱 Accel X:", style = MaterialTheme.typography.bodySmall)
                            Text("${"%.3f".format(data.accelX)} m/s²", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("📱 Accel Y:", style = MaterialTheme.typography.bodySmall)
                            Text("${"%.3f".format(data.accelY)} m/s²", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("📱 Accel Z:", style = MaterialTheme.typography.bodySmall)
                            Text("${"%.3f".format(data.accelZ)} m/s²", style = MaterialTheme.typography.bodySmall)
                        }

                        if (data.ibi.isNotEmpty()) {
                            Text(
                                "💓 IBI: ${data.ibi.take(5).joinToString(", ")}${if (data.ibi.size > 5) "..." else ""} ms",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider()

                    // ML Prediction section
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "🤖 ML Prediction",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Stage ${liveDataState.predictedStage}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1976D2)
                                )
                                Text(
                                    text = "Confidence: ${(liveDataState.confidence * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (liveDataState.confidence >= 0.5f) Color(0xFF388E3C) else Color(0xFFFF9800)
                                )
                            }

                            // Volume indicator - show a placeholder for now
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "🎵 Music",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = if (recordingState.isRecording) "Playing" else "Stopped",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1976D2)
                                )
                            }
                        }

                        // Confidence bar
                        LinearProgressIndicator(
                            progress = liveDataState.confidence,
                            modifier = Modifier.fillMaxWidth(),
                            color = if (liveDataState.confidence >= 0.5f) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            trackColor = Color(0xFFE0E0E0)
                        )
                    }

                    // Last update timestamp
                    Text(
                        text = "⏱️ Updated ${liveDataState.getSecondsSinceUpdate()}s ago",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                } else {
                    // No data yet
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Watch,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFBDBDBD)
                        )
                        Text(
                            text = "Waiting for watch data...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF757575)
                        )
                        Text(
                            text = "Make sure your Galaxy Watch is connected",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
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
                            text = "🎚️ Stage Configuration",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${stageState.stages.size} stages configured",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "ML-Based Stage Control",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (stageState.isMLEnabled)
                                "Music adapts to your activity"
                            else
                                "Stage changes disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = stageState.isMLEnabled,
                        onCheckedChange = { onToggleML() }
                    )
                }
            }
        }

        // === MAIN CONTROL SECTION (START/STOP) ===
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (recordingState.isRecording) {
                        Icon(
                            Icons.Default.FiberManualRecord,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = if (recordingState.isRecording) "🔴 System Active" else "⚪ System Inactive",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (recordingState.isRecording) {
                    Text(
                        text = "Recording data & playing adaptive music",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    recordingState.fileName?.let { fileName ->
                        Text(
                            text = "📄 $fileName",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1976D2)
                        )
                    }
                }

                // Single start/stop button
                Button(
                    onClick = {
                        if (recordingState.isRecording) {
                            onStopRecordingAndMusic(context)
                        } else {
                            onStartRecordingAndMusic(context)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (recordingState.isRecording)
                            Color(0xFFF44336)
                        else
                            Color(0xFF4CAF50)
                    )
                ) {
                    Icon(
                        if (recordingState.isRecording) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (recordingState.isRecording)
                            "STOP Recording & Music"
                        else
                            "START Recording & Music",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Status message
                recordingState.message?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (message.contains("Failed")) Color.Red else Color(0xFF388E3C)
                    )
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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("1. 🎼 Select a music profile (tap profile card)")
                Text("2. ⚙️ Configure stages and add music files")
                Text("3. 🔛 Enable ML-Based Stage Control")
                Text("4. ⏺️ Press START to activate the system")
                Text("5. 🎵 Music plays and adapts to your activity")
                Text("6. 📊 Watch live data and ML predictions")
                Text("7. 🛑 Press STOP when done")
                Text("8. 💾 Your data is saved and shareable")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 The system automatically records data and adjusts music based on your heart rate and movement.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6D4C41),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}