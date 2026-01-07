package com.example.hmbuddy.data.sync

import com.example.hmbuddy.auth.AuthManager
import com.example.hmbuddy.data.Gender
import com.example.hmbuddy.data.RunLog
import com.example.hmbuddy.data.RunType
import com.example.hmbuddy.data.UserProfile
import com.example.hmbuddy.data.WeeklyAchievement
import com.example.hmbuddy.data.WeeklyTarget
import com.example.hmbuddy.data.model.RunLogDocument
import com.example.hmbuddy.data.model.UserProfileDocument
import com.example.hmbuddy.data.model.WeeklyAchievementDocument
import com.example.hmbuddy.data.model.WeeklyTargetDocument
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreDataSource(
    private val firestore: FirebaseFirestore,
    private val authManager: AuthManager
) {
    private fun userDocument() = firestore.collection("users").document(authManager.userId ?: "anonymous")

    // UserProfile operations
    suspend fun saveUserProfile(profile: UserProfile) {
        val doc = UserProfileDocument(
            name = profile.name,
            gender = profile.gender.name,
            age = profile.age,
            updatedAt = Timestamp.now()
        )
        userDocument().collection("profile").document("current")
            .set(doc, SetOptions.merge()).await()
    }

    fun observeUserProfile(): Flow<UserProfile?> = callbackFlow {
        val listener = userDocument().collection("profile").document("current")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val doc = snapshot?.toObject(UserProfileDocument::class.java)
                val profile = doc?.let {
                    UserProfile(
                        name = it.name,
                        gender = Gender.valueOf(it.gender),
                        age = it.age
                    )
                }
                trySend(profile)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getUserProfile(): UserProfile? {
        val snapshot = userDocument().collection("profile").document("current").get().await()
        val doc = snapshot.toObject(UserProfileDocument::class.java) ?: return null
        return UserProfile(
            name = doc.name,
            gender = Gender.valueOf(doc.gender),
            age = doc.age
        )
    }

    // WeeklyTarget operations
    suspend fun saveWeeklyTarget(target: WeeklyTarget) {
        val doc = WeeklyTargetDocument(
            zone2PaceSecondsPerKm = target.zone2PaceSecondsPerKm,
            tempoPaceSecondsPerKm = target.tempoPaceSecondsPerKm,
            weeklyDurationMinutes = target.weeklyDurationMinutes,
            updatedAt = Timestamp.now()
        )
        userDocument().collection("targets").document("current")
            .set(doc, SetOptions.merge()).await()
    }

    fun observeWeeklyTarget(): Flow<WeeklyTarget?> = callbackFlow {
        val listener = userDocument().collection("targets").document("current")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val doc = snapshot?.toObject(WeeklyTargetDocument::class.java)
                val target = doc?.let {
                    WeeklyTarget(
                        zone2PaceSecondsPerKm = it.zone2PaceSecondsPerKm,
                        tempoPaceSecondsPerKm = it.tempoPaceSecondsPerKm,
                        weeklyDurationMinutes = it.weeklyDurationMinutes
                    )
                }
                trySend(target)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getWeeklyTarget(): WeeklyTarget? {
        val snapshot = userDocument().collection("targets").document("current").get().await()
        val doc = snapshot.toObject(WeeklyTargetDocument::class.java) ?: return null
        return WeeklyTarget(
            zone2PaceSecondsPerKm = doc.zone2PaceSecondsPerKm,
            tempoPaceSecondsPerKm = doc.tempoPaceSecondsPerKm,
            weeklyDurationMinutes = doc.weeklyDurationMinutes
        )
    }

    // RunLog operations
    suspend fun saveRunLog(run: RunLog) {
        val doc = RunLogDocument(
            localId = run.id,
            date = run.date,
            durationMinutes = run.durationMinutes,
            runType = run.runType.name,
            paceSecondsPerKm = run.paceSecondsPerKm,
            updatedAt = Timestamp.now()
        )
        userDocument().collection("runLogs").document(run.id.toString())
            .set(doc, SetOptions.merge()).await()
    }

    suspend fun deleteRunLog(run: RunLog) {
        userDocument().collection("runLogs").document(run.id.toString()).delete().await()
    }

    suspend fun getAllRunLogs(): List<RunLog> {
        val snapshot = userDocument().collection("runLogs").get().await()
        return snapshot.documents.mapNotNull { doc ->
            val data = doc.toObject(RunLogDocument::class.java) ?: return@mapNotNull null
            RunLog(
                id = data.localId,
                date = data.date,
                durationMinutes = data.durationMinutes,
                runType = RunType.valueOf(data.runType),
                paceSecondsPerKm = data.paceSecondsPerKm
            )
        }
    }

    fun observeRunLogs(): Flow<List<RunLog>> = callbackFlow {
        val listener = userDocument().collection("runLogs")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val runs = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.toObject(RunLogDocument::class.java) ?: return@mapNotNull null
                    RunLog(
                        id = data.localId,
                        date = data.date,
                        durationMinutes = data.durationMinutes,
                        runType = RunType.valueOf(data.runType),
                        paceSecondsPerKm = data.paceSecondsPerKm
                    )
                } ?: emptyList()
                trySend(runs)
            }
        awaitClose { listener.remove() }
    }

    // WeeklyAchievement operations
    suspend fun saveWeeklyAchievement(achievement: WeeklyAchievement) {
        val doc = WeeklyAchievementDocument(
            weekStartTimestamp = achievement.weekStartTimestamp,
            targetMinutes = achievement.targetMinutes,
            actualMinutes = achievement.actualMinutes,
            goalAchieved = achievement.goalAchieved,
            recordedAt = achievement.recordedAt
        )
        userDocument().collection("achievements").document(achievement.weekStartTimestamp.toString())
            .set(doc, SetOptions.merge()).await()
    }

    suspend fun getAllAchievements(): List<WeeklyAchievement> {
        val snapshot = userDocument().collection("achievements").get().await()
        return snapshot.documents.mapNotNull { doc ->
            val data = doc.toObject(WeeklyAchievementDocument::class.java) ?: return@mapNotNull null
            WeeklyAchievement(
                weekStartTimestamp = data.weekStartTimestamp,
                targetMinutes = data.targetMinutes,
                actualMinutes = data.actualMinutes,
                goalAchieved = data.goalAchieved,
                recordedAt = data.recordedAt
            )
        }
    }

    fun observeAchievements(): Flow<List<WeeklyAchievement>> = callbackFlow {
        val listener = userDocument().collection("achievements")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val achievements = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.toObject(WeeklyAchievementDocument::class.java) ?: return@mapNotNull null
                    WeeklyAchievement(
                        weekStartTimestamp = data.weekStartTimestamp,
                        targetMinutes = data.targetMinutes,
                        actualMinutes = data.actualMinutes,
                        goalAchieved = data.goalAchieved,
                        recordedAt = data.recordedAt
                    )
                } ?: emptyList()
                trySend(achievements)
            }
        awaitClose { listener.remove() }
    }
}
