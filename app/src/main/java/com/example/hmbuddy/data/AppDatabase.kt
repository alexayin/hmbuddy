package com.example.hmbuddy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [RunLog::class, WeeklyTarget::class, UserProfile::class, WeeklyAchievement::class, RaceGoal::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun runDao(): RunDao
    abstract fun weeklyTargetDao(): WeeklyTargetDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun weeklyAchievementDao(): WeeklyAchievementDao
    abstract fun raceGoalDao(): RaceGoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE weekly_targets ADD COLUMN zone2Note TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE weekly_targets ADD COLUMN tempoNote TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "running_buddy_database"
                ).addMigrations(MIGRATION_4_5).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
