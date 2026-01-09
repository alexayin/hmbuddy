package com.example.hmbuddy.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "race_goals")
data class RaceGoal(
    @PrimaryKey
    val id: Int = 1, // Single row for current goal
    val raceName: String,
    val raceDate: LocalDate,
    val targetTimeSeconds: Int? = null // Optional, e.g., 6300 = 1:45:00
)
