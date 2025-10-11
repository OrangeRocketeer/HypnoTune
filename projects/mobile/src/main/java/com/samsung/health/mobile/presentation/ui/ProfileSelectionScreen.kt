package com.samsung.health.mobile.presentation.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.samsung.health.mobile.data.MusicProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSelectionScreen(
    profiles: List<MusicProfile>,
    currentProfileId: String?,
    onProfileSelect: (MusicProfile) -> Unit,
    onCreateCustom: () -> Unit,
    onDeleteProfile: (String) -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Music Profiles") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateCustom,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Create Custom Profile") }
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
            Text(
                "Choose a music profile to customize your workout experience",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Pre-built profiles
            Text(
                "PRE-BUILT PROFILES",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            profiles.filter { !it.isCustom }.forEach { profile ->
                ProfileCard(
                    profile = profile,
                    isSelected = profile.id == currentProfileId,
                    onSelect = { onProfileSelect(profile) },
                    onDelete = null
                )
            }

            // Custom profiles
            val customProfiles = profiles.filter { it.isCustom }
            if (customProfiles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "CUSTOM PROFILES",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                customProfiles.forEach { profile ->
                    ProfileCard(
                        profile = profile,
                        isSelected = profile.id == currentProfileId,
                        onSelect = { onProfileSelect(profile) },
                        onDelete = { showDeleteDialog = profile.id }
                    )
                }
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
                        Text("About Profiles", style = MaterialTheme.typography.titleMedium)
                    }
                    Text("• Each profile has unique volume levels and fade durations")
                    Text("• Select a profile to apply it immediately")
                    Text("• Customize any profile by editing stages")
                    Text("• Save your customizations as a new profile")
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { profileId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Profile?") },
            text = { Text("This custom profile will be permanently deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProfile(profileId)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileCard(
    profile: MusicProfile,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        profile.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    if (profile.isCustom) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFF2196F3),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                "CUSTOM",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    profile.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Show volume range
                val minVolume = profile.stages.minOfOrNull { it.targetVolume } ?: 0
                val maxVolume = profile.stages.maxOfOrNull { it.targetVolume } ?: 100
                Text(
                    "Volume: $minVolume% - $maxVolume%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}