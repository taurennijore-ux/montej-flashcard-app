package com.montej.flashcard.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.montej.flashcard.data.local.FlashcardEntity
import com.montej.flashcard.data.repository.LibraryRepository
import com.montej.flashcard.ui.theme.ThemeMode
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun FlashcardScreen(
    fileId: Long,
    onBack: () -> Unit,
    themeMode: ThemeMode
) {
    val context = LocalContext.current
    val repository = remember { LibraryRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    var flashcards by remember { mutableStateOf<List<FlashcardEntity>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }

    LaunchedEffect(fileId) {
        repository.getFlashcardsByFile(fileId).collect { cards ->
            flashcards = cards
        }
    }

    if (flashcards.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "No flashcards available",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .height(40.dp)
                ) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    val currentCard = flashcards.getOrNull(currentIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text("${currentIndex + 1}/${flashcards.size} • Study Mode")
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        )

        LinearProgressIndicator(
            progress = (currentIndex + 1) / flashcards.size.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (currentCard != null) {
                FlashcardContent(
                    card = currentCard,
                    isFlipped = isFlipped,
                    dragOffset = dragOffset,
                    onFlip = { isFlipped = !isFlipped },
                    onDragOffsetChange = { dragOffset = it },
                    onSwipeLeft = {
                        if (currentIndex < flashcards.size - 1) {
                            currentIndex++
                            isFlipped = false
                            dragOffset = 0f
                            coroutineScope.launch {
                                repository.updateStudyStatistics(fileId, true)
                            }
                        }
                    },
                    onSwipeRight = {
                        if (currentIndex > 0) {
                            currentIndex--
                            isFlipped = false
                            dragOffset = 0f
                        }
                    }
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(
                    label = "Total",
                    value = flashcards.size.toString()
                )
                StatItem(
                    label = "Progress",
                    value = "${((currentIndex + 1) / flashcards.size.toFloat() * 100).toInt()}%"
                )
                StatItem(
                    label = "Remaining",
                    value = (flashcards.size - currentIndex - 1).toString()
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (currentIndex > 0) {
                        currentIndex--
                        isFlipped = false
                        dragOffset = 0f
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                enabled = currentIndex > 0,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("← Previous")
            }

            Button(
                onClick = {
                    if (currentIndex < flashcards.size - 1) {
                        currentIndex++
                        isFlipped = false
                        dragOffset = 0f
                        coroutineScope.launch {
                            repository.updateStudyStatistics(fileId, true)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                enabled = currentIndex < flashcards.size - 1,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Next →")
            }
        }
    }
}

@Composable
fun FlashcardContent(
    card: FlashcardEntity,
    isFlipped: Boolean,
    dragOffset: Float,
    onFlip: () -> Unit,
    onDragOffsetChange: (Float) -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    val rotation = animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        label = "flip_animation"
    )

    val dragPercentage = dragOffset / 300f

    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .aspectRatio(1.5f)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            dragOffset > 100f -> {
                                onSwipeRight()
                                onDragOffsetChange(0f)
                            }
                            dragOffset < -100f -> {
                                onSwipeLeft()
                                onDragOffsetChange(0f)
                            }
                            else -> {
                                onDragOffsetChange(0f)
                            }
                        }
                    }
                ) { _, dragAmount ->
                    onDragOffsetChange(dragOffset + dragAmount)
                }
            }
            .graphicsLayer {
                rotationY = rotation.value
                translationX = dragOffset
                alpha = 1f - (abs(dragPercentage) * 0.2f)
            }
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFlipped)
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onFlip() }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        if (isFlipped) "Answer" else "Question",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = if (isFlipped) card.answer else card.question,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        "Tap to flip • Swipe to navigate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}