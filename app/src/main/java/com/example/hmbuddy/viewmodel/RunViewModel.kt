package com.example.hmbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hmbuddy.data.RunLog
import com.example.hmbuddy.data.RunType
import com.example.hmbuddy.data.repository.RunLogRepository
import com.example.hmbuddy.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RunViewModel(private val repository: RunLogRepository) : ViewModel() {

    val allRuns: StateFlow<List<RunLog>> = repository.getAllRuns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _runsThisWeek = MutableStateFlow<List<RunLog>>(emptyList())
    val runsThisWeek: StateFlow<List<RunLog>> = _runsThisWeek

    init {
        loadRunsThisWeek()
    }

    private fun loadRunsThisWeek() {
        val startOfWeek = getStartOfWeekTimestamp()
        viewModelScope.launch {
            repository.getRunsThisWeek(startOfWeek).collect { runs ->
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
            repository.insertRun(run)
        }
    }

    fun deleteRun(run: RunLog) {
        viewModelScope.launch {
            repository.deleteRun(run)
        }
    }

    fun updateRun(run: RunLog) {
        viewModelScope.launch {
            repository.updateRun(run)
        }
    }

    private fun getStartOfWeekTimestamp(): Long {
        return DateUtils.getStartOfWeekTimestamp()
    }

    class Factory(private val repository: RunLogRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RunViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RunViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
