package com.example.hmbuddy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyAchievementDao {

    @Query("SELECT * FROM weekly_achievements ORDER BY weekStartTimestamp DESC")
    fun getAllAchievements(): Flow<List<WeeklyAchievement>>

    @Query("SELECT * FROM weekly_achievements WHERE weekStartTimestamp = :weekStart")
    suspend fun getAchievementForWeek(weekStart: Long): WeeklyAchievement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAchievement(achievement: WeeklyAchievement)
}
