package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observeProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_logs ORDER BY timestampMs DESC")
    fun observeAllWorkouts(): Flow<List<WorkoutLogEntity>>

    @Query("SELECT * FROM workout_logs ORDER BY timestampMs DESC")
    suspend fun getAllWorkoutsOnce(): List<WorkoutLogEntity>

    @Insert
    suspend fun insertWorkout(log: WorkoutLogEntity): Long

    @Query("UPDATE workout_logs SET estimatedCaloriesBurned = :calories WHERE id = :id")
    suspend fun updateWorkoutCalories(id: Long, calories: Int)

    @Query("DELETE FROM workout_logs")
    suspend fun clearAll()

    @Insert
    suspend fun insertAll(logs: List<WorkoutLogEntity>)
}

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_logs WHERE dateIso = :dateIso ORDER BY timestampMs DESC")
    fun observeFoodsForDate(dateIso: String): Flow<List<FoodLogEntity>>

    @Query("SELECT * FROM food_logs ORDER BY timestampMs DESC")
    fun observeAllFoods(): Flow<List<FoodLogEntity>>

    @Query("SELECT * FROM food_logs ORDER BY timestampMs DESC")
    suspend fun getAllFoodsOnce(): List<FoodLogEntity>

    @Insert
    suspend fun insertFood(log: FoodLogEntity): Long

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteFoodById(id: Long)

    @Query("DELETE FROM food_logs")
    suspend fun clearAll()

    @Insert
    suspend fun insertAll(logs: List<FoodLogEntity>)
}

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_logs WHERE dateIso = :dateIso ORDER BY timestampMs DESC")
    fun observeWaterForDate(dateIso: String): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs ORDER BY timestampMs DESC")
    fun observeAllWater(): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs ORDER BY timestampMs DESC")
    suspend fun getAllWaterOnce(): List<WaterLogEntity>

    @Insert
    suspend fun insertWater(log: WaterLogEntity): Long

    @Query("DELETE FROM water_logs WHERE id = (SELECT id FROM water_logs WHERE dateIso = :dateIso ORDER BY timestampMs DESC LIMIT 1)")
    suspend fun deleteLastWaterForDate(dateIso: String)

    @Query("DELETE FROM water_logs")
    suspend fun clearAll()

    @Insert
    suspend fun insertAll(logs: List<WaterLogEntity>)
}

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_logs ORDER BY dateIso ASC, timestampMs ASC")
    fun observeWeights(): Flow<List<WeightLogEntity>>

    @Query("SELECT * FROM weight_logs ORDER BY dateIso ASC, timestampMs ASC")
    suspend fun getAllWeightsOnce(): List<WeightLogEntity>

    @Insert
    suspend fun insertWeight(log: WeightLogEntity): Long

    @Query("DELETE FROM weight_logs")
    suspend fun clearAll()

    @Insert
    suspend fun insertAll(logs: List<WeightLogEntity>)
}

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurement_logs ORDER BY dateIso ASC, timestampMs ASC")
    fun observeMeasurements(): Flow<List<MeasurementLogEntity>>

    @Query("SELECT * FROM measurement_logs ORDER BY dateIso ASC, timestampMs ASC")
    suspend fun getAllMeasurementsOnce(): List<MeasurementLogEntity>

    @Insert
    suspend fun insertMeasurement(log: MeasurementLogEntity): Long

    @Query("DELETE FROM measurement_logs")
    suspend fun clearAll()

    @Insert
    suspend fun insertAll(logs: List<MeasurementLogEntity>)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM daily_notes ORDER BY dateIso DESC")
    fun observeNotes(): Flow<List<DailyNoteEntity>>

    @Query("SELECT * FROM daily_notes ORDER BY dateIso DESC")
    suspend fun getAllNotesOnce(): List<DailyNoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: DailyNoteEntity)

    @Query("DELETE FROM daily_notes")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<DailyNoteEntity>)
}

@Dao
interface RankedDao {
    @Query("SELECT * FROM ranked_snapshots ORDER BY timestampMs ASC")
    fun observeSnapshots(): Flow<List<RankedSnapshotEntity>>

    @Query("SELECT * FROM ranked_snapshots ORDER BY timestampMs ASC")
    suspend fun getAllSnapshotsOnce(): List<RankedSnapshotEntity>

    @Insert
    suspend fun insertSnapshot(snapshot: RankedSnapshotEntity): Long

    @Query("DELETE FROM ranked_snapshots")
    suspend fun clearAll()

    @Insert
    suspend fun insertAll(items: List<RankedSnapshotEntity>)
}

@Dao
interface CheckinDao {
    @Query("SELECT * FROM day_checkins ORDER BY dateIso ASC")
    fun observeCheckins(): Flow<List<DayCheckinEntity>>

    @Query("SELECT * FROM day_checkins ORDER BY dateIso ASC")
    suspend fun getAllCheckinsOnce(): List<DayCheckinEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCheckin(checkin: DayCheckinEntity)

    @Query("DELETE FROM day_checkins")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DayCheckinEntity>)
}

@Dao
interface BadgeDao {
    @Query("SELECT * FROM unlocked_badges ORDER BY unlockedMs ASC")
    fun observeBadges(): Flow<List<UnlockedBadgeEntity>>

    @Query("SELECT * FROM unlocked_badges ORDER BY unlockedMs ASC")
    suspend fun getAllBadgesOnce(): List<UnlockedBadgeEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlockBadge(badge: UnlockedBadgeEntity)

    @Query("DELETE FROM unlocked_badges")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<UnlockedBadgeEntity>)
}
