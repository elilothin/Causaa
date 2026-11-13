package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        WorkoutLogEntity::class,
        FoodLogEntity::class,
        WaterLogEntity::class,
        WeightLogEntity::class,
        MeasurementLogEntity::class,
        DailyNoteEntity::class,
        RankedSnapshotEntity::class,
        DayCheckinEntity::class,
        UnlockedBadgeEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class CausaSuiDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun foodDao(): FoodDao
    abstract fun waterDao(): WaterDao
    abstract fun weightDao(): WeightDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun noteDao(): NoteDao
    abstract fun rankedDao(): RankedDao
    abstract fun checkinDao(): CheckinDao
    abstract fun badgeDao(): BadgeDao

    companion object {
        @Volatile
        private var INSTANCE: CausaSuiDatabase? = null

        fun getInstance(context: Context): CausaSuiDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CausaSuiDatabase::class.java,
                    "causa_sui_local.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
