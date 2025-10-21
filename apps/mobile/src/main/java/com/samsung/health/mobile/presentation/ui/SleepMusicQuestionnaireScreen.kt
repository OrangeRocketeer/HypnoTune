package com.samsung.health.mobile.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

// Dark theme colors
private val DarkBackground = Color(0xFF0F0F0F)
private val CardBackground = Color(0xFF1C1C1E)
private val AccentBlue = Color(0xFF5E5CE6)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF8E8E93)


@Composable
fun SleepMusicQuestionnaireScreen(
    onNavigateToResults: (QuestionnaireAnswers) -> Unit,
    onBack: () -> Unit
) {
    var currentQuestion by remember { mutableStateOf(0) }
    var answers by remember { mutableStateOf(QuestionnaireAnswers()) }
    val totalQuestions = 10

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Sleep Music Questionnaire",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress
            LinearProgressIndicator(
                progress = { (currentQuestion + 1) / totalQuestions.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = AccentBlue,
                trackColor = CardBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Question ${currentQuestion + 1} of $totalQuestions",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                when (currentQuestion) {
                    0 -> QuestionOne(
                        selectedAnswer = answers.sleepDifficulty,
                        onAnswerSelected = { answers = answers.copy(sleepDifficulty = it) }
                    )
                    1 -> QuestionTwo(
                        selectedAnswer = answers.preferredTempo,
                        onAnswerSelected = { answers = answers.copy(preferredTempo = it) }
                    )
                    2 -> QuestionThree(
                        selectedAnswer = answers.culturalPreference,
                        onAnswerSelected = { answers = answers.copy(culturalPreference = it) }
                    )
                    3 -> QuestionFour(
                        selectedAnswer = answers.natureSounds,
                        onAnswerSelected = { answers = answers.copy(natureSounds = it) }
                    )
                    4 -> QuestionFive(
                        selectedAnswer = answers.sensitivity,
                        onAnswerSelected = { answers = answers.copy(sensitivity = it) }
                    )
                    5 -> QuestionSix(
                        selectedAnswer = answers.sleepDuration,
                        onAnswerSelected = { answers = answers.copy(sleepDuration = it) }
                    )
                    6 -> QuestionSeven(
                        selectedAnswer = answers.bedtimeRoutine,
                        onAnswerSelected = { answers = answers.copy(bedtimeRoutine = it) }
                    )
                    7 -> QuestionEight(
                        selectedAnswer = answers.stressLevel,
                        onAnswerSelected = { answers = answers.copy(stressLevel = it) }
                    )
                    8 -> QuestionNine(
                        selectedAnswer = answers.musicExperience,
                        onAnswerSelected = { answers = answers.copy(musicExperience = it) }
                    )
                    9 -> QuestionTen(
                        selectedAnswer = answers.sleepEnvironment,
                        onAnswerSelected = { answers = answers.copy(sleepEnvironment = it) }
                    )
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        if (currentQuestion > 0) currentQuestion--
                        else onBack()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (currentQuestion > 0) "Back" else "Cancel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        if (currentQuestion < totalQuestions - 1) {
                            currentQuestion++
                        } else {
                            onNavigateToResults(answers)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(start = 8.dp),
                    enabled = when (currentQuestion) {
                        0 -> answers.sleepDifficulty.isNotEmpty()
                        1 -> answers.preferredTempo.isNotEmpty()
                        2 -> answers.culturalPreference.isNotEmpty()
                        3 -> answers.natureSounds.isNotEmpty()
                        4 -> answers.sensitivity.isNotEmpty()
                        5 -> answers.sleepDuration.isNotEmpty()
                        6 -> answers.bedtimeRoutine.isNotEmpty()
                        7 -> answers.stressLevel.isNotEmpty()
                        8 -> answers.musicExperience.isNotEmpty()
                        9 -> answers.sleepEnvironment.isNotEmpty()
                        else -> false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        disabledContainerColor = CardBackground
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        if (currentQuestion < totalQuestions - 1) "Next" else "Get Results",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        if (currentQuestion < totalQuestions - 1) Icons.Default.ArrowForward else Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuestionOne(selectedAnswer: String, onAnswerSelected: (String) -> Unit) {
    QuestionTemplate(
        question = "What's your main challenge when trying to fall asleep?",
        options = listOf(
            "Racing thoughts and anxiety",
            "Physical restlessness",
            "External noise disturbances",
            "Difficulty relaxing after a busy day",
            "General insomnia"
        ),
        selectedAnswer = selectedAnswer,
        onAnswerSelected = onAnswerSelected
    )
}

@Composable
fun QuestionTwo(selectedAnswer: String, onAnswerSelected: (String) -> Unit) {
    QuestionTemplate(
        question = "Which music tempo helps you relax most?",
        options = listOf(
            "Very slow (40-60 BPM) - Deep relaxation",
            "Slow (60-80 BPM) - Gentle calming",
            "Moderate (80-100 BPM) - Light relaxation",
            "I prefer silence with occasional sounds",
            "Not sure"
        ),
        selectedAnswer = selectedAnswer,
        onAnswerSelected = onAnswerSelected
    )
}

@Composable
fun QuestionThree(selectedAnswer: String, onAnswerSelected: (String) -> Unit) {
    QuestionTemplate(
        question = "Do you have a cultural or musical preference?",
        options = listOf(
            "Indian classical (Ragas)",
            "Western classical/Instrumental",
            "Ambient/Electronic",
            "World music (Various cultures)",
            "No preference"
        ),
        selectedAnswer = selectedAnswer,
        onAnswerSelected = onAnswerSelected
    )
}

@Composable
fun QuestionFour(selectedAnswer: String, onAnswerSelected: (String) -> Unit) {
    QuestionTemplate(
        question = "How do you feel about nature sounds?",
        options = listOf(
            "Love them - Rain, ocean, forest",
            "Prefer gentle water sounds only",
            "Like them mixed with music",
            "Find them distracting",
            "Never tried them"
        ),
        selectedAnswer = selectedAnswer,
        onAnswerSelected = onAnswerSelected
    )
}

@Composable
fun QuestionFive(selectedAnswer: String, onAnswerSelected: (String) -> Unit) {
    QuestionTemplate(
        question = "How sensitive are you to sound while sleeping?",
        options = listOf(
            "Very sensitive - Need very low volume",
            "Moderately sensitive",
            "Not very sensitive - Can handle normal volume",
            "Heavy sleeper - Prefer louder sounds",
            "Not sure"
        ),
        selectedAnswer = selectedAnswer,
        onAnswerSelected = onAnswerSelected
    )
}

@Composable
fun QuestionSix(selectedAnswer: String, onAnswerSelected: (String) -> Unit) {
    QuestionTemplate(
        question = "How long does it typically take you to fall asleep?",
        options = listOf(
            "Less than 15 minutes",
            "15-30 minutes",
            "30-60 minutes",
            "More than 60 minutes",
            "It varies greatly"
        ),
        selectedAnswer = selectedAnswer,
        onAnswerSelected = onAnswerSelected
    )
}

@Composable
fun QuestionSeven(selectedAnswer: String, onAnswerSelected: (String) -> Unit) {
    QuestionTemplate(
        question = "Do you have a bedtime routine?",
        options = listOf(
            "Yes, consistent routine every night",
            "Sometimes, depends on the day",
            "Rarely have a routine",
            "No routine at all",
            "Want to build one"
        ),
        selectedAnswer = selectedAnswer,
        onAnswerSelected = onAnswerSelected
    )
}

@Composable
fun QuestionEight(selectedAnswer: String, onAnswerSelected: (String) -> Unit) {
    QuestionTemplate(
        question = "What's your stress level during the day?",
        options = listOf(
            "Very high - Constantly stressed",
            "High - Frequently stressed",
            "Moderate - Sometimes stressed",
            "Low - Rarely stressed",
            "Very low - Almost never stressed"
        ),
        selectedAnswer = selectedAnswer,
        onAnswerSelected = onAnswerSelected
    )
}

@Composable
fun QuestionNine(selectedAnswer: String, onAnswerSelected: (String) -> Unit) {
    QuestionTemplate(
        question = "What's your experience with sleep music?",
        options = listOf(
            "Never tried it before",
            "Tried once or twice",
            "Use it occasionally",
            "Use it regularly",
            "Can't sleep without it"
        ),
        selectedAnswer = selectedAnswer,
        onAnswerSelected = onAnswerSelected
    )
}

@Composable
fun QuestionTen(selectedAnswer: String, onAnswerSelected: (String) -> Unit) {
    QuestionTemplate(
        question = "How would you describe your sleep environment?",
        options = listOf(
            "Very quiet and dark",
            "Quiet but some light",
            "Some ambient noise",
            "Noisy environment (city, traffic)",
            "Shared space with others"
        ),
        selectedAnswer = selectedAnswer,
        onAnswerSelected = onAnswerSelected
    )
}

@Composable
fun QuestionTemplate(
    question: String,
    options: List<String>,
    selectedAnswer: String,
    onAnswerSelected: (String) -> Unit
) {
    Column {
        Text(
            text = question,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 24.dp),
            lineHeight = 32.sp
        )

        options.forEach { option ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .selectable(
                        selected = selectedAnswer == option,
                        onClick = { onAnswerSelected(option) }
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedAnswer == option)
                        AccentBlue.copy(alpha = 0.3f)
                    else
                        CardBackground
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                if (selectedAnswer == option) AccentBlue else Color.Transparent,
                                CircleShape
                            )
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedAnswer == option) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(Color.Transparent, CircleShape)
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(TextSecondary.copy(alpha = 0.3f), CircleShape)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = option,
                        fontSize = 15.sp,
                        color = if (selectedAnswer == option) TextPrimary else TextPrimary.copy(alpha = 0.8f),
                        fontWeight = if (selectedAnswer == option) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}