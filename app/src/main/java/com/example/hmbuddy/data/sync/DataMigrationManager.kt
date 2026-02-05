package com.example.hmbuddy.data.sync

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.example.hmbuddy.auth.AuthManager
import com.example.hmbuddy.data.RaceGoalDao
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
    private val weeklyAchievementDao: WeeklyAchievementDao,
    private val raceGoalDao: RaceGoalDao
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("hmbuddy_migration", Context.MODE_PRIVATE)

    private val keyMigrationCompleted = "migration_completed_for_user_"
    private val keyRestoreCompleted = "restore_completed_for_user_"

    private fun getMigrationKey(): String =
        keyMigrationCompleted + (authManager.userId ?: "unknown")

    private fun getRestoreKey(): String =
        keyRestoreCompleted + (authManager.userId ?: "unknown")

    fun isMigrationCompleted(): Boolean =
        prefs.getBoolean(getMigrationKey(), false)

    private fun isRestoreCompleted(): Boolean =
        prefs.getBoolean(getRestoreKey(), false)

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

            // Migrate RaceGoal
            raceGoalDao.getRaceGoal().first()?.let { raceGoal ->
                firestoreDataSource.saveRaceGoal(raceGoal)
            }

            // Mark migration as complete
            prefs.edit { putBoolean(getMigrationKey(), true) }

        } catch (e: Exception) {
            // Log error but don't crash - migration can be retried
            e.printStackTrace()
        }
    }

    /**
     * Restore data from Firestore to local database.
     * This is useful when local database is wiped (e.g., after app update with schema change).
     */
    suspend fun restoreFromFirestore() {
        if (!authManager.isAuthenticated) {
            Log.w("DataMigrationManager", "Cannot restore: not authenticated")
            return
        }

        try {
            Log.d("DataMigrationManager", "Starting restore from Firestore...")

            // Restore UserProfile
            firestoreDataSource.getUserProfile()?.let { profile ->
                userProfileDao.saveUserProfile(profile)
                Log.d("DataMigrationManager", "Restored user profile: ${profile.name}")
            }

            // Restore WeeklyTarget
            firestoreDataSource.getWeeklyTarget()?.let { target ->
                weeklyTargetDao.saveWeeklyTarget(target)
                Log.d("DataMigrationManager", "Restored weekly target")
            }

            // Restore all RunLogs
            val runLogs = firestoreDataSource.getAllRunLogs()
            runLogs.forEach { runLog ->
                runDao.insertRun(runLog)
            }
            Log.d("DataMigrationManager", "Restored ${runLogs.size} run logs")

            // Restore all WeeklyAchievements
            val achievements = firestoreDataSource.getAllAchievements()
            achievements.forEach { achievement ->
                weeklyAchievementDao.saveAchievement(achievement)
            }
            Log.d("DataMigrationManager", "Restored ${achievements.size} achievements")

            // Restore RaceGoal
            firestoreDataSource.getRaceGoal()?.let { raceGoal ->
                raceGoalDao.insertRaceGoal(raceGoal)
                Log.d("DataMigrationManager", "Restored race goal: ${raceGoal.raceName}")
            }

            // Mark restore as complete
            prefs.edit { putBoolean(getRestoreKey(), true) }
            Log.d("DataMigrationManager", "Restore from Firestore completed successfully")

        } catch (e: Exception) {
            Log.e("DataMigrationManager", "Error restoring from Firestore", e)
            e.printStackTrace()
        }
    }

    /**
     * Check if local database is empty and restore from Firestore if needed.
     */
    suspend fun restoreIfLocalDatabaseEmpty() {
        if (!authManager.isAuthenticated) return

        try {
            val hasLocalData = runDao.getAllRuns().first().isNotEmpty() ||
                    userProfileDao.getUserProfile().first() != null ||
                    weeklyTargetDao.getWeeklyTarget().first() != null ||
                    raceGoalDao.getRaceGoal().first() != null

            if (!hasLocalData) {
                Log.d("DataMigrationManager", "Local database is empty, attempting restore from Firestore")
                restoreFromFirestore()
            }
        } catch (e: Exception) {
            Log.e("DataMigrationManager", "Error checking local database", e)
        }
    }
}
