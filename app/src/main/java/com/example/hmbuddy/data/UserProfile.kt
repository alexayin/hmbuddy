package com.example.hmbuddy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Gender {
    MALE,
    FEMALE
}

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1, // Single row for current user
    val name: String,
    val gender: Gender,
    val age: Int
)
