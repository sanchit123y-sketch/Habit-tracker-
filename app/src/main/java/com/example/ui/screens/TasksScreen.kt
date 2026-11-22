package com.example.ui.screens

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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.PendingActions
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.StudySubject
import com.example.data.StudyTaskEntity
import com.example.ui.StudyUiState
import com.example.ui.TaskListTab
import com.example.ui.components.TaskCard

@Composable
fun TasksScreen(
    uiState: StudyUiState,
    onSelectTab: (TaskListTab) -> Unit,
    onSelectSubjectFilter: (StudySubject?) -> Unit,
    onToggleTask: (StudyTaskEntity) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onOpenAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subjectFilter = uiState.selectedSubjectFilter
    fun matchesSubject(task: StudyTaskEntity): Boolean {
        return subjectFilter == null || StudySubject.fromName(task.subjectName) == subjectFilter
    }

    val todayPendingFiltered = uiState.todayPendingTasks.filter(::matchesSubject)
    val todayCompletedFiltered = uiState.todayCompletedTasks.filter(::matchesSubject)
    val otherPendingFiltered = uiState.otherPendingTasks.filter(::matchesSubject)
    val allCompletedFiltered = uiState.completedHistory.filter(::matchesSubject)

    val hasAnyVisible = when (uiState.selectedTaskTab) {
        TaskListTab.TODAY -> todayPendingFiltered.isNotEmpty() || todayCompletedFiltered.isNotEmpty()
        TaskListTab.ALL -> todayPendingFiltered.isNotEmpty() ||
            otherPendingFiltered.isNotEmpty() ||
            allCompletedFiltered.isNotEmpty()
        TaskListTab.COMPLETED -> allCompletedFiltered.isNotEmpty()
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 960.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Study Task Manager",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Organize tasks by subject and track today's priorities separately.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tab Switcher: Today / All Tasks / Completed
            item {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TaskListTab.entries.forEachIndexed { index, tab ->
                        SegmentedButton(
                            selected = uiState.selectedTaskTab == tab,
                            onClick = { onSelectTab(tab) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = TaskListTab.entries.size
                            ),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                activeContentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("task_tab_${tab.name.lowercase()}")
                        ) {
                            Text(tab.label)
                        }
                    }
                }
            }

            // Subject Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = subjectFilter == null,
                            onClick = { onSelectSubjectFilter(null) },
                            label = { Text("All Subjects") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    items(StudySubject.entries) { subject ->
                        val selected = subjectFilter == subject
                        FilterChip(
                            selected = selected,
                            onClick = {
                                onSelectSubjectFilter(if (selected) null else subject)
                            },
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

            if (!hasAnyVisible) {
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
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_study_empty),
                                contentDescription = stringResource(R.string.cd_study_illustration),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(145.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                            Text(
                                text = if (uiState.selectedTaskTab == TaskListTab.COMPLETED) {
                                    stringResource(R.string.empty_history_title)
                                } else {
                                    stringResource(R.string.empty_tasks_title)
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (uiState.selectedTaskTab == TaskListTab.COMPLETED) {
                                    stringResource(R.string.empty_history_subtitle)
                                } else {
                                    stringResource(R.string.empty_tasks_subtitle)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (uiState.selectedTaskTab != TaskListTab.COMPLETED) {
                                Button(
                                    onClick = onOpenAddTask,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.size(6.dp))
                                    Text(stringResource(R.string.add_task_cta))
                                }
                            }
                        }
                    }
                }
            } else {
                when (uiState.selectedTaskTab) {
                    TaskListTab.TODAY -> {
                        if (todayPendingFiltered.isNotEmpty()) {
                            item {
                                TaskSectionHeader(
                                    title = "Today's Pending (${todayPendingFiltered.size})",
                                    icon = Icons.Rounded.Today
                                )
                            }
                            items(todayPendingFiltered, key = { "tp_${it.id}" }) { task ->
                                TaskCard(
                                    task = task,
                                    isToday = true,
                                    onToggleComplete = { onToggleTask(task) },
                                    onDelete = { onDeleteTask(task.id) }
                                )
                            }
                        }

                        if (todayCompletedFiltered.isNotEmpty()) {
                            item {
                                TaskSectionHeader(
                                    title = "Completed Today (${todayCompletedFiltered.size})",
                                    icon = Icons.Rounded.CheckCircleOutline
                                )
                            }
                            items(todayCompletedFiltered, key = { "tc_${it.id}" }) { task ->
                                TaskCard(
                                    task = task,
                                    isToday = true,
                                    onToggleComplete = { onToggleTask(task) },
                                    onDelete = { onDeleteTask(task.id) }
                                )
                            }
                        }
                    }

                    TaskListTab.ALL -> {
                        if (todayPendingFiltered.isNotEmpty()) {
                            item {
                                TaskSectionHeader(
                                    title = "Today's Tasks (${todayPendingFiltered.size})",
                                    icon = Icons.Rounded.Today
                                )
                            }
                            items(todayPendingFiltered, key = { "ap_today_${it.id}" }) { task ->
                                TaskCard(
                                    task = task,
                                    isToday = true,
                                    onToggleComplete = { onToggleTask(task) },
                                    onDelete = { onDeleteTask(task.id) }
                                )
                            }
                        }

                        if (otherPendingFiltered.isNotEmpty()) {
                            item {
                                TaskSectionHeader(
                                    title = "Upcoming & Other Pending (${otherPendingFiltered.size})",
                                    icon = Icons.Rounded.PendingActions
                                )
                            }
                            items(otherPendingFiltered, key = { "ap_other_${it.id}" }) { task ->
                                TaskCard(
                                    task = task,
                                    isToday = false,
                                    onToggleComplete = { onToggleTask(task) },
                                    onDelete = { onDeleteTask(task.id) }
                                )
                            }
                        }

                        if (allCompletedFiltered.isNotEmpty()) {
                            item {
                                TaskSectionHeader(
                                    title = "Completed (${allCompletedFiltered.size})",
                                    icon = Icons.Rounded.CheckCircleOutline
                                )
                            }
                            items(allCompletedFiltered, key = { "ac_${it.id}" }) { task ->
                                TaskCard(
                                    task = task,
                                    isToday = task.scheduledDateKey == uiState.todayDateKey,
                                    onToggleComplete = { onToggleTask(task) },
                                    onDelete = { onDeleteTask(task.id) }
                                )
                            }
                        }
                    }

                    TaskListTab.COMPLETED -> {
                        item {
                            TaskSectionHeader(
                                title = "Completed Tasks (${allCompletedFiltered.size})",
                                icon = Icons.Rounded.CheckCircleOutline
                            )
                        }
                        items(allCompletedFiltered, key = { "comp_${it.id}" }) { task ->
                            TaskCard(
                                task = task,
                                isToday = task.scheduledDateKey == uiState.todayDateKey,
                                onToggleComplete = { onToggleTask(task) },
                                onDelete = { onDeleteTask(task.id) }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(76.dp))
            }
        }
    }
}

@Composable
private fun TaskSectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
