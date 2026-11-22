package com.example.ai

import com.example.data.StudySubject

enum class TeacherLanguage(val label: String, val shortLabel: String) {
    ENGLISH("Simple English", "English"),
    HINDI("Simple Hindi (हिंदी)", "Hindi")
}

enum class AiTeacherModeTab(val label: String) {
    CHAT("Ask AI"),
    HOMEWORK("Homework"),
    QUIZ("Quiz"),
    LEARN("Learn")
}

enum class LearningToolType(val title: String, val subtitle: String) {
    DAILY_TIP("Daily Study Tip", "Get a fresh tip tailored to your class & subject"),
    WORD_MEANING("Word Meaning", "Easy meaning, pronunciation & sentence example"),
    DEFINITION_EXAMPLES("Definitions & Examples", "Clear concept definition with everyday examples"),
    PRACTICE_QUESTIONS("Practice Questions", "Age-appropriate practice problems with steps")
}

data class TeacherContext(
    val classGrade: Int = 6,
    val subject: StudySubject = StudySubject.MATHS,
    val chapter: String = "",
    val language: TeacherLanguage = TeacherLanguage.ENGLISH
)

data class TeacherChatMessage(
    val id: String,
    val isFromStudent: Boolean,
    val text: String,
    val isSetupNotice: Boolean = false,
    val timestampMillis: Long = System.currentTimeMillis()
)

data class HomeworkExplanation(
    val question: String,
    val conceptUsed: String,
    val steps: List<String>,
    val example: String,
    val finalAnswer: String
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)

data class QuizSessionState(
    val topicLabel: String = "",
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswerChecked: Boolean = false,
    val correctCount: Int = 0,
    val answeredCount: Int = 0
) {
    val currentQuestion: QuizQuestion?
        get() = questions.getOrNull(currentIndex)

    val isQuizFinished: Boolean
        get() = questions.isNotEmpty() && currentIndex >= questions.size
}

data class LearningOutput(
    val toolType: LearningToolType,
    val title: String,
    val body: String,
    val examples: List<String> = emptyList()
)

sealed interface AiTeacherResult<out T> {
    data class Success<T>(val data: T) : AiTeacherResult<T>
    data class SetupRequired(val message: String) : AiTeacherResult<Nothing>
    data class Error(val message: String) : AiTeacherResult<Nothing>
}

object AiTeacherSubjects {
    val supported: List<StudySubject> = listOf(
        StudySubject.MATHS,
        StudySubject.SCIENCE,
        StudySubject.ENGLISH,
        StudySubject.HINDI,
        StudySubject.SST
    )
}
