package com.example.hmbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hmbuddy.data.RunDao
import com.example.hmbuddy.data.RunLog
import com.example.hmbuddy.data.RunType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class RunViewModel(private val runDao: RunDao) : ViewModel() {

    val allRuns: StateFlow<List<RunLog>> = runDao.getAllRuns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _runsThisWeek = MutableStateFlow<List<RunLog>>(emptyList())
    val runsThisWeek: StateFlow<List<RunLog>> = _runsThisWeek

    init {
        loadRunsThisWeek()
    }

    private fun loadRunsThisWeek() {
        val startOfWeek = getStartOfWeekTimestamp()
        viewModelScope.launch {
            runDao.getRunsThisWeek(startOfWeek).collect { runs ->
                _runsThisWeek.value = runs
            }
        }
    }

    fun logRun(durationMinutes: Int, runType: RunType, paceSecondsPerKm: Int, date: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            val run = RunLog(
                date = date,
                durationMinutes = durationMinutes,
                runType = runType,
                paceSecondsPerKm = paceSecondsPerKm
            )
            runDao.insertRun(run)
        }
    }

    fun deleteRun(run: RunLog) {
        viewModelScope.launch {
            runDao.deleteRun(run)
        }
    }

    fun updateRun(run: RunLog) {
        viewModelScope.launch {
            runDao.updateRun(run)
        }
    }

    private fun getStartOfWeekTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    class Factory(private val runDao: RunDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RunViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RunViewModel(runDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
