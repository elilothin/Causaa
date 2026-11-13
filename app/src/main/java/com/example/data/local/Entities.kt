package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val pseudo: String = "",
    val sex: String = "HOMME",
    val age: Int = 22,
    val heightCm: Int = 175,
    val weightKg: Double = 75.0,
    val morphotype: String = "MESOMORPHE",
    val goal: String = "MAINTIEN",
    val targetWeightKg: Double = 75.0,
    val targetDateIso: String = "",
    val daysPerWeek: Int = 4,
    val physicalJob: Boolean = false,
    val usePplForThreeDays: Boolean = false,
    val difficulty: String = "MOYEN",
    val preferredLocation: String = "SALLE",
    val themeMode: String = "BEIGE",
    val customCalorieDeltaPct: Int = -20,
    val waterGoalLiters: Double = 2.0,
    val bottleVolumeLiters: Double = 0.75,
    val xp: Int = 0,
    val skinIndex: Int = 0,
    val outfitIndex: Int = 0,
    val shoeAccentIndex: Int = 0,
    val avatarJson: String = "{}",
    val homeTiersJson: String = "{}", // Map<exerciseId, Int>
    val soundsEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val reduceMotion: Boolean = false,
    val economy30Fps: Boolean = false,
    val workoutReminderEnabled: Boolean = false,
    val workoutReminderHour: Int = 18,
    val workoutReminderMinute: Int = 0,
    val workoutReminderDaysCsv: String = "0,2,4,5",
    val waterReminderEnabled: Boolean = false,
    val waterReminderStartHour: Int = 8,
    val waterReminderEndHour: Int = 21,
    val waterReminderIntervalHours: Int = 2,
    val backupReminderMonthly: Boolean = false,
    val onboardingCompleted: Boolean = false,
)

@Entity(tableName = "workout_logs")
data class WorkoutLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateIso: String, // YYYY-MM-DD
    val timestampMs: Long,
    val sessionName: String,
    val location: String,
    val difficulty: String,
    val completedExerciseIdsCsv: String,
    val skippedExerciseIdsCsv: String,
    val estimatedCaloriesBurned: Int,
    val xpEarned: Int,
)

@Entity(tableName = "food_logs")
data class FoodLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateIso: String,
    val timestampMs: Long,
    val name: String,
    val calories: Int,
    val proteinG: Double,
    val fatG: Double,
    val carbsG: Double,
    val isDirectEntry: Boolean = false,
)

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateIso: String,
    val timestampMs: Long,
    val amountLiters: Double,
)

@Entity(tableName = "weight_logs")
data class WeightLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateIso: String,
    val timestampMs: Long,
    val weightKg: Double,
)

@Entity(tableName = "measurement_logs")
data class MeasurementLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateIso: String,
    val timestampMs: Long,
    val part: String, // BRAS, POITRINE, TAILLE, CUISSES, HANCHES
    val valueCm: Double,
)

@Entity(tableName = "daily_notes")
data class DailyNoteEntity(
    @PrimaryKey val dateIso: String,
    val note: String,
    val updatedMs: Long,
)

@Entity(tableName = "ranked_snapshots")
data class RankedSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateIso: String,
    val timestampMs: Long,
    val rankCode: String,
    val globalScore: Double,
    val maitriseScore: Double,
    val forceScore: Double,
    val explosiviteScore: Double,
    val vitesseScore: Double,
    val enduranceScore: Double,
    val rawValuesJson: String,
)

@Entity(tableName = "day_checkins")
data class DayCheckinEntity(
    @PrimaryKey val dateIso: String,
    val status: String, // WORKOUT_DONE, REST_VALIDATED
    val timestampMs: Long,
)

@Entity(tableName = "unlocked_badges")
data class UnlockedBadgeEntity(
    @PrimaryKey val badgeId: String,
    val unlockedMs: Long,
)
