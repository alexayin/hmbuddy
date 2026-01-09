package com.example.hmbuddy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RaceGoalDao {
    @Query("SELECT * FROM race_goals WHERE id = 1")
    fun getRaceGoal(): Flow<RaceGoal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRaceGoal(raceGoal: RaceGoal)

    @Query("DELETE FROM race_goals WHERE id = 1")
    suspend fun deleteRaceGoal()
}