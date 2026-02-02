package com.example.hmbuddy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weekly_targets")
data class WeeklyTarget(
    @PrimaryKey
    val id: Int = 1, // Single row for current targets
    val zone2PaceSecondsPerKm: Int, // e.g., 390 = 6:30 min/km
    val tempoPaceSecondsPerKm: Int, // e.g., 300 = 5:00 min/km
    val weeklyDurationMinutes: Int, // total target duration for the week
    val zone2Note: String = "",
    val tempoNote: String = ""
)
