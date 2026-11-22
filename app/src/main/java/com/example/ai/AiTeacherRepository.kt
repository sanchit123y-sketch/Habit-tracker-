package com.example.ai

/**
 * Single repository boundary for all AI Teacher capabilities.
 *
 * All AI Teacher actions (chat Q&A, homework explanation, one-at-a-time quiz generation,
 * and learning tools) route through this interface so a live Firebase AI Logic backend can
 * be plugged in cleanly without changing ViewModel or UI code.
 */
interface AiTeacherRepository {
    val isApiConnected: Boolean
    val setupRequiredMessage: String

    suspend fun askTeacher(
        question: String,
        context: TeacherContext,
        history: List<TeacherChatMessage>
    ): AiTeacherResult<String>

    suspend fun explainHomework(
        homeworkQuestion: String,
        context: TeacherContext
    ): AiTeacherResult<HomeworkExplanation>

    suspend fun generateQuiz(
        topic: String,
        questionCount: Int,
        context: TeacherContext
    ): AiTeacherResult<List<QuizQuestion>>

    suspend fun generateLearningAid(
        toolType: LearningToolType,
        query: String,
        context: TeacherContext
    ): AiTeacherResult<LearningOutput>
}

/**
 * Safe unconfigured repository used when Firebase AI Logic has not been provisioned.
 *
 * Strictly avoids fabricating fake or hardcoded AI answers and transparently reports
 * that API/Firebase setup is required.
 */
class UnconfiguredAiTeacherRepository : AiTeacherRepository {
    override val isApiConnected: Boolean = false

    override val setupRequiredMessage: String =
        "AI Teacher API is not connected yet. Firebase AI Logic setup is required in AI Studio " +
            "to generate live step-by-step answers, homework explanations, quizzes, and study tips."

    override suspend fun askTeacher(
        question: String,
        context: TeacherContext,
        history: List<TeacherChatMessage>
    ): AiTeacherResult<String> {
        return AiTeacherResult.SetupRequired(setupRequiredMessage)
    }

    override suspend fun explainHomework(
        homeworkQuestion: String,
        context: TeacherContext
    ): AiTeacherResult<HomeworkExplanation> {
        return AiTeacherResult.SetupRequired(setupRequiredMessage)
    }

    override suspend fun generateQuiz(
        topic: String,
        questionCount: Int,
        context: TeacherContext
    ): AiTeacherResult<List<QuizQuestion>> {
        return AiTeacherResult.SetupRequired(setupRequiredMessage)
    }

    override suspend fun generateLearningAid(
        toolType: LearningToolType,
        query: String,
        context: TeacherContext
    ): AiTeacherResult<LearningOutput> {
        return AiTeacherResult.SetupRequired(setupRequiredMessage)
    }
}
