package com.example

import com.example.data.DateUtils
import com.example.data.StudySubject
import com.example.notifications.StudyReminderScheduler
import com.example.ui.TimerMode
import com.example.ui.TimerUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {
    @Test
    fun allSixSubjectsExistWithExpectedLabels() {
        val names = StudySubject.entries.map { it.displayName }
        assertEquals(
            listOf("Maths", "English", "Science", "Hindi", "SST", "Other"),
            names
        )
        assertEquals(StudySubject.MATHS, StudySubject.fromName("Maths"))
        assertEquals(StudySubject.SST, StudySubject.fromName("sst"))
        assertEquals(StudySubject.OTHER, StudySubject.fromName("unknown"))
    }

    @Test
    fun timerDefaultsAndFormattingAreCorrect() {
        val focus = TimerUiState(mode = TimerMode.FOCUS, remainingSeconds = TimerMode.FOCUS.durationSeconds)
        assertEquals("25:00", focus.formattedTime)
        assertEquals(0f, focus.progressFraction, 0.001f)

        val breakTimer = TimerUiState(mode = TimerMode.BREAK, remainingSeconds = TimerMode.BREAK.durationSeconds)
        assertEquals("05:00", breakTimer.formattedTime)

        val halfway = TimerUiState(mode = TimerMode.FOCUS, remainingSeconds = 750)
        assertEquals("12:30", halfway.formattedTime)
        assertEquals(0.5f, halfway.progressFraction, 0.001f)
    }

    @Test
    fun last7DaysReturnsSevenSequentialSlotsEndingToday() {
        val slots = DateUtils.last7DaySlots()
        assertEquals(7, slots.size)
        assertTrue(slots.last().isToday)
        assertEquals(DateUtils.todayDateKey(), slots.last().dateKey)
    }

    @Test
    fun reminderSchedulerSchedulesTodayIfUpcomingAndTomorrowIfPassed() {
        val nowCal = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 21, 14, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val upcomingMillis = StudyReminderScheduler.calculateNextTriggerMillis(
            hour = 17,
            minute = 30,
            nowMillis = nowCal.timeInMillis
        )
        val upcomingCal = Calendar.getInstance().apply { timeInMillis = upcomingMillis }
        assertEquals(21, upcomingCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(17, upcomingCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, upcomingCal.get(Calendar.MINUTE))

        val passedMillis = StudyReminderScheduler.calculateNextTriggerMillis(
            hour = 9,
            minute = 15,
            nowMillis = nowCal.timeInMillis
        )
        val passedCal = Calendar.getInstance().apply { timeInMillis = passedMillis }
        assertEquals(22, passedCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, passedCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(15, passedCal.get(Calendar.MINUTE))
    }

    @Test
    fun unconfiguredAiTeacherRepositoryRequiresSetupWithoutFakeResponses() = kotlinx.coroutines.test.runTest {
        val repo = com.example.ai.UnconfiguredAiTeacherRepository()
        val ctx = com.example.ai.TeacherContext(
            classGrade = 6,
            subject = StudySubject.SCIENCE,
            chapter = "Light",
            language = com.example.ai.TeacherLanguage.ENGLISH
        )
        val chatResult = repo.askTeacher("What is reflection?", ctx, emptyList())
        assertTrue(chatResult is com.example.ai.AiTeacherResult.SetupRequired)

        val hwResult = repo.explainHomework("Solve 2x + 4 = 10", ctx)
        assertTrue(hwResult is com.example.ai.AiTeacherResult.SetupRequired)

        val quizResult = repo.generateQuiz("Light", 5, ctx)
        assertTrue(quizResult is com.example.ai.AiTeacherResult.SetupRequired)

        val learnResult = repo.generateLearningAid(com.example.ai.LearningToolType.DAILY_TIP, "", ctx)
        assertTrue(learnResult is com.example.ai.AiTeacherResult.SetupRequired)
    }

    @Test
    fun aiTeacherViewModelClassGradeClampsBetweenOneAndTen() {
        val vm = com.example.ui.AiTeacherViewModel()
        vm.selectClassGrade(0)
        assertEquals(1, vm.uiState.value.context.classGrade)
        vm.selectClassGrade(12)
        assertEquals(10, vm.uiState.value.context.classGrade)
        vm.selectClassGrade(8)
        assertEquals(8, vm.uiState.value.context.classGrade)
    }
}
