package com.example.hmbuddy.data.model

import com.google.firebase.Timestamp

data class UserProfileDocument(
    val name: String = "",
    val gender: String = "MALE",
    val age: Int = 0,
    val updatedAt: Timestamp = Timestamp.now()
)

data class WeeklyTargetDocument(
    val zone2PaceSecondsPerKm: Int = 0,
    val tempoPaceSecondsPerKm: Int = 0,
    val weeklyDurationMinutes: Int = 0,
    val zone2Note: String = "",
    val tempoNote: String = "",
    val updatedAt: Timestamp = Timestamp.now()
)

data class RunLogDocument(
    val localId: Long = 0,
    val date: Long = 0,
    val durationMinutes: Int = 0,
    val runType: String = "ZONE2",
    val paceSecondsPerKm: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class WeeklyAchievementDocument(
    val weekStartTimestamp: Long = 0,
    val targetMinutes: Int = 0,
    val actualMinutes: Int = 0,
    val goalAchieved: Boolean = false,
    val recordedAt: Long = 0
)

data class RaceGoalDocument(
    val raceName: String = "",
    val raceDate: String = "", // ISO format e.g. "2026-03-15"
    val targetTimeSeconds: Int? = null,
    val updatedAt: Timestamp = Timestamp.now()
)
