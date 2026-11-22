package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateKey: String,
    val durationMinutes: Int,
    val subjectName: String,
    val sessionType: String = "FOCUS",
    val timestampMillis: Long = System.currentTimeMillis()
)
