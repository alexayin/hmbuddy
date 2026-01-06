package com.example.hmbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hmbuddy.data.RunDao
import com.example.hmbuddy.data.WeeklyAchievement
import com.example.hmbuddy.data.WeeklyAchievementDao
import com.example.hmbuddy.data.WeeklyTargetDao
import com.example.hmbuddy.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class WeeklyAchievementViewModel(
    private val achievementDao: WeeklyAchievementDao,
    private val runDao: RunDao,
    private val targetDao: WeeklyTargetDao
) : ViewModel() {

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak

    private val _achievements = MutableStateFlow<List<WeeklyAchievement>>(emptyList())
    val achievements: StateFlow<List<WeeklyAchievement>> = _achievements

    init {
        loadAchievements()
        checkAndRecordPreviousWeek()
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            achievementDao.getAllAchievements().collect { achievementList ->
                _achievements.value = achievementList
                _currentStreak.value = calculateStreak(achievementList)
            }
        }
    }

    private fun checkAndRecordPreviousWeek() {
        viewModelScope.launch {
            val currentWeekStart = DateUtils.getStartOfWeekTimestamp()
            val previousWeekStart = DateUtils.getStartOfPreviousWeekTimestamp()
            val previousWeekEnd = currentWeekStart

            val existingRecord = achievementDao.getAchievementForWeek(previousWeekStart)
            if (existingRecord != null) {
                return@launch
            }

            val weeklyTarget = targetDao.getWeeklyTarget().first() ?: return@launch

            val totalMinutes = runDao.getTotalMinutesForWeek(previousWeekStart, previousWeekEnd)

            if (totalMinutes > 0 || weeklyTarget.weeklyDurationMinutes > 0) {
                val achievement = WeeklyAchievement(
                    weekStartTimestamp = previousWeekStart,
                    targetMinutes = weeklyTarget.weeklyDurationMinutes,
                    actualMinutes = totalMinutes,
                    goalAchieved = totalMinutes >= weeklyTarget.weeklyDurationMinutes
                )
                achievementDao.saveAchievement(achievement)
            }
        }
    }

    private fun calculateStreak(achievements: List<WeeklyAchievement>): Int {
        val currentWeekStart = DateUtils.getStartOfWeekTimestamp()

        val pastWeekAchievements = achievements.filter {
            it.weekStartTimestamp < currentWeekStart
        }

        var streak = 0
        var expectedWeekStart = DateUtils.getStartOfPreviousWeekTimestamp()

        for (achievement in pastWeekAchievements) {
            if (achievement.weekStartTimestamp == expectedWeekStart && achievement.goalAchieved) {
                streak++
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = expectedWeekStart
                calendar.add(Calendar.WEEK_OF_YEAR, -1)
                expectedWeekStart = calendar.timeInMillis
            } else {
                break
            }
        }

        return streak
    }

    class Factory(
        private val achievementDao: WeeklyAchievementDao,
        private val runDao: RunDao,
        private val targetDao: WeeklyTargetDao
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WeeklyAchievementViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WeeklyAchievementViewModel(achievementDao, runDao, targetDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
