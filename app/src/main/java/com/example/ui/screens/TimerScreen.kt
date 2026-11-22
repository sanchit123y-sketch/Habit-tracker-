package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.StudySubject
import com.example.ui.StudyUiState
import com.example.ui.TimerMode
import com.example.ui.components.DailyGoalCard
import com.example.ui.components.DailyReminderCard
import com.example.ui.theme.EmeraldBright
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.JetBrainsMonoFamily

@Composable
fun TimerScreen(
    uiState: StudyUiState,
    onSelectMode: (TimerMode) -> Unit,
    onSelectSubject: (StudySubject) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onQuickAdd15Min: () -> Unit,
    onToggleReminder: (Boolean) -> Unit,
    onSetReminderTime: (Int, Int) -> Unit,
    onToggleSkipIfGoalMet: (Boolean) -> Unit,
    onSendTestNotification: () -> Boolean,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val timer = uiState.timer
    val animatedSweep by animateFloatAsState(
        targetValue = (timer.progressFraction * 360f).coerceIn(0f, 360f),
        animationSpec = tween(durationMillis = 400, easing = LinearEasing),
        label = "timerRingSweep"
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 680.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 960.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Study Focus Timer",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "25-minute deep focus cycles paired with 5-minute refreshing breaks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Mode Toggle: Focus 25m vs Break 5m
            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    TimerMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = timer.mode == mode,
                            onClick = { onSelectMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = TimerMode.entries.size
                            ),
                            icon = {
                                Icon(
                                    imageVector = if (mode == TimerMode.FOCUS) {
                                        Icons.Rounded.SelfImprovement
                                    } else {
                                        Icons.Rounded.Coffee
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                activeContentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("timer_mode_${mode.name.lowercase()}")
                        ) {
                            Text(mode.label)
                        }
                    }
                }
            }

            // Subject Selection for Focus Tracking
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Studying Subject",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(StudySubject.entries) { subject ->
                            val selected = timer.selectedSubject == subject
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectSubject(subject) },
                                label = { Text(subject.displayName) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = subject.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            }

            // Main Timer Ring Card (+ side-by-side Goal Card on tablet/wide)
            item {
                val timerCard: @Composable (Modifier) -> Unit = { cardMod ->
                    Card(
                        modifier = cardMod.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Status Badge
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = when {
                                        timer.isRunning && timer.mode == TimerMode.FOCUS ->
                                            "Focusing • ${timer.selectedSubject.displayName}"
                                        timer.isRunning && timer.mode == TimerMode.BREAK ->
                                            "Break Time"
                                        timer.mode == TimerMode.FOCUS -> "Ready to Focus"
                                        else -> "Ready for Break"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Circular Countdown Ring
                            val trackColor = MaterialTheme.colorScheme.primaryContainer
                            val ringColor = if (timer.mode == TimerMode.FOCUS) {
                                EmeraldBright
                            } else {
                                Color(0xFF0D9488)
                            }
                            Box(
                                modifier = Modifier.size(230.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeWidth = 14.dp.toPx()
                                    drawArc(
                                        color = trackColor,
                                        startAngle = 0f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                    drawArc(
                                        brush = Brush.sweepGradient(
                                            colors = listOf(ringColor, EmeraldDeep, ringColor)
                                        ),
                                        startAngle = -90f,
                                        sweepAngle = animatedSweep,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = timer.formattedTime,
                                        style = MaterialTheme.typography.displaySmall.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontSize = 48.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.testTag("timer_countdown_text")
                                    )
                                    Text(
                                        text = "${uiState.todayStudyMinutes} min studied today",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            AnimatedVisibility(visible = timer.lastCompletedMessage != null) {
                                Text(
                                    text = timer.lastCompletedMessage.orEmpty(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Start, Pause & Reset Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (timer.isRunning) {
                                    Button(
                                        onClick = onPause,
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(54.dp)
                                            .testTag("timer_pause_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Pause,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.size(6.dp))
                                        Text(stringResource(R.string.timer_pause))
                                    }
                                } else {
                                    Button(
                                        onClick = onStart,
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(54.dp)
                                            .testTag("timer_start_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.PlayArrow,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.size(6.dp))
                                        Text(stringResource(R.string.timer_start))
                                    }
                                }

                                OutlinedButton(
                                    onClick = onReset,
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                        .testTag("timer_reset_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Refresh,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.size(6.dp))
                                    Text(stringResource(R.string.timer_reset))
                                }
                            }

                            // Study Time Display Strip
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Session Logged",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${timer.activeSessionElapsedMinutes} min",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Today's Total Study",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${uiState.todayStudyMinutes} min",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        timerCard(Modifier.weight(1.2f))
                        DailyGoalCard(
                            todayStudyMinutes = uiState.todayStudyMinutes,
                            goalMinutes = uiState.dailyGoalMinutes,
                            goalFraction = uiState.dailyGoalProgressFraction,
                            isGoalAchieved = uiState.isDailyGoalAchieved,
                            onQuickAdd15Min = onQuickAdd15Min,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        timerCard(Modifier)
                        DailyGoalCard(
                            todayStudyMinutes = uiState.todayStudyMinutes,
                            goalMinutes = uiState.dailyGoalMinutes,
                            goalFraction = uiState.dailyGoalProgressFraction,
                            isGoalAchieved = uiState.isDailyGoalAchieved,
                            onQuickAdd15Min = onQuickAdd15Min
                        )
                    }
                }
            }

            item {
                DailyReminderCard(
                    settings = uiState.reminderSettings,
                    onToggleEnabled = onToggleReminder,
                    onTimeSelected = onSetReminderTime,
                    onToggleSkipIfGoalMet = onToggleSkipIfGoalMet,
                    onSendTestNotification = onSendTestNotification,
                    onShowMessage = onShowMessage
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}
