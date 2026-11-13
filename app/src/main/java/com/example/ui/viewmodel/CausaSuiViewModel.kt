package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.FeedbackManager
import com.example.core.ReminderScheduler
import com.example.core.SoundCue
import com.example.data.catalog.ExerciseCatalog
import com.example.data.catalog.NutritionCalculator
import com.example.data.catalog.PunchlinesCatalog
import com.example.data.catalog.RankedCalculator
import com.example.data.local.*
import com.example.data.model.*
import com.example.ui.theme.CharacterOutfitOptions
import com.example.ui.theme.CharacterShoeAccentOptions
import com.example.ui.theme.CharacterSkinOptions
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

enum class MainTab(val shortLabel: String, val fullLabel: String) {
    ACCUEIL("Accueil", "Accueil"),
    PROGRAMME("Prog.", "Programme"),
    NUTRITION("Nutri.", "Nutrition"),
    PROGRES("Progrès", "Progrès"),
    MOI("Moi", "Moi"),
}

sealed interface AppOverlay {
    data object None : AppOverlay
    data class WorkoutPlayer(val session: ActiveWorkoutState) : AppOverlay
    data object RankedQuestionnaire : AppOverlay
    data object Settings : AppOverlay
    data object Backup : AppOverlay
    data object AvatarBuilder : AppOverlay
}

data class ActiveWorkoutState(
    val sessionName: String,
    val location: WorkoutLocation,
    val difficulty: DifficultyLevel,
    val exerciseIds: List<String>,
    val currentIndex: Int = 0,
    val completedIds: List<String> = emptyList(),
    val skippedIds: List<String> = emptyList(),
    val isSummaryStage: Boolean = false,
    val editableCaloriesBurned: Int = 0,
    val earnedXp: Int = 0,
    val summaryPunchline: String = "",
    val saved: Boolean = false,
)

data class CausaSuiUiState(
    val isLoading: Boolean = true,
    val profile: UserProfileEntity = UserProfileEntity(),
    val activeTab: MainTab = MainTab.ACCUEIL,
    val overlay: AppOverlay = AppOverlay.None,
    val themeMode: AppThemeMode = AppThemeMode.BEIGE,
    val nutritionTargets: NutritionTargets = NutritionCalculator.calculateTargets(
        Sex.HOMME, 22, 175, 75.0, Morphotype.MESOMORPHE, FitnessGoal.MAINTIEN, 4, false
    ),
    val todayFoods: List<FoodLogEntity> = emptyList(),
    val allFoods: List<FoodLogEntity> = emptyList(),
    val todayWaterLiters: Double = 0.0,
    val allWater: List<WaterLogEntity> = emptyList(),
    val todayBurnedKcal: Int = 0,
    val workouts: List<WorkoutLogEntity> = emptyList(),
    val weights: List<WeightLogEntity> = emptyList(),
    val measurements: List<MeasurementLogEntity> = emptyList(),
    val notes: List<DailyNoteEntity> = emptyList(),
    val rankedSnapshots: List<RankedSnapshotEntity> = emptyList(),
    val latestRank: RankTier = RankTier.F,
    val latestPillarScores: List<PillarScore> = emptyList(),
    val checkins: List<DayCheckinEntity> = emptyList(),
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val completionRatePct: Int = 0,
    val weeklyPlan: List<DayPlanItem> = ExerciseCatalog.getWeeklyPlan(4),
    val todayPlan: DayPlanItem = ExerciseCatalog.getWeeklyPlan(4).first(),
    val todayValidated: Boolean = false,
    val unlockedBadgeIds: Set<String> = emptySet(),
    val avatarConfig: AvatarConfig = AvatarConfig(),
    val homeTiers: Map<String, Int> = emptyMap(),
    val characterParams: CharacterVisualParams = CharacterVisualParams(
        Sex.HOMME, 22, 175, 75.0, Morphotype.MESOMORPHE, 1, false,
        CharacterSkinOptions[0], CharacterOutfitOptions[0], CharacterShoeAccentOptions[0]
    ),
    val dailyPunchline: String = PunchlinesCatalog.dailyPunchline(1),
    val rapidWeightLossWarning: Boolean = false,
    val toastMessage: String? = null,
)

class CausaSuiViewModel(application: Application) : AndroidViewModel(application) {

    private val db = CausaSuiDatabase.getInstance(application)
    private val repo = CausaSuiRepository(db)
    val feedback = FeedbackManager(application)

    private val _activeTab = MutableStateFlow(MainTab.ACCUEIL)
    private val _overlay = MutableStateFlow<AppOverlay>(AppOverlay.None)
    private val _toast = MutableStateFlow<String?>(null)

    private val combinedData = combine(
        repo.profile,
        repo.workouts,
        repo.allFoods,
        repo.allWater,
        repo.weights,
    ) { profile, workouts, foods, water, weights ->
        FiveTuple(profile, workouts, foods, water, weights)
    }

    private val secondaryData = combine(
        repo.measurements,
        repo.notes,
        repo.rankedSnapshots,
        repo.checkins,
        repo.unlockedBadges,
    ) { meas, notes, ranked, checkins, badges ->
        FiveTuple(meas, notes, ranked, checkins, badges)
    }

    val uiState: StateFlow<CausaSuiUiState> = combine(
        combinedData,
        secondaryData,
        _activeTab,
        _overlay,
        _toast,
    ) { primary, secondary, tab, overlay, toast ->
        val p = primary.first ?: UserProfileEntity()
        val workouts = primary.second
        val allFoods = primary.third
        val allWater = primary.fourth
        val weights = primary.fifth

        val measurements = secondary.first
        val notes = secondary.second
        val ranked = secondary.third
        val checkins = secondary.fourth
        val badges = secondary.fifth

        val todayIso = DateUtilsCompat.todayIso()
        val sex = runCatching { Sex.valueOf(p.sex) }.getOrDefault(Sex.HOMME)
        val morph = runCatching { Morphotype.valueOf(p.morphotype) }.getOrDefault(Morphotype.MESOMORPHE)
        val goal = runCatching { FitnessGoal.valueOf(p.goal) }.getOrDefault(FitnessGoal.MAINTIEN)
        val theme = runCatching { AppThemeMode.valueOf(p.themeMode) }.getOrDefault(AppThemeMode.BEIGE)

        val targets = NutritionCalculator.calculateTargets(
            sex = sex,
            age = p.age,
            heightCm = p.heightCm,
            weightKg = p.weightKg,
            morphotype = morph,
            goal = goal,
            daysPerWeek = p.daysPerWeek,
            physicalJob = p.physicalJob,
            customLossPercent = p.customCalorieDeltaPct,
            customGainPercent = p.customCalorieDeltaPct.coerceIn(5, 20),
        )

        val todayFoods = allFoods.filter { it.dateIso == todayIso }
        val todayWater = allWater.filter { it.dateIso == todayIso }.sumOf { it.amountLiters }
        val todayBurned = workouts.filter { it.dateIso == todayIso }.sumOf { it.estimatedCaloriesBurned }

        val plan = ExerciseCatalog.getWeeklyPlan(p.daysPerWeek, p.usePplForThreeDays)
        val todayDow = DateUtilsCompat.dayOfWeekIndex()
        val todayPlan = plan.getOrElse(todayDow) { plan.first() }
        val checkinMap = checkins.associateBy { it.dateIso }
        val todayValidated = checkinMap.containsKey(todayIso)

        val (currentStreak, bestStreak, ratePct) = computeStreaks(checkins, todayIso)

        val latestSnapshot = ranked.lastOrNull()
        val latestRank = latestSnapshot?.let { RankTier.fromCode(it.rankCode) } ?: RankTier.F
        val pillarScores = if (latestSnapshot != null) {
            val rawVals = BackupManager.rankedValuesFromJson(latestSnapshot.rawValuesJson)
            listOf(
                PillarScore(RankedPillar.MAITRISE, latestSnapshot.maitriseScore, RankedCalculator.tests.count { it.pillar == RankedPillar.MAITRISE && (rawVals[it.id] ?: 0f) > 0f }),
                PillarScore(RankedPillar.FORCE, latestSnapshot.forceScore, RankedCalculator.tests.count { it.pillar == RankedPillar.FORCE && (rawVals[it.id] ?: 0f) > 0f }),
                PillarScore(RankedPillar.EXPLOSIVITE, latestSnapshot.explosiviteScore, RankedCalculator.tests.count { it.pillar == RankedPillar.EXPLOSIVITE && (rawVals[it.id] ?: 0f) > 0f }),
                PillarScore(RankedPillar.VITESSE, latestSnapshot.vitesseScore, RankedCalculator.tests.count { it.pillar == RankedPillar.VITESSE && (rawVals[it.id] ?: 0f) > 0f }),
                PillarScore(RankedPillar.ENDURANCE, latestSnapshot.enduranceScore, RankedCalculator.tests.count { it.pillar == RankedPillar.ENDURANCE && (rawVals[it.id] ?: 0f) > 0f }),
            )
        } else {
            RankedPillar.entries.map { PillarScore(it, 0.0, 0) }
        }

        val charParams = CharacterVisualParams(
            sex = sex,
            age = p.age,
            heightCm = p.heightCm,
            weightKg = p.weightKg,
            morphotype = morph,
            muscleStage = latestRank.muscleStage,
            hasGoldAura = latestRank.hasGoldAura,
            skinColor = CharacterSkinOptions.getOrElse(p.skinIndex) { CharacterSkinOptions[0] },
            outfitColor = CharacterOutfitOptions.getOrElse(p.outfitIndex) { CharacterOutfitOptions[0] },
            shoeAccentColor = CharacterShoeAccentOptions.getOrElse(p.shoeAccentIndex) { CharacterShoeAccentOptions[0] },
        )

        val rapidLoss = checkRapidWeightLoss(weights)

        CausaSuiUiState(
            isLoading = false,
            profile = p,
            activeTab = tab,
            overlay = overlay,
            themeMode = theme,
            nutritionTargets = targets,
            todayFoods = todayFoods,
            allFoods = allFoods,
            todayWaterLiters = todayWater,
            allWater = allWater,
            todayBurnedKcal = todayBurned,
            workouts = workouts,
            weights = weights,
            measurements = measurements,
            notes = notes,
            rankedSnapshots = ranked,
            latestRank = latestRank,
            latestPillarScores = pillarScores,
            checkins = checkins,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            completionRatePct = ratePct,
            weeklyPlan = plan,
            todayPlan = todayPlan,
            todayValidated = todayValidated,
            unlockedBadgeIds = badges.map { it.badgeId }.toSet(),
            avatarConfig = BackupManager.avatarFromJson(p.avatarJson),
            homeTiers = BackupManager.tiersFromJson(p.homeTiersJson),
            characterParams = charParams,
            dailyPunchline = PunchlinesCatalog.dailyPunchline(DateUtilsCompat.dayOfYear()),
            rapidWeightLossWarning = rapidLoss,
            toastMessage = toast,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CausaSuiUiState(),
    )

    fun selectTab(tab: MainTab) {
        _activeTab.value = tab
    }

    fun openOverlay(overlay: AppOverlay) {
        _overlay.value = overlay
    }

    fun closeOverlay() {
        _overlay.value = AppOverlay.None
    }

    fun clearToast() {
        _toast.value = null
    }

    fun showToast(msg: String) {
        _toast.value = msg
    }

    fun toggleTheme() {
        val current = uiState.value.profile
        val next = if (current.themeMode == AppThemeMode.BEIGE.name) AppThemeMode.DARK.name else AppThemeMode.BEIGE.name
        updateProfile(current.copy(themeMode = next))
    }

    fun completeOnboarding(profile: UserProfileEntity) {
        viewModelScope.launch {
            val finalized = profile.copy(id = 1, onboardingCompleted = true)
            repo.saveProfile(finalized)
            repo.logWeight(finalized.weightKg)
            showToast("Profil et programme générés. Forge-toi.")
        }
    }

    fun updateProfile(updated: UserProfileEntity, logWeightIfChanged: Boolean = false) {
        viewModelScope.launch {
            val prevWeight = uiState.value.profile.weightKg
            repo.saveProfile(updated.copy(id = 1))
            if (logWeightIfChanged && kotlin.math.abs(updated.weightKg - prevWeight) >= 0.05) {
                repo.logWeight(updated.weightKg)
            }
            scheduleAppReminders(updated)
        }
    }

    fun logNewWeight(weightKg: Double) {
        if (weightKg !in 35.0..250.0) {
            showToast("Poids hors bornes réalistes (35 à 250 kg).")
            return
        }
        viewModelScope.launch {
            repo.logWeight(weightKg)
            showToast("Poids enregistré : ${"%.1f".format(weightKg)} kg")
        }
    }

    fun setWorkoutLocation(location: WorkoutLocation) {
        val p = uiState.value.profile
        updateProfile(p.copy(preferredLocation = location.name))
    }

    fun setDifficulty(difficulty: DifficultyLevel) {
        val p = uiState.value.profile
        updateProfile(p.copy(difficulty = difficulty.name))
    }

    fun setThreeDayPpl(usePpl: Boolean) {
        val p = uiState.value.profile
        updateProfile(p.copy(usePplForThreeDays = usePpl))
    }

    fun startWorkout(dayPlan: DayPlanItem) {
        if (dayPlan.isRestDay || dayPlan.exerciseIds.isEmpty()) return
        val p = uiState.value.profile
        val loc = runCatching { WorkoutLocation.valueOf(p.preferredLocation) }.getOrDefault(WorkoutLocation.SALLE)
        val diff = runCatching { DifficultyLevel.valueOf(p.difficulty) }.getOrDefault(DifficultyLevel.MOYEN)
        feedback.vibrateShort(p.hapticsEnabled)
        _overlay.value = AppOverlay.WorkoutPlayer(
            ActiveWorkoutState(
                sessionName = dayPlan.sessionName,
                location = loc,
                difficulty = diff,
                exerciseIds = dayPlan.exerciseIds,
            )
        )
    }

    fun advanceWorkout(skipCurrent: Boolean) {
        val currentOverlay = _overlay.value as? AppOverlay.WorkoutPlayer ?: return
        val session = currentOverlay.session
        val p = uiState.value.profile
        val currentId = session.exerciseIds.getOrNull(session.currentIndex) ?: return

        val nextCompleted = if (!skipCurrent) session.completedIds + currentId else session.completedIds
        val nextSkipped = if (skipCurrent) session.skippedIds + currentId else session.skippedIds

        if (!skipCurrent) {
            feedback.vibrateShort(p.hapticsEnabled)
            feedback.playCue(SoundCue.NEXT_EXERCISE, p.soundsEnabled)
        }

        val nextIdx = session.currentIndex + 1
        if (nextIdx >= session.exerciseIds.size) {
            val completedDefs = nextCompleted.mapNotNull { ExerciseCatalog.exercises[it] }
            val estKcal = NutritionCalculator.estimateWorkoutCalories(p.weightKg, completedDefs, session.difficulty)
            val xp = max(20, nextCompleted.size * 22)
            _overlay.value = AppOverlay.WorkoutPlayer(
                session.copy(
                    completedIds = nextCompleted,
                    skippedIds = nextSkipped,
                    isSummaryStage = true,
                    editableCaloriesBurned = estKcal,
                    earnedXp = xp,
                    summaryPunchline = PunchlinesCatalog.randomFor(PunchlineCategory.VALIDATION),
                )
            )
        } else {
            _overlay.value = AppOverlay.WorkoutPlayer(
                session.copy(
                    currentIndex = nextIdx,
                    completedIds = nextCompleted,
                    skippedIds = nextSkipped,
                )
            )
        }
    }

    fun updateSummaryCalories(kcal: Int) {
        val currentOverlay = _overlay.value as? AppOverlay.WorkoutPlayer ?: return
        _overlay.value = AppOverlay.WorkoutPlayer(
            currentOverlay.session.copy(editableCaloriesBurned = kcal.coerceIn(0, 3000))
        )
    }

    fun upgradeHomeVariant(exerciseId: String) {
        val def = ExerciseCatalog.exercises[exerciseId] ?: return
        val p = uiState.value.profile
        val tiers = BackupManager.tiersFromJson(p.homeTiersJson).toMutableMap()
        val cur = tiers[exerciseId] ?: 0
        val next = (cur + 1).coerceAtMost(def.homeVariants.lastIndex)
        tiers[exerciseId] = next
        updateProfile(p.copy(homeTiersJson = BackupManager.tiersToJson(tiers)))
        showToast("Variante maison relevée : ${def.homeVariants[next]}")
    }

    fun validateAndSaveWorkout() {
        val currentOverlay = _overlay.value as? AppOverlay.WorkoutPlayer ?: return
        val session = currentOverlay.session
        val p = uiState.value.profile
        viewModelScope.launch {
            repo.logWorkoutAndValidateDay(
                sessionName = session.sessionName,
                location = session.location.name,
                difficulty = session.difficulty.name,
                completedExerciseIds = session.completedIds,
                skippedExerciseIds = session.skippedIds,
                caloriesBurned = session.editableCaloriesBurned,
                xpEarned = session.earnedXp,
            )
            feedback.vibrateVictory(p.hapticsEnabled)
            feedback.playCue(SoundCue.WORKOUT_DONE, p.soundsEnabled)
            evaluateAndUnlockBadges(newXp = p.xp + session.earnedXp)
            _overlay.value = AppOverlay.None
            showToast("Séance validée · +${session.earnedXp} XP")
        }
    }

    fun validateRestDay() {
        val p = uiState.value.profile
        viewModelScope.launch {
            repo.validateRestDay()
            feedback.vibrateShort(p.hapticsEnabled)
            feedback.playCue(SoundCue.REST_VALIDATED, p.soundsEnabled)
            evaluateAndUnlockBadges(newXp = p.xp + 25)
            showToast(PunchlinesCatalog.randomFor(PunchlineCategory.REPOS))
        }
    }

    fun addWater(liters: Double) {
        val p = uiState.value.profile
        viewModelScope.launch {
            repo.logWater(liters)
            feedback.vibrateShort(p.hapticsEnabled)
            feedback.playCue(SoundCue.WATER_ADD, p.soundsEnabled)
            evaluateAndUnlockBadges()
        }
    }

    fun undoWater() {
        viewModelScope.launch {
            repo.undoLastWater()
            showToast("Dernier ajout d'eau annulé.")
        }
    }

    fun addFoodFromCatalog(food: FoodItem, servings: Double) {
        val mult = servings.coerceIn(0.25, 10.0)
        viewModelScope.launch {
            repo.logFood(
                name = if (mult == 1.0) food.name else "${food.name} ×${"%.1f".format(mult)}",
                calories = (food.calories * mult).roundToInt(),
                proteinG = food.proteinG * mult,
                fatG = food.fatG * mult,
                carbsG = food.carbsG * mult,
                isDirect = false,
            )
            showToast("${food.name} ajouté.")
        }
    }

    fun addDirectCalories(label: String, calories: Int) {
        if (calories <= 0) return
        viewModelScope.launch {
            repo.logFood(
                name = label.ifBlank { "Saisie directe" },
                calories = calories,
                proteinG = 0.0,
                fatG = 0.0,
                carbsG = 0.0,
                isDirect = true,
            )
            showToast("+$calories kcal ajoutées.")
        }
    }

    fun deleteFoodLog(id: Long) {
        viewModelScope.launch {
            repo.deleteFood(id)
        }
    }

    fun addMeasurement(part: MeasurementPart, valueCm: Double) {
        if (valueCm !in 10.0..250.0) {
            showToast("Mesure invalide (10 à 250 cm).")
            return
        }
        viewModelScope.launch {
            repo.logMeasurement(part, valueCm)
            showToast("${part.label} : ${"%.1f".format(valueCm)} cm enregistrés.")
        }
    }

    fun saveDailyNote(dateIso: String, text: String) {
        viewModelScope.launch {
            repo.saveNote(dateIso, text.trim())
            showToast("Note enregistrée.")
        }
    }

    fun submitRanked(rawValues: Map<String, Float>) {
        val p = uiState.value.profile
        val sex = runCatching { Sex.valueOf(p.sex) }.getOrDefault(Sex.HOMME)
        val prevRank = uiState.value.latestRank
        val (pillars, newRank) = RankedCalculator.evaluate(rawValues, sex, p.weightKg)
        val globalScore = pillars.map { it.score }.average()

        viewModelScope.launch {
            repo.saveRankedSnapshot(
                RankedSnapshotEntity(
                    dateIso = DateUtilsCompat.todayIso(),
                    timestampMs = System.currentTimeMillis(),
                    rankCode = newRank.code,
                    globalScore = globalScore,
                    maitriseScore = pillars.first { it.pillar == RankedPillar.MAITRISE }.score,
                    forceScore = pillars.first { it.pillar == RankedPillar.FORCE }.score,
                    explosiviteScore = pillars.first { it.pillar == RankedPillar.EXPLOSIVITE }.score,
                    vitesseScore = pillars.first { it.pillar == RankedPillar.VITESSE }.score,
                    enduranceScore = pillars.first { it.pillar == RankedPillar.ENDURANCE }.score,
                    rawValuesJson = BackupManager.rankedValuesToJson(rawValues),
                )
            )
            repo.unlockBadge("first_rank")
            if (newRank.levelIndex >= RankTier.A.levelIndex) {
                repo.unlockBadge("rank_a")
            }
            if (newRank.levelIndex > prevRank.levelIndex) {
                feedback.vibrateVictory(p.hapticsEnabled)
                feedback.playCue(SoundCue.RANK_UP, p.soundsEnabled)
            } else {
                feedback.vibrateShort(p.hapticsEnabled)
                feedback.playCue(SoundCue.NEXT_EXERCISE, p.soundsEnabled)
            }
            _overlay.value = AppOverlay.None
            showToast("Rang calculé : ${newRank.code} (${newRank.title})")
        }
    }

    fun saveAvatar(config: AvatarConfig) {
        val p = uiState.value.profile
        updateProfile(p.copy(avatarJson = BackupManager.avatarToJson(config)))
        _overlay.value = AppOverlay.None
        showToast("Avatar mis à jour.")
    }

    fun updateCharacterColors(skinIdx: Int, outfitIdx: Int, shoeIdx: Int) {
        val p = uiState.value.profile
        updateProfile(
            p.copy(
                skinIndex = skinIdx.coerceIn(0, CharacterSkinOptions.lastIndex),
                outfitIndex = outfitIdx.coerceIn(0, CharacterOutfitOptions.lastIndex),
                shoeAccentIndex = shoeIdx.coerceIn(0, CharacterShoeAccentOptions.lastIndex),
            )
        )
    }

    suspend fun exportBackupJson(): String = repo.exportBackupJson()

    fun importBackupJson(json: String) {
        viewModelScope.launch {
            val res = repo.importBackupJson(json)
            if (res.isSuccess) {
                _overlay.value = AppOverlay.None
                showToast("Sauvegarde restaurée avec succès.")
            } else {
                showToast(res.exceptionOrNull()?.message ?: "Fichier de sauvegarde invalide.")
            }
        }
    }

    private fun scheduleAppReminders(p: UserProfileEntity) {
        ReminderScheduler.scheduleReminders(
            context = getApplication(),
            workoutEnabled = p.workoutReminderEnabled,
            workoutHour = p.workoutReminderHour,
            workoutMinute = p.workoutReminderMinute,
            waterEnabled = p.waterReminderEnabled,
            waterStartHour = p.waterReminderStartHour,
            waterIntervalHours = p.waterReminderIntervalHours,
            backupMonthlyEnabled = p.backupReminderMonthly,
        )
    }

    private suspend fun evaluateAndUnlockBadges(newXp: Int? = null) {
        val s = uiState.value
        val workoutsCount = s.workouts.size + 1
        if (workoutsCount >= 1) repo.unlockBadge("first_workout")
        if (workoutsCount >= 10) repo.unlockBadge("sessions_10")
        if (s.currentStreak >= 7 || s.bestStreak >= 7) repo.unlockBadge("streak_7")
        if (s.currentStreak >= 30 || s.bestStreak >= 30) repo.unlockBadge("streak_30")
        val xpTotal = newXp ?: s.profile.xp
        if (xpTotal >= 1000) repo.unlockBadge("xp_1000")

        val waterGoal = s.profile.waterGoalLiters
        val daysMeetingWater = s.allWater.groupBy { it.dateIso }
            .count { (_, logs) -> logs.sumOf { it.amountLiters } >= waterGoal }
        if (daysMeetingWater >= 7) repo.unlockBadge("water_7")
    }

    private fun computeStreaks(checkins: List<DayCheckinEntity>, todayIso: String): Triple<Int, Int, Int> {
        if (checkins.isEmpty()) return Triple(0, 0, 0)
        val dates = checkins.map { it.dateIso }.toSortedSet()

        // Current streak walking backward from today (or yesterday if today pending)
        var cur = 0
        var cursor = if (dates.contains(todayIso)) todayIso else DateUtilsCompat.addDays(todayIso, -1)
        while (dates.contains(cursor)) {
            cur++
            cursor = DateUtilsCompat.addDays(cursor, -1)
        }

        // Best streak
        var best = 0
        var run = 0
        var prev: String? = null
        for (d in dates) {
            if (prev != null && DateUtilsCompat.addDays(prev, 1) == d) {
                run++
            } else {
                run = 1
            }
            if (run > best) best = run
            prev = d
        }

        // Rate over last 30 days or since first checkin
        val firstDate = dates.first()
        var daysSinceFirst = 1
        var c = firstDate
        while (c < todayIso && daysSinceFirst < 30) {
            c = DateUtilsCompat.addDays(c, 1)
            daysSinceFirst++
        }
        val windowStart = DateUtilsCompat.addDays(todayIso, -(daysSinceFirst - 1))
        val doneInWindow = dates.count { it >= windowStart && it <= todayIso }
        val rate = ((doneInWindow.toDouble() / daysSinceFirst) * 100.0).roundToInt().coerceIn(0, 100)

        return Triple(cur, best, rate)
    }

    private fun checkRapidWeightLoss(weights: List<WeightLogEntity>): Boolean {
        if (weights.size < 4) return false
        val latest = weights.last()
        val threeWeeksAgoIso = DateUtilsCompat.addDays(latest.dateIso, -21)
        val baseline = weights.firstOrNull { it.dateIso >= threeWeeksAgoIso } ?: weights.first()
        if (baseline.dateIso == latest.dateIso) return false
        val lossRatio = (baseline.weightKg - latest.weightKg) / baseline.weightKg
        return lossRatio > 0.03 // > 1% per week over 3 weeks
    }
}

private data class FiveTuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
)
