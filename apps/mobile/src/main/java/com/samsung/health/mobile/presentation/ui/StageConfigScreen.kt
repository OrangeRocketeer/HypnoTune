package com.samsung.health.mobile.presentation.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samsung.health.mobile.data.StageConfig

private val DarkBackground = Color(0xFF0F0F0F)
private val CardBackground = Color(0xFF1C1C1E)
private val AccentBlue = Color(0xFF5E5CE6)
private val AccentGreen = Color(0xFF34C759)
private val AccentOrange = Color(0xFFFF9500)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF8E8E93)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StageConfigScreen(
    stages: List<StageConfig>,
    currentStage: Int,
    onStageUpdate: (StageConfig) -> Unit,
    onResetDefaults: () -> Unit,
    onBack: () -> Unit,
    onMusicSelected: (Int, Uri) -> Unit
) {
    val scrollState = rememberScrollState()
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Configure Stages",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { showResetDialog = true }) {
                        Text(
                            "Reset",
                            color = AccentOrange,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stage cards
            stages.forEach { stage ->
                CleanStageCard(
                    stage = stage,
                    isActive = stage.stageNumber == currentStage,
                    onUpdate = onStageUpdate,
                    onMusicSelected = { uri -> onMusicSelected(stage.stageNumber, uri) }
                )
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // Reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = CardBackground,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    "Reset to Defaults?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "This will reset all stages to their default settings.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetDefaults()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = AccentOrange, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = AccentBlue, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

@Composable
fun CleanStageCard(
    stage: StageConfig,
    isActive: Boolean,
    onUpdate: (StageConfig) -> Unit,
    onMusicSelected: (Uri) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var editedStage by remember { mutableStateOf(stage) }
    val context = LocalContext.current

    val musicPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    editedStage = editedStage.copy(musicUri = uri.toString())
                    onMusicSelected(uri)
                } catch (e: Exception) {
                    android.util.Log.e("StageConfigCard", "Error taking permission", e)
                }
            }
        }
    }

    LaunchedEffect(stage) {
        editedStage = stage
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) AccentBlue.copy(alpha = 0.15f) else CardBackground
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isActive) {
            androidx.compose.foundation.BorderStroke(1.5.dp, AccentBlue)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (isActive) AccentBlue.copy(alpha = 0.3f)
                                else AccentBlue.copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${stage.stageNumber}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) AccentBlue else TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            stage.stageName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "${stage.targetVolume}%",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                "•",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                "${stage.fadeDuration / 1000.0}s fade",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = TextPrimary
                    )
                }
            }

            // Music status
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    if (stage.musicUri != null) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (stage.musicUri != null) AccentGreen else AccentOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    if (stage.musicUri != null) {
                        Uri.parse(stage.musicUri).lastPathSegment ?: "Music selected"
                    } else {
                        "No music file selected"
                    },
                    fontSize = 12.sp,
                    color = if (stage.musicUri != null) AccentGreen else AccentOrange,
                    maxLines = 1
                )
            }

            // Expanded edit section
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = TextSecondary.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(16.dp))

                // Music Selection Button
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "audio/*"
                        }
                        musicPickerLauncher.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AccentBlue
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(AccentBlue.copy(alpha = 0.5f))
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (editedStage.musicUri != null) "Change Music" else "Select Music",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stage Name
                OutlinedTextField(
                    value = editedStage.stageName,
                    onValueChange = { editedStage = editedStage.copy(stageName = it) },
                    label = { Text("Stage Name", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentBlue,
                        unfocusedLabelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Volume Slider
                Column {
                    Text(
                        "Volume",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = editedStage.targetVolume.toFloat(),
                            onValueChange = { editedStage = editedStage.copy(targetVolume = it.toInt()) },
                            valueRange = 0f..100f,
                            steps = 19,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = AccentBlue,
                                activeTrackColor = AccentBlue,
                                inactiveTrackColor = TextSecondary.copy(alpha = 0.2f)
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "${editedStage.targetVolume}%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.width(50.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Music Type
                OutlinedTextField(
                    value = editedStage.musicType,
                    onValueChange = { editedStage = editedStage.copy(musicType = it) },
                    label = { Text("Music Type", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Calm Ambient", fontSize = 14.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentBlue,
                        unfocusedLabelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Fade Duration
                Column {
                    Text(
                        "Fade Duration",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = editedStage.fadeDuration.toFloat(),
                            onValueChange = { editedStage = editedStage.copy(fadeDuration = it.toInt()) },
                            valueRange = 500f..5000f,
                            steps = 17,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = AccentBlue,
                                activeTrackColor = AccentBlue,
                                inactiveTrackColor = TextSecondary.copy(alpha = 0.2f)
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "${editedStage.fadeDuration / 1000.0}s",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.width(50.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save button
                Button(
                    onClick = {
                        onUpdate(editedStage)
                        expanded = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Save Changes",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}