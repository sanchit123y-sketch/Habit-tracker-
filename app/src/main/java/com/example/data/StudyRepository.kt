package com.example.data

import kotlinx.coroutines.flow.Flow

class StudyRepository(private val studyDao: StudyDao) {

    val allTasks: Flow<List<StudyTaskEntity>> = studyDao.observeAllTasks()
    val allSessions: Flow<List<StudySessionEntity>> = studyDao.observeAllSessions()

    suspend fun addTask(
        title: String,
        notes: String,
        subject: StudySubject,
        scheduledDateKey: String
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        studyDao.insertTask(
            StudyTaskEntity(
                title = cleanTitle,
                notes = notes.trim(),
                subjectName = subject.name,
                scheduledDateKey = scheduledDateKey,
                isCompleted = false,
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun toggleTaskCompletion(task: StudyTaskEntity) {
        val nowCompleted = !task.isCompleted
        val updated = task.copy(
            isCompleted = nowCompleted,
            completedAtMillis = if (nowCompleted) System.currentTimeMillis() else null,
            completedDateKey = if (nowCompleted) DateUtils.todayDateKey() else null
        )
        studyDao.updateTask(updated)
    }

    suspend fun deleteTask(taskId: Long) {
        studyDao.deleteTaskById(taskId)
    }

    suspend fun recordStudyMinutes(
        minutes: Int,
        subject: StudySubject,
        sessionType: String = "FOCUS"
    ) {
        if (minutes <= 0) return
        studyDao.insertSession(
            StudySessionEntity(
                dateKey = DateUtils.todayDateKey(),
                durationMinutes = minutes,
                subjectName = subject.name,
                sessionType = sessionType,
                timestampMillis = System.currentTimeMillis()
            )
        )
    }
}
