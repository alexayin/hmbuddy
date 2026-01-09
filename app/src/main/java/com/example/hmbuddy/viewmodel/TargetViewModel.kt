package com.example.hmbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hmbuddy.data.RaceGoal
import com.example.hmbuddy.data.WeeklyTarget
import com.example.hmbuddy.data.repository.RaceGoalRepository
import com.example.hmbuddy.data.repository.WeeklyTargetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class TargetViewModel(
    private val weeklyTargetRepository: WeeklyTargetRepository,
    private val raceGoalRepository: RaceGoalRepository
) : ViewModel() {

    val weeklyTarget: StateFlow<WeeklyTarget?> = weeklyTargetRepository.getWeeklyTarget()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val raceGoal: StateFlow<RaceGoal?> = raceGoalRepository.getRaceGoal()
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
            weeklyTargetRepository.saveWeeklyTarget(target)
        }
    }

    fun saveRaceGoal(
        raceName: String,
        raceDate: LocalDate,
        targetTimeSeconds: Int?
    ) {
        viewModelScope.launch {
            val goal = RaceGoal(
                raceName = raceName,
                raceDate = raceDate,
                targetTimeSeconds = targetTimeSeconds
            )
            raceGoalRepository.saveRaceGoal(goal)
        }
    }

    fun clearRaceGoal() {
        viewModelScope.launch {
            raceGoalRepository.deleteRaceGoal()
        }
    }

    class Factory(
        private val weeklyTargetRepository: WeeklyTargetRepository,
        private val raceGoalRepository: RaceGoalRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TargetViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TargetViewModel(weeklyTargetRepository, raceGoalRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
