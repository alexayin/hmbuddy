package com.example.hmbuddy.data.repository

import com.example.hmbuddy.data.RaceGoal
import com.example.hmbuddy.data.RaceGoalDao
import kotlinx.coroutines.flow.Flow

class RaceGoalRepository(
    private val raceGoalDao: RaceGoalDao
) {
    fun getRaceGoal(): Flow<RaceGoal?> = raceGoalDao.getRaceGoal()

    suspend fun saveRaceGoal(raceGoal: RaceGoal) {
        raceGoalDao.insertRaceGoal(raceGoal)
    }

    suspend fun deleteRaceGoal() {
        raceGoalDao.deleteRaceGoal()
    }
}