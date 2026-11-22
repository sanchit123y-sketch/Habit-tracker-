package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PendingActions
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.StudyTaskEntity
import com.example.ui.StudyUiState
import com.example.ui.components.AiTeacherAvatarIcon
import com.example.ui.components.DailyGoalCard
import com.example.ui.components.DailyReminderCard
import com.example.ui.components.StudyLeafBrandHeader
import com.example.ui.components.TaskCard
import com.example.ui.theme.EmeraldBright
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.PureWhite

@Composable
fun DashboardScreen(
    uiState: StudyUiState,
    onToggleTask: (StudyTaskEntity) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onOpenAddTask: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToAiTeacher: () -> Unit,
    onQuickAdd15Min: () -> Unit,
    onToggleReminder: (Boolean) -> Unit,
    onSetReminderTime: (Int, Int) -> Unit,
    onToggleSkipIfGoalMet: (Boolean) -> Unit,
    onSendTestNotification: () -> Boolean,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedDailyProgress by animateFloatAsState(
        targetValue = uiState.dailyProgressFraction.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "dailyProgressAnim"
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
            // 1. Hero Welcome Banner with Custom Logo, Date & Streak
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(EmeraldDeep, EmeraldBright)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StudyLeafBrandHeader(
                                    subtitle = stringResource(R.string.welcome_subtitle),
                                    inverseColors = true
                                )

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(PureWhite.copy(alpha = 0.18f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocalFireDepartment,
                                        contentDescription = stringResource(R.string.study_streak_label),
                                        tint = Color(0xFFFDE047),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "${uiState.studyStreakDays}d streak",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = PureWhite
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = uiState.greeting,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = PureWhite
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CalendarToday,
                                        contentDescription = null,
                                        tint = EmeraldLight,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = uiState.todayFormattedDate,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = EmeraldLight
                                    )
                                }
                            }

                            // Daily Study Progress Percentage + Animated Progress Bar
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(PureWhite.copy(alpha = 0.15f))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Daily Study Progress",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = PureWhite
                                    )
                                    Text(
                                        text = "${uiState.dailyProgressPercent}%",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = PureWhite,
                                        modifier = Modifier.testTag("daily_progress_percent")
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(PureWhite.copy(alpha = 0.28f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(animatedDailyProgress)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(PureWhite)
                                    )
                                }

                                val summaryLine = if (uiState.todayTasks.isNotEmpty()) {
                                    "${uiState.todayCompletedTasks.size} of ${uiState.todayTasks.size} today's tasks completed • ${uiState.todayStudyMinutes} min studied"
                                } else {
                                    "${uiState.todayStudyMinutes} of ${uiState.dailyGoalMinutes} min daily goal studied"
                                }
                                Text(
                                    text = summaryLine,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmeraldLight
                                )
                            }
                        }
                    }
                }
            }

            // 2. Three Key Metrics: Total Completed, Total Pending, Daily Streak
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardStatCard(
                        title = "Completed",
                        value = uiState.totalCompletedCount.toString(),
                        subtitle = "${uiState.todayCompletedTasks.size} today",
                        icon = Icons.Rounded.CheckCircle,
                        accent = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_completed_tasks")
                    )
                    DashboardStatCard(
                        title = "Pending",
                        value = uiState.totalPendingCount.toString(),
                        subtitle = "${uiState.todayPendingTasks.size} today",
                        icon = Icons.Rounded.PendingActions,
                        accent = Color(0xFF0284C7),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_pending_tasks")
                    )
                    DashboardStatCard(
                        title = "Streak",
                        value = "${uiState.studyStreakDays}d",
                        subtitle = if (uiState.studyStreakDays > 0) "Keep going!" else "Start today",
                        icon = Icons.Rounded.LocalFireDepartment,
                        accent = Color(0xFFD97706),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_study_streak")
                    )
                }
            }

            // 3. Daily 1-Hour Study Goal Card (paired nicely in 2-col on tablet if wide)
            if (isWide) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        DailyGoalCard(
                            todayStudyMinutes = uiState.todayStudyMinutes,
                            goalMinutes = uiState.dailyGoalMinutes,
                            goalFraction = uiState.dailyGoalProgressFraction,
                            isGoalAchieved = uiState.isDailyGoalAchieved,
                            onQuickAdd15Min = onQuickAdd15Min,
                            onOpenFocusTimer = onNavigateToTimer,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionCard(
                            onOpenAddTask = onOpenAddTask,
                            onNavigateToTimer = onNavigateToTimer,
                            onNavigateToAiTeacher = onNavigateToAiTeacher,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                item {
                    DailyGoalCard(
                        todayStudyMinutes = uiState.todayStudyMinutes,
                        goalMinutes = uiState.dailyGoalMinutes,
                        goalFraction = uiState.dailyGoalProgressFraction,
                        isGoalAchieved = uiState.isDailyGoalAchieved,
                        onQuickAdd15Min = onQuickAdd15Min,
                        onOpenFocusTimer = onNavigateToTimer
                    )
                }
            }

            // 3a. AI Teacher Entry Card on Main Dashboard
            item {
                AiTeacherDashboardCard(
                    onOpenAiTeacher = onNavigateToAiTeacher
                )
            }

            // 3b. Daily 1-Hour Study Goal Reminder Card
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

            // 4. Today's Tasks Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.todays_tasks_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${uiState.todayPendingTasks.size} pending • ${uiState.todayCompletedTasks.size} done",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = onNavigateToTasks,
                            modifier = Modifier.testTag("view_all_tasks_button")
                        ) {
                            Text("All Tasks")
                            Spacer(modifier = Modifier.size(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 5. Today's Tasks List or Friendly Empty State
            if (uiState.todayTasks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_study_empty),
                                contentDescription = stringResource(R.string.cd_study_illustration),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                            Text(
                                text = stringResource(R.string.empty_tasks_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.empty_tasks_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = onOpenAddTask,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("dashboard_add_task_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(stringResource(R.string.add_task_cta))
                            }
                        }
                    }
                }
            } else {
                items(
                    items = uiState.todayTasks,
                    key = { "dash_${it.id}" }
                ) { task ->
                    TaskCard(
                        task = task,
                        isToday = true,
                        onToggleComplete = { onToggleTask(task) },
                        onDelete = { onDeleteTask(task.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
private fun DashboardStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AiTeacherDashboardCard(
    onOpenAiTeacher: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AiTeacherAvatarIcon(size = 48.dp)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = stringResource(R.string.ai_teacher_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Class 1–10 step-by-step doubts, homework helper & quizzes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Button(
                onClick = onOpenAiTeacher,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("dashboard_ai_teacher_button")
            ) {
                Text(stringResource(R.string.ai_teacher_dashboard_cta))
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    onOpenAddTask: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToAiTeacher: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Plan a subject task, start a 25-minute focus timer, or ask the AI Teacher.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onOpenAddTask,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                Spacer(modifier = Modifier.size(6.dp))
                Text("Add Study Task")
            }
            OutlinedButton(
                onClick = onNavigateToTimer,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Timer, contentDescription = null)
                Spacer(modifier = Modifier.size(6.dp))
                Text("Start 25m Focus")
            }
            OutlinedButton(
                onClick = onNavigateToAiTeacher,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(stringResource(R.string.ai_teacher_dashboard_cta))
            }
        }
    }
}
