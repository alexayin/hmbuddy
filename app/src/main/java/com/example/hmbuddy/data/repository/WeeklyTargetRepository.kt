package com.example.hmbuddy.data.repository

import com.example.hmbuddy.auth.AuthManager
import com.example.hmbuddy.data.WeeklyTarget
import com.example.hmbuddy.data.WeeklyTargetDao
import com.example.hmbuddy.data.sync.FirestoreDataSource
import com.example.hmbuddy.data.sync.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeeklyTargetRepository(
    private val weeklyTargetDao: WeeklyTargetDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val authManager: AuthManager,
    private val applicationScope: CoroutineScope
) {
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus

    fun getWeeklyTarget(): Flow<WeeklyTarget?> = weeklyTargetDao.getWeeklyTarget()

    suspend fun saveWeeklyTarget(target: WeeklyTarget) {
        weeklyTargetDao.saveWeeklyTarget(target)
        syncToFirestore {
            firestoreDataSource.saveWeeklyTarget(target)
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
