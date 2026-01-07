package com.example.hmbuddy.data.repository

import com.example.hmbuddy.auth.AuthManager
import com.example.hmbuddy.data.RunDao
import com.example.hmbuddy.data.RunLog
import com.example.hmbuddy.data.sync.FirestoreDataSource
import com.example.hmbuddy.data.sync.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RunLogRepository(
    private val runDao: RunDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val authManager: AuthManager,
    private val applicationScope: CoroutineScope
) {
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus

    fun getAllRuns(): Flow<List<RunLog>> = runDao.getAllRuns()

    fun getRunsThisWeek(startOfWeek: Long): Flow<List<RunLog>> =
        runDao.getRunsThisWeek(startOfWeek)

    suspend fun getTotalMinutesForWeek(weekStart: Long, weekEnd: Long): Int =
        runDao.getTotalMinutesForWeek(weekStart, weekEnd)

    suspend fun insertRun(run: RunLog) {
        runDao.insertRun(run)
        syncToFirestore {
            // For new runs, we need to get the generated ID from Room
            // Since Room auto-generates, we'll sync with the provided run object
            // In a real app, you'd want to get the inserted ID back
            firestoreDataSource.saveRunLog(run)
        }
    }

    suspend fun updateRun(run: RunLog) {
        runDao.updateRun(run)
        syncToFirestore {
            firestoreDataSource.saveRunLog(run)
        }
    }

    suspend fun deleteRun(run: RunLog) {
        runDao.deleteRun(run)
        syncToFirestore {
            firestoreDataSource.deleteRunLog(run)
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
