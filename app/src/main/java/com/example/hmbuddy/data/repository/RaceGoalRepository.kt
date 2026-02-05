package com.example.hmbuddy.data.repository

import com.example.hmbuddy.auth.AuthManager
import com.example.hmbuddy.data.RaceGoal
import com.example.hmbuddy.data.RaceGoalDao
import com.example.hmbuddy.data.sync.FirestoreDataSource
import com.example.hmbuddy.data.sync.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RaceGoalRepository(
    private val raceGoalDao: RaceGoalDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val authManager: AuthManager,
    private val applicationScope: CoroutineScope
) {
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus

    fun getRaceGoal(): Flow<RaceGoal?> = raceGoalDao.getRaceGoal()

    suspend fun saveRaceGoal(raceGoal: RaceGoal) {
        raceGoalDao.insertRaceGoal(raceGoal)
        syncToFirestore {
            firestoreDataSource.saveRaceGoal(raceGoal)
        }
    }

    suspend fun deleteRaceGoal() {
        raceGoalDao.deleteRaceGoal()
        syncToFirestore {
            firestoreDataSource.deleteRaceGoal()
        }
    }

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
