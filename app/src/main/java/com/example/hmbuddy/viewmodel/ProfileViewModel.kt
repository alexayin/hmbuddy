package com.example.hmbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hmbuddy.data.Gender
import com.example.hmbuddy.data.UserProfile
import com.example.hmbuddy.data.UserProfileDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(private val userProfileDao: UserProfileDao) : ViewModel() {

    val userProfile: StateFlow<UserProfile?> = userProfileDao.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveUserProfile(name: String, gender: Gender, age: Int) {
        viewModelScope.launch {
            val profile = UserProfile(
                name = name,
                gender = gender,
                age = age
            )
            userProfileDao.saveUserProfile(profile)
        }
    }

    class Factory(private val userProfileDao: UserProfileDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(userProfileDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
