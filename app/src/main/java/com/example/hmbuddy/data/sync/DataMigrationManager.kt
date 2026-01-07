package com.example.hmbuddy.data.sync

import android.content.Context
import android.content.SharedPreferences
import com.example.hmbuddy.auth.AuthManager
import com.example.hmbuddy.data.RunDao
import com.example.hmbuddy.data.UserProfileDao
import com.example.hmbuddy.data.WeeklyAchievementDao
import com.example.hmbuddy.data.WeeklyTargetDao
import kotlinx.coroutines.flow.first

class DataMigrationManager(
    context: Context,
    private val authManager: AuthManager,
    private val firestoreDataSource: FirestoreDataSource,
    private val runDao: RunDao,
    private val userProfileDao: UserProfileDao,
    private val weeklyTargetDao: WeeklyTargetDao,
    private val weeklyAchievementDao: WeeklyAchievementDao
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("hmbuddy_migration", Context.MODE_PRIVATE)

    private val keyMigrationCompleted = "migration_completed_for_user_"

    private fun getMigrationKey(): String =
        keyMigrationCompleted + (authManager.userId ?: "unknown")

    fun isMigrationCompleted(): Boolean =
        prefs.getBoolean(getMigrationKey(), false)

    suspend fun performMigrationIfNeeded() {
        if (isMigrationCompleted()) return
        if (!authManager.isAuthenticated) return

        try {
            // Migrate UserProfile
            userProfileDao.getUserProfile().first()?.let { profile ->
                firestoreDataSource.saveUserProfile(profile)
            }

            // Migrate WeeklyTarget
            weeklyTargetDao.getWeeklyTarget().first()?.let { target ->
                firestoreDataSource.saveWeeklyTarget(target)
            }

            // Migrate all RunLogs
            runDao.getAllRuns().first().forEach { runLog ->
                firestoreDataSource.saveRunLog(runLog)
            }

            // Migrate all WeeklyAchievements
            weeklyAchievementDao.getAllAchievements().first().forEach { achievement ->
                firestoreDataSource.saveWeeklyAchievement(achievement)
            }

            // Mark migration as complete
            prefs.edit().putBoolean(getMigrationKey(), true).apply()

        } catch (e: Exception) {
            // Log error but don't crash - migration can be retried
            e.printStackTrace()
        }
    }
}
