package com.example.hmbuddy.data.repository

import com.example.hmbuddy.auth.AuthManager
import com.example.hmbuddy.data.RunDao
import com.example.hmbuddy.data.WeeklyAchievement
import com.example.hmbuddy.data.WeeklyAchievementDao
import com.example.hmbuddy.data.WeeklyTargetDao
import com.example.hmbuddy.data.sync.FirestoreDataSource
import com.example.hmbuddy.data.sync.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeeklyAchievementRepository(
    private val achievementDao: WeeklyAchievementDao,
    private val runDao: RunDao,
    private val targetDao: WeeklyTargetDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val authManager: AuthManager,
    private val applicationScope: CoroutineScope
) {
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus

    fun getAllAchievements(): Flow<List<WeeklyAchievement>> = achievementDao.getAllAchievements()

    suspend fun getAchievementForWeek(weekStart: Long): WeeklyAchievement? =
        achievementDao.getAchievementForWeek(weekStart)

    suspend fun saveAchievement(achievement: WeeklyAchievement) {
        achievementDao.saveAchievement(achievement)
        syncToFirestore {
            firestoreDataSource.saveWeeklyAchievement(achievement)
        }
    }

    suspend fun getTotalMinutesForWeek(weekStart: Long, weekEnd: Long): Int =
        runDao.getTotalMinutesForWeek(weekStart, weekEnd)

    fun getTotalMinutesFlowForWeek(weekStart: Long): Flow<Int> =
        runDao.getTotalMinutesFlowForWeek(weekStart)

    fun getWeeklyTarget(): Flow<com.example.hmbuddy.data.WeeklyTarget?> =
        targetDao.getWeeklyTarget()

    private fun syncToFirestore(block: suspend () -> Unit) {
        if (!authManager.isAuthenticated) return

        applicationScope.launch(Dispatchers.IO) {
            try {
                _syncStatus.value = SyncStatus.Syncing
                block()
                _syncStatus.value = SyncStatus.Success
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.Error(e.message ?: "Sync failed")
            }
        }
    }
}
