package com.example.hmbuddy.data.repository

import com.example.hmbuddy.auth.AuthManager
import com.example.hmbuddy.data.UserProfile
import com.example.hmbuddy.data.UserProfileDao
import com.example.hmbuddy.data.sync.FirestoreDataSource
import com.example.hmbuddy.data.sync.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserProfileRepository(
    private val userProfileDao: UserProfileDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val authManager: AuthManager,
    private val applicationScope: CoroutineScope
) {
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus

    fun getUserProfile(): Flow<UserProfile?> = userProfileDao.getUserProfile()

    suspend fun saveUserProfile(profile: UserProfile) {
        userProfileDao.saveUserProfile(profile)
        syncToFirestore {
            firestoreDataSource.saveUserProfile(profile)
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
