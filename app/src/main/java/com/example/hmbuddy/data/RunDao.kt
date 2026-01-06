package com.example.hmbuddy.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {
    @Query("SELECT * FROM run_logs ORDER BY date DESC")
    fun getAllRuns(): Flow<List<RunLog>>

    @Query("SELECT * FROM run_logs WHERE date >= :startOfWeek ORDER BY date DESC")
    fun getRunsThisWeek(startOfWeek: Long): Flow<List<RunLog>>

    @Insert
    suspend fun insertRun(run: RunLog)

    @Update
    suspend fun updateRun(run: RunLog)

    @Delete
    suspend fun deleteRun(run: RunLog)
}
