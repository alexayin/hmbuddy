package com.example.hmbuddy

import android.app.Application
import com.example.hmbuddy.auth.AuthManager
import com.example.hmbuddy.data.AppDatabase
import com.example.hmbuddy.data.repository.RaceGoalRepository
import com.example.hmbuddy.data.repository.RunLogRepository
import com.example.hmbuddy.data.repository.UserProfileRepository
import com.example.hmbuddy.data.repository.WeeklyAchievementRepository
import com.example.hmbuddy.data.repository.WeeklyTargetRepository
import com.example.hmbuddy.data.sync.DataMigrationManager
import com.example.hmbuddy.data.sync.FirestoreDataSource
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HmBuddyApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this) }
    val authManager by lazy { AuthManager() }
    val firestore by lazy { FirebaseFirestore.getInstance() }
    val firestoreDataSource by lazy { FirestoreDataSource(firestore, authManager) }

    val userProfileRepository by lazy {
        UserProfileRepository(
            database.userProfileDao(),
            firestoreDataSource,
            authManager,
            applicationScope
        )
    }

    val weeklyTargetRepository by lazy {
        WeeklyTargetRepository(
            database.weeklyTargetDao(),
            firestoreDataSource,
            authManager,
            applicationScope
        )
    }

    val raceGoalRepository by lazy {
        RaceGoalRepository(
            database.raceGoalDao(),
            firestoreDataSource,
            authManager,
            applicationScope
        )
    }

    val runLogRepository by lazy {
        RunLogRepository(
            database.runDao(),
            firestoreDataSource,
            authManager,
            applicationScope
        )
    }

    val weeklyAchievementRepository by lazy {
        WeeklyAchievementRepository(
            database.weeklyAchievementDao(),
            database.runDao(),
            database.weeklyTargetDao(),
            firestoreDataSource,
            authManager,
            applicationScope
        )
    }

    private val migrationManager by lazy {
        DataMigrationManager(
            this,
            authManager,
            firestoreDataSource,
            database.runDao(),
            database.userProfileDao(),
            database.weeklyTargetDao(),
            database.weeklyAchievementDao(),
            database.raceGoalDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        applicationScope.launch {
            try {
                authManager.ensureAuthenticated()
                // First try to restore from Firestore if local DB is empty
                migrationManager.restoreIfLocalDatabaseEmpty()
                // Then perform migration if needed (uploads local to Firestore)
                migrationManager.performMigrationIfNeeded()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
