package com.samsung.health.mobile.presentation.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samsung.health.mobile.data.MusicProfile

private val DarkBackground = Color(0xFF0F0F0F)
private val CardBackground = Color(0xFF1C1C1E)
private val AccentBlue = Color(0xFF5E5CE6)
private val AccentGreen = Color(0xFF34C759)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF8E8E93)

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
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Music Profiles",
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
            // Pre-built profiles
            profiles.filter { !it.isCustom }.forEach { profile ->
                CleanProfileCard(
                    profile = profile,
                    isSelected = profile.id == currentProfileId,
                    onSelect = { onProfileSelect(profile) },
                    onDelete = null
                )
            }

            // Custom profiles section
            val customProfiles = profiles.filter { it.isCustom }
            if (customProfiles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "CUSTOM PROFILES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))

                customProfiles.forEach { profile ->
                    CleanProfileCard(
                        profile = profile,
                        isSelected = profile.id == currentProfileId,
                        onSelect = { onProfileSelect(profile) },
                        onDelete = { showDeleteDialog = profile.id }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Create custom button
            OutlinedButton(
                onClick = onCreateCustom,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AccentBlue
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.5.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(AccentBlue)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Create Custom Profile",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { profileId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            containerColor = CardBackground,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    "Delete Profile?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "This custom profile will be permanently deleted.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProfile(profileId)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = Color(0xFFFF453A), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel", color = AccentBlue, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

@Composable
fun CleanProfileCard(
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
            containerColor = if (isSelected) AccentBlue.copy(alpha = 0.15f) else CardBackground
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, AccentBlue)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isSelected) AccentBlue.copy(alpha = 0.3f)
                        else AccentBlue.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isSelected) Icons.Default.CheckCircle else Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (isSelected) AccentBlue else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Profile info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        profile.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (profile.isCustom) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(AccentGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "CUSTOM",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGreen,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    profile.description,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }

            // Action icon
            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFFF453A),
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else if (isSelected) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}