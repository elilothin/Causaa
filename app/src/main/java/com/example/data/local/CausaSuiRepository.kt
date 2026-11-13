package com.example.data.local

import com.example.data.model.DayStatus
import com.example.data.model.ExerciseDef
import com.example.data.model.MeasurementPart
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateUtilsCompat {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun todayIso(): String = formatDate(Calendar.getInstance())

    fun formatDate(cal: Calendar): String {
        synchronized(isoFormat) {
            return isoFormat.format(cal.time)
        }
    }

    fun parseIso(iso: String): Calendar? = runCatching {
        synchronized(isoFormat) {
            val date = isoFormat.parse(iso) ?: return null
            Calendar.getInstance().apply { time = date }
        }
    }.getOrNull()

    fun addDays(iso: String, deltaDays: Int): String {
        val cal = parseIso(iso) ?: Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, deltaDays)
        return formatDate(cal)
    }

    /** 0 = LUN .. 6 = DIM */
    fun dayOfWeekIndex(cal: Calendar = Calendar.getInstance()): Int {
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            else -> 6
        }
    }

    fun dayOfYear(): Int = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

    fun currentMonthDays(): List<Pair<Int, String>> {
        val cal = Calendar.getInstance()
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        return (1..maxDay).map { day ->
            val c = Calendar.getInstance().apply {
                set(year, month, day, 12, 0, 0)
            }
            day to formatDate(c)
        }
    }

    fun currentMonthLabel(): String {
        val fmt = SimpleDateFormat("MMMM yyyy", Locale.FRENCH)
        return fmt.format(Calendar.getInstance().time).replaceFirstChar { it.uppercase() }
    }

    fun formatShortDisplay(iso: String): String {
        val cal = parseIso(iso) ?: return iso
        val fmt = SimpleDateFormat("dd/MM", Locale.FRANCE)
        return fmt.format(cal.time)
    }
}

class CausaSuiRepository(private val db: CausaSuiDatabase) {

    val profile: Flow<UserProfileEntity?> = db.profileDao().observeProfile()
    val workouts: Flow<List<WorkoutLogEntity>> = db.workoutDao().observeAllWorkouts()
    val allFoods: Flow<List<FoodLogEntity>> = db.foodDao().observeAllFoods()
    val allWater: Flow<List<WaterLogEntity>> = db.waterDao().observeAllWater()
    val weights: Flow<List<WeightLogEntity>> = db.weightDao().observeWeights()
    val measurements: Flow<List<MeasurementLogEntity>> = db.measurementDao().observeMeasurements()
    val notes: Flow<List<DailyNoteEntity>> = db.noteDao().observeNotes()
    val rankedSnapshots: Flow<List<RankedSnapshotEntity>> = db.rankedDao().observeSnapshots()
    val checkins: Flow<List<DayCheckinEntity>> = db.checkinDao().observeCheckins()
    val unlockedBadges: Flow<List<UnlockedBadgeEntity>> = db.badgeDao().observeBadges()

    suspend fun getProfileOnce(): UserProfileEntity? = db.profileDao().getProfileOnce()

    suspend fun saveProfile(entity: UserProfileEntity) {
        db.profileDao().upsertProfile(entity)
    }

    suspend fun logWeight(weightKg: Double, dateIso: String = DateUtilsCompat.todayIso()) {
        db.weightDao().insertWeight(
            WeightLogEntity(
                dateIso = dateIso,
                timestampMs = System.currentTimeMillis(),
                weightKg = weightKg,
            )
        )
        val p = db.profileDao().getProfileOnce()
        if (p != null) {
            db.profileDao().upsertProfile(p.copy(weightKg = weightKg))
        }
    }

    suspend fun logWater(amountLiters: Double, dateIso: String = DateUtilsCompat.todayIso()) {
        db.waterDao().insertWater(
            WaterLogEntity(
                dateIso = dateIso,
                timestampMs = System.currentTimeMillis(),
                amountLiters = amountLiters,
            )
        )
    }

    suspend fun undoLastWater(dateIso: String = DateUtilsCompat.todayIso()) {
        db.waterDao().deleteLastWaterForDate(dateIso)
    }

    suspend fun logFood(
        name: String,
        calories: Int,
        proteinG: Double,
        fatG: Double,
        carbsG: Double,
        isDirect: Boolean,
        dateIso: String = DateUtilsCompat.todayIso(),
    ) {
        db.foodDao().insertFood(
            FoodLogEntity(
                dateIso = dateIso,
                timestampMs = System.currentTimeMillis(),
                name = name,
                calories = calories,
                proteinG = proteinG,
                fatG = fatG,
                carbsG = carbsG,
                isDirectEntry = isDirect,
            )
        )
    }

    suspend fun deleteFood(id: Long) {
        db.foodDao().deleteFoodById(id)
    }

    suspend fun logWorkoutAndValidateDay(
        sessionName: String,
        location: String,
        difficulty: String,
        completedExerciseIds: List<String>,
        skippedExerciseIds: List<String>,
        caloriesBurned: Int,
        xpEarned: Int,
        dateIso: String = DateUtilsCompat.todayIso(),
    ) {
        val now = System.currentTimeMillis()
        db.workoutDao().insertWorkout(
            WorkoutLogEntity(
                dateIso = dateIso,
                timestampMs = now,
                sessionName = sessionName,
                location = location,
                difficulty = difficulty,
                completedExerciseIdsCsv = completedExerciseIds.joinToString(","),
                skippedExerciseIdsCsv = skippedExerciseIds.joinToString(","),
                estimatedCaloriesBurned = caloriesBurned,
                xpEarned = xpEarned,
            )
        )
        db.checkinDao().upsertCheckin(
            DayCheckinEntity(
                dateIso = dateIso,
                status = DayStatus.WORKOUT_DONE.name,
                timestampMs = now,
            )
        )
        val p = db.profileDao().getProfileOnce()
        if (p != null) {
            db.profileDao().upsertProfile(p.copy(xp = p.xp + xpEarned))
        }
    }

    suspend fun updateWorkoutCalories(workoutId: Long, calories: Int) {
        db.workoutDao().updateWorkoutCalories(workoutId, calories.coerceAtLeast(0))
    }

    suspend fun validateRestDay(xpBonus: Int = 25, dateIso: String = DateUtilsCompat.todayIso()) {
        val now = System.currentTimeMillis()
        db.checkinDao().upsertCheckin(
            DayCheckinEntity(
                dateIso = dateIso,
                status = DayStatus.REST_VALIDATED.name,
                timestampMs = now,
            )
        )
        val p = db.profileDao().getProfileOnce()
        if (p != null) {
            db.profileDao().upsertProfile(p.copy(xp = p.xp + xpBonus))
        }
    }

    suspend fun logMeasurement(part: MeasurementPart, valueCm: Double, dateIso: String = DateUtilsCompat.todayIso()) {
        db.measurementDao().insertMeasurement(
            MeasurementLogEntity(
                dateIso = dateIso,
                timestampMs = System.currentTimeMillis(),
                part = part.name,
                valueCm = valueCm,
            )
        )
    }

    suspend fun saveNote(dateIso: String, text: String) {
        db.noteDao().upsertNote(
            DailyNoteEntity(
                dateIso = dateIso,
                note = text,
                updatedMs = System.currentTimeMillis(),
            )
        )
    }

    suspend fun saveRankedSnapshot(snapshot: RankedSnapshotEntity) {
        db.rankedDao().insertSnapshot(snapshot)
    }

    suspend fun unlockBadge(badgeId: String) {
        db.badgeDao().unlockBadge(UnlockedBadgeEntity(badgeId, System.currentTimeMillis()))
    }

    suspend fun exportBackupJson(): String = BackupManager.exportJson(db)

    suspend fun importBackupJson(json: String): Result<Unit> = BackupManager.importJson(db, json)
}
