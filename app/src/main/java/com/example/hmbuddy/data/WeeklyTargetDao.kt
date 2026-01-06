package com.example.hmbuddy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyTargetDao {
    @Query("SELECT * FROM weekly_targets WHERE id = 1")
    fun getWeeklyTarget(): Flow<WeeklyTarget?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWeeklyTarget(target: WeeklyTarget)
}
