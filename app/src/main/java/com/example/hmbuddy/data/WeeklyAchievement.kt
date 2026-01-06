package com.example.hmbuddy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Records the achievement status for a completed week.
 * Each record represents one week's performance snapshot.
 */
@Entity(tableName = "weekly_achievements")
data class WeeklyAchievement(
    @PrimaryKey
    val weekStartTimestamp: Long,
    val targetMinutes: Int,
    val actualMinutes: Int,
    val goalAchieved: Boolean,
    val recordedAt: Long = System.currentTimeMillis()
)
