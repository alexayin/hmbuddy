package com.example.hmbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hmbuddy.data.WeeklyTarget
import com.example.hmbuddy.data.WeeklyTargetDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TargetViewModel(private val weeklyTargetDao: WeeklyTargetDao) : ViewModel() {

    val weeklyTarget: StateFlow<WeeklyTarget?> = weeklyTargetDao.getWeeklyTarget()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveWeeklyTarget(
        zone2PaceSecondsPerKm: Int,
        tempoPaceSecondsPerKm: Int,
        weeklyDurationMinutes: Int
    ) {
        viewModelScope.launch {
            val target = WeeklyTarget(
                zone2PaceSecondsPerKm = zone2PaceSecondsPerKm,
                tempoPaceSecondsPerKm = tempoPaceSecondsPerKm,
                weeklyDurationMinutes = weeklyDurationMinutes
            )
            weeklyTargetDao.saveWeeklyTarget(target)
        }
    }

    class Factory(private val weeklyTargetDao: WeeklyTargetDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TargetViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TargetViewModel(weeklyTargetDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
