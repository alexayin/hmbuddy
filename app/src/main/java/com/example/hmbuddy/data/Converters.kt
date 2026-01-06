package com.example.hmbuddy.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromRunType(value: RunType): String {
        return value.name
    }

    @TypeConverter
    fun toRunType(value: String): RunType {
        return RunType.valueOf(value)
    }

    @TypeConverter
    fun fromGender(value: Gender): String {
        return value.name
    }

    @TypeConverter
    fun toGender(value: String): Gender {
        return Gender.valueOf(value)
    }
}
