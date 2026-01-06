package com.example.hmbuddy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RunType {
    ZONE2,
    TEMPO
}

@Entity(tableName = "run_logs")
data class RunLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long, // timestamp in milliseconds
    val durationMinutes: Int,
    val runType: RunType,
    val paceSecondsPerKm: Int // stored as total seconds for easier calculations
)
