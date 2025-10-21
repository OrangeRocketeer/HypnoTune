package com.samsung.health.mobile.presentation.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import com.samsung.health.mobile.data.StageConfig

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stage Configuration") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onResetDefaults) {
                        Icon(Icons.Default.Refresh, "Reset to Defaults")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current stage indicator
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF1976D2))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Current Stage", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "Stage $currentStage: ${stages.getOrNull(currentStage)?.stageName ?: "Unknown"}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF1976D2)
                        )
                    }
                }
            }

            Text(
                "Configure each stage's music playback settings:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Stage configuration cards
            stages.forEach { stage ->
                StageConfigCard(
                    stage = stage,
                    isActive = stage.stageNumber == currentStage,
                    onUpdate = onStageUpdate,
                    onMusicSelected = { uri -> onMusicSelected(stage.stageNumber, uri) }
                )
            }

            // Info card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF9C4)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("How it works", style = MaterialTheme.typography.titleMedium)
                    }
                    Text("• ML model analyzes your HR and activity data")
                    Text("• Determines your current activity stage (0-4)")
                    Text("• Switches to that stage's music file")
                    Text("• Music volume fades smoothly to match the stage")
                    Text("• Each stage can have different music and volume")
                    Text("• Fade duration controls transition smoothness")
                }
            }
        }
    }
}

@Composable
fun StageConfigCard(
    stage: StageConfig,
    isActive: Boolean,
    onUpdate: (StageConfig) -> Unit,
    onMusicSelected: (Uri) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var editedStage by remember { mutableStateOf(stage) }
    val context = LocalContext.current

    // Music file picker launcher
    val musicPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Take persistable permission
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

    // Update editedStage when stage prop changes
    LaunchedEffect(stage) {
        editedStage = stage
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isActive) {
                        Icon(
                            Icons.Default.Circle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Column {
                        Text(
                            "Stage ${stage.stageNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            stage.stageName,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }

            // Summary (always visible)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Volume: ${stage.targetVolume}%", style = MaterialTheme.typography.bodySmall)
                Text("Fade: ${stage.fadeDuration}ms", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "Music: ${stage.musicType}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Show selected music file
            if (stage.musicUri != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        Uri.parse(stage.musicUri).lastPathSegment ?: "Music selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50)
                    )
                }
            } else {
                Text(
                    "⚠ No music file selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD84315)
                )
            }

            // Expanded edit section
            if (expanded) {
                HorizontalDivider()

                // Music Selection Button
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "audio/*"
                        }
                        musicPickerLauncher.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (editedStage.musicUri != null) "Change Music File" else "Select Music File")
                }

                if (editedStage.musicUri != null) {
                    Text(
                        "Selected: ${Uri.parse(editedStage.musicUri).lastPathSegment}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1976D2)
                    )
                }

                // Stage Name
                OutlinedTextField(
                    value = editedStage.stageName,
                    onValueChange = { editedStage = editedStage.copy(stageName = it) },
                    label = { Text("Stage Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Volume Slider
                Column {
                    Text("Target Volume: ${editedStage.targetVolume}%")
                    Slider(
                        value = editedStage.targetVolume.toFloat(),
                        onValueChange = { editedStage = editedStage.copy(targetVolume = it.toInt()) },
                        valueRange = 0f..100f,
                        steps = 19 // Creates stops at every 5%
                    )
                }

                // Music Type
                OutlinedTextField(
                    value = editedStage.musicType,
                    onValueChange = { editedStage = editedStage.copy(musicType = it) },
                    label = { Text("Music Type / Playlist") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Calm Ambient, High Energy") }
                )

                // Fade Duration
                Column {
                    Text("Fade Duration: ${editedStage.fadeDuration}ms (${editedStage.fadeDuration / 1000.0}s)")
                    Slider(
                        value = editedStage.fadeDuration.toFloat(),
                        onValueChange = { editedStage = editedStage.copy(fadeDuration = it.toInt()) },
                        valueRange = 500f..5000f,
                        steps = 17 // Creates stops at every 250ms
                    )
                }

                // Save button
                Button(
                    onClick = {
                        onUpdate(editedStage)
                        expanded = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Changes")
                }
            }
        }
    }
}