package com.samsung.health.mobile.presentation.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samsung.health.mobile.presentation.RecordingState
import com.samsung.health.mobile.presentation.StageState
import com.samsung.health.mobile.presentation.ReminderState
import com.samsung.health.mobile.data.LiveWatchData

// Dark theme colors
private val DarkBackground = Color(0xFF0F0F0F)
private val CardBackground = Color(0xFF1C1C1E)
private val PurpleStart = Color(0xFF6B4CE6)
private val PurpleEnd = Color(0xFF9B72FF)
private val GreenPrimary = Color(0xFF34C759)
private val GreenDark = Color(0xFF248A3D)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF8E8E93)
private val AccentBlue = Color(0xFF5E5CE6)
private val AccentGreen = Color(0xFF34C759)

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
    onNavigateToQuestionnaire: () -> Unit,
    onDismissReminder: () -> Unit,
    onToggleML: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // App Header Card
            AppHeaderCard()

            SleepQuestionnaireCard(onClick = onNavigateToQuestionnaire)
            // Music Profile Card
            MusicProfileCard(
                profileName = currentProfileName,
                onClick = onNavigateToProfileSelection
            )

            // Profile Freshness Tracker
            ProfileFreshnessCard(
                daysSinceLastChange = reminderState.daysSinceLastChange,
                daysUntilReminder = reminderState.daysUntilReminder,
                shouldShowReminder = reminderState.shouldShowReminder,
                onDismiss = onDismissReminder
            )

            // Current Stage Display
            CurrentStageCard(
                stageState = stageState,
                onNavigateToConfig = onNavigateToStageConfig
            )

            // AI Stage Control
            AIStageControlCard(
                isEnabled = stageState.isMLEnabled,
                onToggle = onToggleML
            )

            // Recording Control
            RecordingControlCard(
                isRecording = recordingState.isRecording,
                onStart = { onStartRecordingAndMusic(context) },
                onStop = { onStopRecordingAndMusic(context) }
            )

            // How to Use
            HowToUseCard()

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun AppHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(PurpleStart, PurpleEnd)
                    )
                )
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    "HypnoTune",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Smart Sleep Music Therapy",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
fun MusicProfileCard(
    profileName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(AccentBlue.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LibraryMusic,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Music Profile",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        profileName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ProfileFreshnessCard(
    daysSinceLastChange: Int,
    daysUntilReminder: Int,
    shouldShowReminder: Boolean,
    onDismiss: () -> Unit
) {
    var showInfoDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (shouldShowReminder) Color(0xFF3A2D2B) else Color(0xFF2D4A2B)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (shouldShowReminder) Color(0xFFFF9500).copy(alpha = 0.3f)
                                else GreenPrimary.copy(alpha = 0.3f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (shouldShowReminder) Icons.Default.Warning else Icons.Default.Celebration,
                            contentDescription = null,
                            tint = if (shouldShowReminder) Color(0xFFFF9500) else GreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Profile Freshness",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Info Icon Button
                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Info",
                            tint = AccentBlue.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (shouldShowReminder) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                if (shouldShowReminder) "Time to change your profile!"
                else "$daysUntilReminder days until reminder",
                fontSize = 14.sp,
                color = if (shouldShowReminder) Color(0xFFFF9500) else GreenPrimary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Day $daysSinceLastChange of 21",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (shouldShowReminder) Color(0xFF3A2A1A) else Color(0xFF1A3A1A)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(
                            fraction = if (daysSinceLastChange > 0) daysSinceLastChange / 21f else 0f
                        )
                        .fillMaxHeight()
                        .background(
                            if (shouldShowReminder) Color(0xFFFF9500) else GreenPrimary
                        )
                )
            }
        }
    }

    // Info Dialog
    if (showInfoDialog) {
        EarwormInfoDialog(onDismiss = { showInfoDialog = false })
    }
}

@Composable
fun EarwormInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(AccentBlue.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                "About Profile Freshness",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "What is the Earworm Phenomenon?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentBlue
                )
                Text(
                    "An 'earworm' is when a song gets stuck in your head. Listening to the same music for 21+ days can trigger this.",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "Why 21 Days?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentBlue
                )
                Text(
                    "Research shows repetitive exposure for 3 weeks can cause the brain to 'lock in' melodies.",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "Solution",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentGreen
                )
                Text(
                    "Change your profile every 21 days to keep your listening fresh!",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlue
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Got it!", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@Composable
fun CurrentStageCard(
    stageState: StageState,
    onNavigateToConfig: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(AccentBlue.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            "Current Stage",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            stageState.stages.getOrNull(stageState.currentStage)?.stageName ?: "Rest",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }

                IconButton(
                    onClick = onNavigateToConfig,
                    modifier = Modifier
                        .size(40.dp)
                        .background(AccentBlue.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Volume", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${stageState.stages.getOrNull(stageState.currentStage)?.targetVolume ?: 30}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(TextSecondary.copy(alpha = 0.2f))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Type", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        stageState.stages.getOrNull(stageState.currentStage)?.musicType ?: "Ambient",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun AIStageControlCard(
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            if (isEnabled) GreenPrimary.copy(alpha = 0.2f)
                            else TextSecondary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Psychology,
                        contentDescription = null,
                        tint = if (isEnabled) GreenPrimary else TextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        "AI Stage Control",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (isEnabled) "Active & Learning" else "Inactive",
                        fontSize = 13.sp,
                        color = if (isEnabled) GreenPrimary else TextSecondary
                    )
                }
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AccentBlue,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = TextSecondary.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun RecordingControlCard(
    isRecording: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                if (isRecording) "System Active" else "Ready to Record",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(
                        if (isRecording) {
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF5E5CE6), Color(0xFF4A48D4))
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(GreenPrimary, GreenDark)
                            )
                        }
                    )
                    .clickable { if (isRecording) onStop() else onStart() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }

            Button(
                onClick = { if (isRecording) onStop() else onStart() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) AccentBlue else GreenPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    if (isRecording) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isRecording) "Stop" else "Start",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun HowToUseCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D2A3A)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(AccentBlue.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "How to Use:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            HowToStep("1", Icons.Default.LibraryMusic, "Select a music profile")
            HowToStep("2", Icons.Default.Tune, "Configure stages & music files")
            HowToStep("3", Icons.Default.Watch, "Connect your Galaxy Watch")
            HowToStep("4", Icons.Default.PlayArrow, "Start recording & enjoy!")
        }
    }
}

@Composable
fun HowToStep(
    number: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(AccentBlue.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                number,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AccentBlue
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text,
            fontSize = 14.sp,
            color = TextPrimary
        )
    }
}
@Composable
fun SleepQuestionnaireCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF6B4CE6).copy(alpha = 0.3f),
                            Color(0xFF9B72FF).copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFF5E5CE6).copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Quiz,
                            contentDescription = null,
                            tint = Color(0xFF5E5CE6),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Find Your Perfect Music",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFFFFF)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Take a quick questionnaire",
                            fontSize = 13.sp,
                            color = Color(0xFF8E8E93)
                        )
                    }
                }
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF5E5CE6),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}