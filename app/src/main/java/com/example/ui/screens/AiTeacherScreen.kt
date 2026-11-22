package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ai.AiTeacherModeTab
import com.example.ai.AiTeacherSubjects
import com.example.ai.LearningToolType
import com.example.ai.TeacherLanguage
import com.example.data.StudySubject
import com.example.ui.AiTeacherUiState
import com.example.ui.components.AiSetupRequiredBanner
import com.example.ui.components.AiTeacherAvatarIcon
import com.example.ui.theme.EmeraldBright
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.PureWhite

@Composable
fun AiTeacherScreen(
    uiState: AiTeacherUiState,
    onSelectTab: (AiTeacherModeTab) -> Unit,
    onSelectClassGrade: (Int) -> Unit,
    onSelectSubject: (StudySubject) -> Unit,
    onUpdateChapter: (String) -> Unit,
    onSelectLanguage: (TeacherLanguage) -> Unit,
    onChatInputChange: (String) -> Unit,
    onSendChatQuestion: (String?) -> Unit,
    onClearChat: () -> Unit,
    onHomeworkInputChange: (String) -> Unit,
    onExplainHomework: () -> Unit,
    onQuizTopicChange: (String) -> Unit,
    onGenerateQuiz: () -> Unit,
    onSelectQuizOption: (Int) -> Unit,
    onCheckQuizAnswer: () -> Unit,
    onNextQuizQuestion: () -> Unit,
    onResetQuiz: () -> Unit,
    onSelectLearningTool: (LearningToolType) -> Unit,
    onLearningQueryChange: (String) -> Unit,
    onRunLearningTool: (LearningToolType) -> Unit,
    modifier: Modifier = Modifier
) {
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
            // 1. Friendly AI Teacher Header Banner
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            AiTeacherAvatarIcon(size = 54.dp)
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.ai_teacher_title),
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = PureWhite
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(PureWhite.copy(alpha = 0.2f))
                                            .padding(horizontal = 10.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "Class ${uiState.context.classGrade}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = PureWhite
                                        )
                                    }
                                }
                                Text(
                                    text = stringResource(R.string.ai_teacher_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmeraldLight
                                )
                            }
                        }
                    }
                }
            }

            // 2. Transparent API / Setup Status Notice when not connected
            if (!uiState.isApiConnected) {
                item {
                    AiSetupRequiredBanner(message = uiState.setupRequiredMessage)
                }
            }

            // 3. Class 1-10, Subject, Chapter & Language Selector Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Class, Subject & Chapter",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Class 1 to Class 10
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items((1..10).toList()) { grade ->
                                val selected = uiState.context.classGrade == grade
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSelectClassGrade(grade) },
                                    label = { Text("Class $grade") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.testTag("class_grade_chip_$grade")
                                )
                            }
                        }

                        // Subjects: Maths, Science, English, Hindi, SST
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(AiTeacherSubjects.supported) { subject ->
                                val selected = uiState.context.subject == subject
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
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("teacher_subject_${subject.name.lowercase()}")
                                )
                            }
                        }

                        if (isWide) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ChapterField(
                                    value = uiState.context.chapter,
                                    onValueChange = onUpdateChapter,
                                    modifier = Modifier.weight(1.3f)
                                )
                                LanguageSelectorRow(
                                    selectedLanguage = uiState.context.language,
                                    onSelectLanguage = onSelectLanguage,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            ChapterField(
                                value = uiState.context.chapter,
                                onValueChange = onUpdateChapter,
                                modifier = Modifier.fillMaxWidth()
                            )
                            LanguageSelectorRow(
                                selectedLanguage = uiState.context.language,
                                onSelectLanguage = onSelectLanguage,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // 4. Mode Tabs: Ask AI / Homework / Quiz / Learn
            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    AiTeacherModeTab.entries.forEachIndexed { index, tab ->
                        SegmentedButton(
                            selected = uiState.activeTab == tab,
                            onClick = { onSelectTab(tab) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = AiTeacherModeTab.entries.size
                            ),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                activeContentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("ai_teacher_tab_${tab.name.lowercase()}")
                        ) {
                            Text(tab.label, maxLines = 1)
                        }
                    }
                }
            }

            // 5. Active Tab Content
            item {
                when (uiState.activeTab) {
                    AiTeacherModeTab.CHAT -> {
                        AiTeacherChatSection(
                            uiState = uiState,
                            onChatInputChange = onChatInputChange,
                            onSendQuestion = onSendChatQuestion,
                            onClearChat = onClearChat
                        )
                    }

                    AiTeacherModeTab.HOMEWORK -> {
                        HomeworkHelperSection(
                            uiState = uiState,
                            onHomeworkInputChange = onHomeworkInputChange,
                            onExplainHomework = onExplainHomework
                        )
                    }

                    AiTeacherModeTab.QUIZ -> {
                        QuizGeneratorSection(
                            uiState = uiState,
                            onTopicChange = onQuizTopicChange,
                            onGenerateQuiz = onGenerateQuiz,
                            onSelectOption = onSelectQuizOption,
                            onCheckAnswer = onCheckQuizAnswer,
                            onNextQuestion = onNextQuizQuestion,
                            onResetQuiz = onResetQuiz
                        )
                    }

                    AiTeacherModeTab.LEARN -> {
                        LearningFeaturesSection(
                            uiState = uiState,
                            onSelectTool = onSelectLearningTool,
                            onQueryChange = onLearningQueryChange,
                            onRunTool = onRunLearningTool
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
private fun ChapterField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Chapter or Topic (optional)") },
        placeholder = { Text("e.g., Chapter 4: Fractions") },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        modifier = modifier.testTag("teacher_chapter_input")
    )
}

@Composable
private fun LanguageSelectorRow(
    selectedLanguage: TeacherLanguage,
    onSelectLanguage: (TeacherLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Translate,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        TeacherLanguage.entries.forEach { lang ->
            val selected = selectedLanguage == lang
            FilterChip(
                selected = selected,
                onClick = { onSelectLanguage(lang) },
                label = { Text(lang.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("teacher_lang_${lang.name.lowercase()}")
            )
        }
    }
}
