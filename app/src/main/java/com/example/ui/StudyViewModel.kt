package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DateUtils
import com.example.data.StudyDatabase
import com.example.data.StudyRepository
import com.example.data.StudySessionEntity
import com.example.data.StudySubject
import com.example.data.StudyTaskEntity
import com.example.notifications.ReminderPreferencesRepository
import com.example.notifications.ReminderSettings
import com.example.notifications.StudyNotificationHelper
import com.example.notifications.StudyReminderScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TimerMode(val label: String, val durationSeconds: Int) {
    FOCUS("Focus 25m", 25 * 60),
    BREAK("Break 5m", 5 * 60)
}

enum class TaskListTab(val label: String) {
    TODAY("Today"),
    ALL("All Tasks"),
    COMPLETED("Completed")
}

data class DailyChartPoint(
    val dateKey: String,
    val shortDayLabel: String,
    val fullDayLabel: String,
    val studyMinutes: Int,
    val completedTasks: Int,
    val isToday: Boolean
)

data class SubjectProgress(
    val subject: StudySubject,
    val totalTasks: Int,
    val completedTasks: Int,
    val studyMinutes: Int,
    val progressFraction: Float
)

data class WeeklyReportSummary(
    val totalMinutes: Int = 0,
    val averageMinutesPerDay: Int = 0,
    val tasksCompletedThisWeek: Int = 0,
    val bestDayLabel: String = "—",
    val bestDayMinutes: Int = 0
)

data class TimerUiState(
    val mode: TimerMode = TimerMode.FOCUS,
    val remainingSeconds: Int = TimerMode.FOCUS.durationSeconds,
    val isRunning: Boolean = false,
    val selectedSubject: StudySubject = StudySubject.MATHS,
    val activeSessionElapsedMinutes: Int = 0,
    val completedPomodorosToday: Int = 0,
    val lastCompletedMessage: String? = null
) {
    val totalSeconds: Int get() = mode.durationSeconds
    val progressFraction: Float
        get() = if (totalSeconds == 0) 0f
        else 1f - (remainingSeconds.toFloat() / totalSeconds.toFloat())

    val formattedTime: String
        get() {
            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            return "%02d:%02d".format(mins, secs)
        }
}

data class StudyUiState(
    val todayDateKey: String = DateUtils.todayDateKey(),
    val todayFormattedDate: String = DateUtils.formatFullTodayDate(),
    val greeting: String = DateUtils.timeGreeting(),
    val todayTasks: List<StudyTaskEntity> = emptyList(),
    val todayPendingTasks: List<StudyTaskEntity> = emptyList(),
    val todayCompletedTasks: List<StudyTaskEntity> = emptyList(),
    val otherPendingTasks: List<StudyTaskEntity> = emptyList(),
    val allTasks: List<StudyTaskEntity> = emptyList(),
    val completedHistory: List<StudyTaskEntity> = emptyList(),
    val totalCompletedCount: Int = 0,
    val totalPendingCount: Int = 0,
    val dailyProgressPercent: Int = 0,
    val dailyProgressFraction: Float = 0f,
    val todayStudyMinutes: Int = 0,
    val dailyGoalMinutes: Int = 60,
    val dailyGoalProgressFraction: Float = 0f,
    val dailyGoalPercent: Int = 0,
    val isDailyGoalAchieved: Boolean = false,
    val studyStreakDays: Int = 0,
    val dailyChartPoints: List<DailyChartPoint> = emptyList(),
    val weeklyReport: WeeklyReportSummary = WeeklyReportSummary(),
    val subjectProgressList: List<SubjectProgress> = emptyList(),
    val selectedTaskTab: TaskListTab = TaskListTab.TODAY,
    val selectedSubjectFilter: StudySubject? = null,
    val timer: TimerUiState = TimerUiState(),
    val reminderSettings: ReminderSettings = ReminderSettings()
)

private data class FilterState(
    val taskTab: TaskListTab = TaskListTab.TODAY,
    val subjectFilter: StudySubject? = null
)

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository by lazy {
        val dao = StudyDatabase.getInstance(application).studyDao()
        StudyRepository(dao)
    }

    private val reminderRepo: ReminderPreferencesRepository by lazy {
        ReminderPreferencesRepository(application)
    }

    private val filterState = MutableStateFlow(FilterState())
    private val timerState = MutableStateFlow(TimerUiState())
    private var timerJob: Job? = null
    private var focusSecondsBuffer: Int = 0

    val uiState: StateFlow<StudyUiState> = combine(
        repository.allTasks,
        repository.allSessions,
        filterState,
        timerState,
        reminderRepo.settingsFlow
    ) { tasks, sessions, filters, timer, reminderSettings ->
        buildUiState(tasks, sessions, filters, timer, reminderSettings)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StudyUiState()
    )

    private fun buildUiState(
        tasks: List<StudyTaskEntity>,
        sessions: List<StudySessionEntity>,
        filters: FilterState,
        timer: TimerUiState,
        reminderSettings: ReminderSettings
    ): StudyUiState {
        val todayKey = DateUtils.todayDateKey()

        val todayTasks = tasks.filter { it.scheduledDateKey == todayKey }
        val todayPending = todayTasks.filter { !it.isCompleted }
        val todayCompleted = todayTasks.filter { it.isCompleted }
        val otherPending = tasks.filter { !it.isCompleted && it.scheduledDateKey != todayKey }

        val totalCompleted = tasks.count { it.isCompleted }
        val totalPending = tasks.count { !it.isCompleted }

        val todayMinutes = sessions
            .filter { it.dateKey == todayKey }
            .sumOf { it.durationMinutes }

        val dailyGoalMinutes = 60
        val goalFraction = (todayMinutes.toFloat() / dailyGoalMinutes.toFloat()).coerceIn(0f, 1f)
        val goalPercent = ((todayMinutes * 100) / dailyGoalMinutes).coerceIn(0, 100)
        val isGoalAchieved = todayMinutes >= dailyGoalMinutes

        val dailyProgressFraction = if (todayTasks.isNotEmpty()) {
            todayCompleted.size.toFloat() / todayTasks.size.toFloat()
        } else {
            goalFraction
        }
        val dailyProgressPercent = (dailyProgressFraction * 100).toInt().coerceIn(0, 100)

        val completedHistory = tasks
            .filter { it.isCompleted }
            .sortedByDescending { it.completedAtMillis ?: it.createdAtMillis }

        val last7Days = DateUtils.last7DaySlots()
        val chartPoints = last7Days.map { slot ->
            val dayMinutes = sessions
                .filter { it.dateKey == slot.dateKey }
                .sumOf { it.durationMinutes }
            val dayCompletedTasks = tasks.count { task ->
                task.isCompleted && (task.completedDateKey ?: task.scheduledDateKey) == slot.dateKey
            }
            DailyChartPoint(
                dateKey = slot.dateKey,
                shortDayLabel = slot.shortDayLabel,
                fullDayLabel = slot.fullDayLabel,
                studyMinutes = dayMinutes,
                completedTasks = dayCompletedTasks,
                isToday = slot.isToday
            )
        }

        val weeklyMinutes = chartPoints.sumOf { it.studyMinutes }
        val weeklyTasksCompleted = chartPoints.sumOf { it.completedTasks }
        val bestDay = chartPoints.maxByOrNull { it.studyMinutes }
        val weeklyReport = WeeklyReportSummary(
            totalMinutes = weeklyMinutes,
            averageMinutesPerDay = weeklyMinutes / 7,
            tasksCompletedThisWeek = weeklyTasksCompleted,
            bestDayLabel = if ((bestDay?.studyMinutes ?: 0) > 0) bestDay!!.shortDayLabel else "—",
            bestDayMinutes = bestDay?.studyMinutes ?: 0
        )

        val subjectProgress = StudySubject.entries.map { subject ->
            val subjectTasks = tasks.filter { StudySubject.fromName(it.subjectName) == subject }
            val completedCount = subjectTasks.count { it.isCompleted }
            val subjectMinutes = sessions
                .filter { StudySubject.fromName(it.subjectName) == subject }
                .sumOf { it.durationMinutes }
            val fraction = if (subjectTasks.isNotEmpty()) {
                completedCount.toFloat() / subjectTasks.size.toFloat()
            } else if (subjectMinutes > 0) {
                (subjectMinutes / 60f).coerceIn(0.15f, 1f)
            } else {
                0f
            }
            SubjectProgress(
                subject = subject,
                totalTasks = subjectTasks.size,
                completedTasks = completedCount,
                studyMinutes = subjectMinutes,
                progressFraction = fraction
            )
        }

        val streak = calculateStreakDays(tasks, sessions)

        return StudyUiState(
            todayDateKey = todayKey,
            todayFormattedDate = DateUtils.formatFullTodayDate(),
            greeting = DateUtils.timeGreeting(),
            todayTasks = todayTasks,
            todayPendingTasks = todayPending,
            todayCompletedTasks = todayCompleted,
            otherPendingTasks = otherPending,
            allTasks = tasks,
            completedHistory = completedHistory,
            totalCompletedCount = totalCompleted,
            totalPendingCount = totalPending,
            dailyProgressPercent = dailyProgressPercent,
            dailyProgressFraction = dailyProgressFraction,
            todayStudyMinutes = todayMinutes,
            dailyGoalMinutes = dailyGoalMinutes,
            dailyGoalProgressFraction = goalFraction,
            dailyGoalPercent = goalPercent,
            isDailyGoalAchieved = isGoalAchieved,
            studyStreakDays = streak,
            dailyChartPoints = chartPoints,
            weeklyReport = weeklyReport,
            subjectProgressList = subjectProgress,
            selectedTaskTab = filters.taskTab,
            selectedSubjectFilter = filters.subjectFilter,
            timer = timer,
            reminderSettings = reminderSettings
        )
    }

    private fun calculateStreakDays(
        tasks: List<StudyTaskEntity>,
        sessions: List<StudySessionEntity>
    ): Int {
        val activeDates = buildSet {
            sessions.filter { it.durationMinutes > 0 }.forEach { add(it.dateKey) }
            tasks.filter { it.isCompleted }.forEach { task ->
                add(task.completedDateKey ?: task.scheduledDateKey)
            }
        }
        if (activeDates.isEmpty()) return 0

        val todayKey = DateUtils.todayDateKey()
        var offset = if (activeDates.contains(todayKey)) 0 else 1
        var streak = 0
        while (offset < 365) {
            val key = DateUtils.dateKeyOffset(offset)
            if (activeDates.contains(key)) {
                streak++
                offset++
            } else {
                break
            }
        }
        return streak
    }

    // --- Task Actions ---

    fun addTask(
        title: String,
        notes: String,
        subject: StudySubject,
        forToday: Boolean
    ) {
        val dateKey = if (forToday) DateUtils.todayDateKey() else DateUtils.tomorrowDateKey()
        viewModelScope.launch {
            repository.addTask(
                title = title,
                notes = notes,
                subject = subject,
                scheduledDateKey = dateKey
            )
        }
    }

    fun toggleTaskCompletion(task: StudyTaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun selectTaskTab(tab: TaskListTab) {
        filterState.update { it.copy(taskTab = tab) }
    }

    fun selectSubjectFilter(subject: StudySubject?) {
        filterState.update { it.copy(subjectFilter = subject) }
    }

    // --- Timer & Study Minutes Actions ---

    fun selectTimerMode(mode: TimerMode) {
        timerJob?.cancel()
        focusSecondsBuffer = 0
        timerState.update {
            it.copy(
                mode = mode,
                remainingSeconds = mode.durationSeconds,
                isRunning = false,
                activeSessionElapsedMinutes = 0,
                lastCompletedMessage = null
            )
        }
    }

    fun selectTimerSubject(subject: StudySubject) {
        timerState.update { it.copy(selectedSubject = subject) }
    }

    fun startTimer() {
        if (timerState.value.isRunning) return
        timerState.update { it.copy(isRunning = true, lastCompletedMessage = null) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (timerState.value.isRunning && timerState.value.remainingSeconds > 0) {
                delay(1_000L)
                val current = timerState.value
                if (!current.isRunning) break

                val nextRemaining = (current.remainingSeconds - 1).coerceAtLeast(0)

                if (current.mode == TimerMode.FOCUS) {
                    focusSecondsBuffer++
                    if (focusSecondsBuffer >= 60) {
                        focusSecondsBuffer = 0
                        val subject = current.selectedSubject
                        viewModelScope.launch {
                            repository.recordStudyMinutes(
                                minutes = 1,
                                subject = subject,
                                sessionType = "FOCUS"
                            )
                        }
                        timerState.update {
                            it.copy(
                                activeSessionElapsedMinutes = it.activeSessionElapsedMinutes + 1
                            )
                        }
                    }
                }

                if (nextRemaining == 0) {
                    focusSecondsBuffer = 0
                    val finishedMode = current.mode
                    if (finishedMode == TimerMode.FOCUS) {
                        timerState.update {
                            it.copy(
                                isRunning = false,
                                mode = TimerMode.BREAK,
                                remainingSeconds = TimerMode.BREAK.durationSeconds,
                                completedPomodorosToday = it.completedPomodorosToday + 1,
                                activeSessionElapsedMinutes = 0,
                                lastCompletedMessage = "25-min Focus complete! Ready for a 5-min break."
                            )
                        }
                    } else {
                        timerState.update {
                            it.copy(
                                isRunning = false,
                                mode = TimerMode.FOCUS,
                                remainingSeconds = TimerMode.FOCUS.durationSeconds,
                                activeSessionElapsedMinutes = 0,
                                lastCompletedMessage = "Break complete! Ready for your next focus session."
                            )
                        }
                    }
                    break
                } else {
                    timerState.update { it.copy(remainingSeconds = nextRemaining) }
                }
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        timerState.update { it.copy(isRunning = false) }
    }

    fun resetTimer() {
        timerJob?.cancel()
        focusSecondsBuffer = 0
        timerState.update {
            it.copy(
                isRunning = false,
                remainingSeconds = it.mode.durationSeconds,
                activeSessionElapsedMinutes = 0,
                lastCompletedMessage = null
            )
        }
    }

    fun logQuickStudyMinutes(minutes: Int, subject: StudySubject = timerState.value.selectedSubject) {
        viewModelScope.launch {
            repository.recordStudyMinutes(
                minutes = minutes,
                subject = subject,
                sessionType = "MANUAL"
            )
        }
    }

    // --- Daily Reminder Actions ---

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            reminderRepo.updateEnabled(enabled)
            val current = reminderRepo.getSettingsOnce()
            val app = getApplication<Application>()
            if (enabled) {
                StudyReminderScheduler.scheduleDailyReminder(app, current.hour, current.minute)
            } else {
                StudyReminderScheduler.cancelReminder(app)
            }
        }
    }

    fun setReminderTime(hour: Int, minute: Int, autoEnable: Boolean = true) {
        viewModelScope.launch {
            reminderRepo.updateTime(hour, minute)
            if (autoEnable) {
                reminderRepo.updateEnabled(true)
            }
            val current = reminderRepo.getSettingsOnce()
            val app = getApplication<Application>()
            if (current.enabled) {
                StudyReminderScheduler.scheduleDailyReminder(app, hour, minute)
            }
        }
    }

    fun setSkipIfGoalMet(skip: Boolean) {
        viewModelScope.launch {
            reminderRepo.updateSkipIfGoalMet(skip)
        }
    }

    fun sendTestReminderNotification(): Boolean {
        return StudyNotificationHelper.showStudyReminderNotification(
            context = getApplication(),
            todayStudyMinutes = uiState.value.todayStudyMinutes,
            isTest = true
        )
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
