package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiTeacherModeTab
import com.example.ai.AiTeacherRepository
import com.example.ai.AiTeacherResult
import com.example.ai.HomeworkExplanation
import com.example.ai.LearningOutput
import com.example.ai.LearningToolType
import com.example.ai.QuizSessionState
import com.example.ai.TeacherChatMessage
import com.example.ai.TeacherContext
import com.example.ai.TeacherLanguage
import com.example.ai.UnconfiguredAiTeacherRepository
import com.example.data.StudySubject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class AiTeacherUiState(
    val isApiConnected: Boolean = false,
    val setupRequiredMessage: String = "",
    val context: TeacherContext = TeacherContext(),
    val activeTab: AiTeacherModeTab = AiTeacherModeTab.CHAT,

    // 1. AI Teacher Chat
    val chatInput: String = "",
    val chatMessages: List<TeacherChatMessage> = emptyList(),
    val isChatLoading: Boolean = false,
    val chatErrorMessage: String? = null,

    // 2. Homework Helper
    val homeworkInput: String = "",
    val isHomeworkLoading: Boolean = false,
    val homeworkSolution: HomeworkExplanation? = null,
    val homeworkErrorMessage: String? = null,
    val homeworkSetupNotice: String? = null,

    // 3. Quiz Generator (one question at a time + score tracking)
    val quizTopicInput: String = "",
    val isQuizLoading: Boolean = false,
    val quizSession: QuizSessionState = QuizSessionState(),
    val quizErrorMessage: String? = null,
    val quizSetupNotice: String? = null,
    val lifetimeQuizCorrect: Int = 0,
    val lifetimeQuizAttempted: Int = 0,

    // 4. Learning Features
    val selectedLearningTool: LearningToolType = LearningToolType.DAILY_TIP,
    val learningQueryInput: String = "",
    val isLearningLoading: Boolean = false,
    val learningOutput: LearningOutput? = null,
    val learningErrorMessage: String? = null,
    val learningSetupNotice: String? = null
)

class AiTeacherViewModel(
    private val repository: AiTeacherRepository = UnconfiguredAiTeacherRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AiTeacherUiState(
            isApiConnected = repository.isApiConnected,
            setupRequiredMessage = repository.setupRequiredMessage
        )
    )
    val uiState: StateFlow<AiTeacherUiState> = _uiState.asStateFlow()

    fun selectTab(tab: AiTeacherModeTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun selectClassGrade(grade: Int) {
        val clamped = grade.coerceIn(1, 10)
        _uiState.update { it.copy(context = it.context.copy(classGrade = clamped)) }
    }

    fun selectSubject(subject: StudySubject) {
        _uiState.update { it.copy(context = it.context.copy(subject = subject)) }
    }

    fun updateChapter(chapter: String) {
        _uiState.update { it.copy(context = it.context.copy(chapter = chapter)) }
    }

    fun selectLanguage(language: TeacherLanguage) {
        _uiState.update { it.copy(context = it.context.copy(language = language)) }
    }

    // --- 1. AI Teacher Chat ---

    fun updateChatInput(value: String) {
        _uiState.update { it.copy(chatInput = value, chatErrorMessage = null) }
    }

    fun sendChatMessage(presetQuestion: String? = null) {
        val state = _uiState.value
        val question = (presetQuestion ?: state.chatInput).trim()
        if (question.isEmpty() || state.isChatLoading) return

        val studentMsg = TeacherChatMessage(
            id = UUID.randomUUID().toString(),
            isFromStudent = true,
            text = question
        )

        _uiState.update {
            it.copy(
                chatInput = if (presetQuestion == null) "" else it.chatInput,
                chatMessages = it.chatMessages + studentMsg,
                isChatLoading = true,
                chatErrorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                when (val result = repository.askTeacher(question, state.context, state.chatMessages)) {
                    is AiTeacherResult.Success -> {
                        val reply = TeacherChatMessage(
                            id = UUID.randomUUID().toString(),
                            isFromStudent = false,
                            text = result.data
                        )
                        _uiState.update {
                            it.copy(
                                chatMessages = it.chatMessages + reply,
                                isChatLoading = false
                            )
                        }
                    }

                    is AiTeacherResult.SetupRequired -> {
                        val notice = TeacherChatMessage(
                            id = UUID.randomUUID().toString(),
                            isFromStudent = false,
                            text = result.message,
                            isSetupNotice = true
                        )
                        _uiState.update {
                            it.copy(
                                chatMessages = it.chatMessages + notice,
                                isChatLoading = false
                            )
                        }
                    }

                    is AiTeacherResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isChatLoading = false,
                                chatErrorMessage = result.message
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isChatLoading = false,
                        chatErrorMessage = e.localizedMessage ?: "Failed to reach AI Teacher."
                    )
                }
            }
        }
    }

    fun clearChat() {
        _uiState.update { it.copy(chatMessages = emptyList(), chatErrorMessage = null) }
    }

    // --- 2. Homework Helper ---

    fun updateHomeworkInput(value: String) {
        _uiState.update { it.copy(homeworkInput = value, homeworkErrorMessage = null) }
    }

    fun explainHomework() {
        val state = _uiState.value
        val question = state.homeworkInput.trim()
        if (question.isEmpty() || state.isHomeworkLoading) return

        _uiState.update {
            it.copy(
                isHomeworkLoading = true,
                homeworkErrorMessage = null,
                homeworkSetupNotice = null
            )
        }

        viewModelScope.launch {
            try {
                when (val result = repository.explainHomework(question, state.context)) {
                    is AiTeacherResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isHomeworkLoading = false,
                                homeworkSolution = result.data,
                                homeworkSetupNotice = null
                            )
                        }
                    }

                    is AiTeacherResult.SetupRequired -> {
                        _uiState.update {
                            it.copy(
                                isHomeworkLoading = false,
                                homeworkSolution = null,
                                homeworkSetupNotice = result.message
                            )
                        }
                    }

                    is AiTeacherResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isHomeworkLoading = false,
                                homeworkErrorMessage = result.message
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isHomeworkLoading = false,
                        homeworkErrorMessage = e.localizedMessage ?: "Could not explain homework."
                    )
                }
            }
        }
    }

    // --- 3. Quiz Generator ---

    fun updateQuizTopic(topic: String) {
        _uiState.update { it.copy(quizTopicInput = topic, quizErrorMessage = null) }
    }

    fun generateQuiz(questionCount: Int = 5) {
        val state = _uiState.value
        if (state.isQuizLoading) return

        val topic = state.quizTopicInput.trim()
            .ifEmpty { state.context.chapter.trim() }
            .ifEmpty { "${state.context.subject.displayName} (Class ${state.context.classGrade})" }

        _uiState.update {
            it.copy(
                isQuizLoading = true,
                quizErrorMessage = null,
                quizSetupNotice = null
            )
        }

        viewModelScope.launch {
            try {
                when (val result = repository.generateQuiz(topic, questionCount, state.context)) {
                    is AiTeacherResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isQuizLoading = false,
                                quizSession = QuizSessionState(
                                    topicLabel = topic,
                                    questions = result.data,
                                    currentIndex = 0
                                ),
                                quizSetupNotice = null
                            )
                        }
                    }

                    is AiTeacherResult.SetupRequired -> {
                        _uiState.update {
                            it.copy(
                                isQuizLoading = false,
                                quizSetupNotice = result.message
                            )
                        }
                    }

                    is AiTeacherResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isQuizLoading = false,
                                quizErrorMessage = result.message
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isQuizLoading = false,
                        quizErrorMessage = e.localizedMessage ?: "Could not generate quiz."
                    )
                }
            }
        }
    }

    fun selectQuizOption(optionIndex: Int) {
        _uiState.update { state ->
            val session = state.quizSession
            if (session.isAnswerChecked) return@update state
            state.copy(quizSession = session.copy(selectedOptionIndex = optionIndex))
        }
    }

    fun checkCurrentQuizAnswer() {
        _uiState.update { state ->
            val session = state.quizSession
            val q = session.currentQuestion ?: return@update state
            val selected = session.selectedOptionIndex ?: return@update state
            if (session.isAnswerChecked) return@update state

            val isCorrect = selected == q.correctOptionIndex
            state.copy(
                quizSession = session.copy(
                    isAnswerChecked = true,
                    correctCount = session.correctCount + if (isCorrect) 1 else 0,
                    answeredCount = session.answeredCount + 1
                ),
                lifetimeQuizCorrect = state.lifetimeQuizCorrect + if (isCorrect) 1 else 0,
                lifetimeQuizAttempted = state.lifetimeQuizAttempted + 1
            )
        }
    }

    fun nextQuizQuestion() {
        _uiState.update { state ->
            val session = state.quizSession
            if (!session.isAnswerChecked) return@update state
            state.copy(
                quizSession = session.copy(
                    currentIndex = session.currentIndex + 1,
                    selectedOptionIndex = null,
                    isAnswerChecked = false
                )
            )
        }
    }

    fun resetQuiz() {
        _uiState.update {
            it.copy(
                quizSession = QuizSessionState(),
                quizErrorMessage = null,
                quizSetupNotice = null
            )
        }
    }

    // --- 4. Learning Features ---

    fun selectLearningTool(toolType: LearningToolType) {
        _uiState.update {
            it.copy(
                selectedLearningTool = toolType,
                learningErrorMessage = null
            )
        }
    }

    fun updateLearningQuery(query: String) {
        _uiState.update { it.copy(learningQueryInput = query, learningErrorMessage = null) }
    }

    fun runLearningTool(toolType: LearningToolType = _uiState.value.selectedLearningTool) {
        val state = _uiState.value
        if (state.isLearningLoading) return

        val query = state.learningQueryInput.trim()
        _uiState.update {
            it.copy(
                selectedLearningTool = toolType,
                isLearningLoading = true,
                learningErrorMessage = null,
                learningSetupNotice = null
            )
        }

        viewModelScope.launch {
            try {
                when (val result = repository.generateLearningAid(toolType, query, state.context)) {
                    is AiTeacherResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLearningLoading = false,
                                learningOutput = result.data,
                                learningSetupNotice = null
                            )
                        }
                    }

                    is AiTeacherResult.SetupRequired -> {
                        _uiState.update {
                            it.copy(
                                isLearningLoading = false,
                                learningOutput = null,
                                learningSetupNotice = result.message
                            )
                        }
                    }

                    is AiTeacherResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLearningLoading = false,
                                learningErrorMessage = result.message
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLearningLoading = false,
                        learningErrorMessage = e.localizedMessage ?: "Failed to load learning content."
                    )
                }
            }
        }
    }
}
