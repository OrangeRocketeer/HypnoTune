package com.samsung.health.mobile.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Dark theme colors
private val DarkBackground = Color(0xFF0F0F0F)
private val CardBackground = Color(0xFF1C1C1E)
private val PurpleStart = Color(0xFF6B4CE6)
private val PurpleEnd = Color(0xFF9B72FF)
private val GreenPrimary = Color(0xFF34C759)
private val AccentBlue = Color(0xFF5E5CE6)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF8E8E93)

data class MusicRecommendation(
    val musicType: String,
    val musicDescription: String,
    val volumeLevel: Int,
    val duration: String,
    val additionalTips: List<String>,
    val specificTracks: List<String>
)

// Data class for questionnaire answers
data class QuestionnaireAnswers(
    val sleepDifficulty: String = "",
    val preferredTempo: String = "",
    val culturalPreference: String = "",
    val natureSounds: String = "",
    val sensitivity: String = "",
    val sleepDuration: String = "",
    val bedtimeRoutine: String = "",
    val stressLevel: String = "",
    val musicExperience: String = "",
    val sleepEnvironment: String = ""
)

@Composable
fun MusicRecommendationResultScreen(
    answers: QuestionnaireAnswers,
    onStartListening: () -> Unit,
    onRetakeQuestionnaire: () -> Unit,
    onBack: () -> Unit
) {
    val recommendation = generateRecommendation(answers)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header
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
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Text(
                            "Your Perfect Sleep Music",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Personalized just for you",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Music Type
            RecommendationCard(
                title = "Recommended Music Type",
                icon = Icons.Default.MusicNote
            ) {
                Text(
                    text = recommendation.musicType,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = recommendation.musicDescription,
                    fontSize = 14.sp,
                    color = TextPrimary.copy(alpha = 0.9f),
                    lineHeight = 20.sp
                )
            }

            // Volume Level
            RecommendationCard(
                title = "Volume Setting",
                icon = Icons.Default.VolumeUp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(AccentBlue.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${recommendation.volumeLevel}%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        LinearProgressIndicator(
                            progress = recommendation.volumeLevel / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp),
                            color = AccentBlue,
                            trackColor = CardBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = getVolumeDescription(recommendation.volumeLevel),
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Duration
            RecommendationCard(
                title = "Recommended Duration",
                icon = Icons.Default.Timer
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(GreenPrimary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = recommendation.duration,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }

            // Specific Tracks
            RecommendationCard(
                title = "Suggested Tracks",
                icon = Icons.Default.QueueMusic
            ) {
                recommendation.specificTracks.forEachIndexed { index, track ->
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(AccentBlue.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 14.sp,
                                color = AccentBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = track,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Additional Tips
            RecommendationCard(
                title = "Sleep Tips",
                icon = Icons.Default.Lightbulb
            ) {
                recommendation.additionalTips.forEach { tip ->
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .padding(top = 8.dp)
                                .background(GreenPrimary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = tip,
                            fontSize = 14.sp,
                            color = TextPrimary.copy(alpha = 0.9f),
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Button(
                onClick = onStartListening,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Start Listening",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onRetakeQuestionnaire,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Retake Questionnaire",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Back to Home",
                    color = TextSecondary,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun RecommendationCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(AccentBlue.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            content()
        }
    }
}

fun generateRecommendation(answers: QuestionnaireAnswers): MusicRecommendation {
    val musicType = when {
        answers.culturalPreference.contains("Indian") -> "Indian Classical Ragas"
        answers.culturalPreference.contains("Western") -> "Western Classical/Instrumental"
        answers.culturalPreference.contains("Ambient") -> "Ambient & Electronic"
        answers.natureSounds.contains("Love them") -> "Nature Sounds with Music"
        answers.natureSounds.contains("water") -> "Gentle Water Sounds"
        else -> "Soft Instrumental Music"
    }

    val musicDescription = when {
        musicType.contains("Ragas") -> "Traditional Indian ragas designed for evening and night time (Yaman, Bhairavi, Darbari). These have been used for centuries to promote deep relaxation and sleep."
        musicType.contains("Western Classical") -> "Slow-tempo classical pieces from composers like Debussy, Satie, and Chopin. Perfect for calming the mind and reducing anxiety."
        musicType.contains("Ambient") -> "Modern ambient soundscapes with gentle electronic textures. Minimal melody with focus on atmosphere and tranquility."
        musicType.contains("Nature") -> "Natural environmental sounds blended with soft instrumentation. Rain, ocean waves, and forest ambiance combined with gentle music."
        musicType.contains("Water") -> "Flowing water sounds including streams, gentle rain, and calm ocean waves. Pure nature sounds for maximum relaxation."
        else -> "Carefully curated instrumental music with slow tempo and minimal variation to promote gradual sleep onset."
    }

    val volumeLevel = when (answers.sensitivity) {
        "Very sensitive - Need very low volume" -> 15
        "Moderately sensitive" -> 25
        "Not very sensitive - Can handle normal volume" -> 35
        "Heavy sleeper - Prefer louder sounds" -> 45
        else -> 25
    }

    val duration = when (answers.sleepDifficulty) {
        "Racing thoughts and anxiety" -> "45-60 minutes"
        "Physical restlessness" -> "30-45 minutes"
        "External noise disturbances" -> "60-90 minutes (white noise overlay)"
        else -> "30-45 minutes"
    }

    val specificTracks = when {
        musicType.contains("Ragas") -> listOf(
            "Raga Yaman - Evening Peace",
            "Raga Bhairavi - Deep Relaxation",
            "Raga Darbari Kanada - Night Meditation",
            "Raga Malkauns - Stress Relief"
        )
        musicType.contains("Western Classical") -> listOf(
            "Debussy - Clair de Lune",
            "Satie - Gymnopédie No. 1",
            "Chopin - Nocturne Op. 9 No. 2",
            "Bach - Air on G String"
        )
        musicType.contains("Ambient") -> listOf(
            "Weightless - Marconi Union",
            "Deep Sleep Ambient Meditation",
            "Theta Waves for Sleep",
            "Calm Electronic Soundscape"
        )
        musicType.contains("Nature") -> listOf(
            "Gentle Rain with Piano",
            "Ocean Waves at Night",
            "Forest Stream Meditation",
            "Thunderstorm Ambiance"
        )
        else -> listOf(
            "Soft Piano for Sleep",
            "Gentle Guitar Melodies",
            "Peaceful Strings",
            "Calming Harp Music"
        )
    }

    val additionalTips = buildList {
        add("Start playing 15-20 minutes before your desired sleep time")

        when (answers.sleepDifficulty) {
            "Racing thoughts and anxiety" -> {
                add("Practice deep breathing along with the music")
                add("Focus on the music to redirect anxious thoughts")
            }
            "Physical restlessness" -> {
                add("Try progressive muscle relaxation while listening")
                add("Ensure your sleeping position is comfortable")
            }
            "External noise disturbances" -> {
                add("Consider using sleep headphones or a good speaker")
                add("The music will mask external disturbances")
            }
        }

        add("Keep your bedroom cool (60-67°F / 15-19°C)")
        add("Avoid screens 30 minutes before bed")

        if (volumeLevel < 25) {
            add("Place the speaker at a comfortable distance from your bed")
        }
    }

    return MusicRecommendation(
        musicType = musicType,
        musicDescription = musicDescription,
        volumeLevel = volumeLevel,
        duration = duration,
        additionalTips = additionalTips,
        specificTracks = specificTracks
    )
}

fun getVolumeDescription(volume: Int): String {
    return when {
        volume <= 20 -> "Very quiet - Barely audible"
        volume <= 30 -> "Quiet - Soft background"
        volume <= 40 -> "Moderate - Comfortable level"
        else -> "Normal - Clear but not loud"
    }
}