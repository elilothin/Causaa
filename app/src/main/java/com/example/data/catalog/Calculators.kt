package com.example.data.catalog

import com.example.data.model.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object NutritionCalculator {

    fun calculateTargets(
        sex: Sex,
        age: Int,
        heightCm: Int,
        weightKg: Double,
        morphotype: Morphotype,
        goal: FitnessGoal,
        daysPerWeek: Int,
        physicalJob: Boolean,
        customLossPercent: Int = -20, // -10 to -25
        customGainPercent: Int = 15,  // +5 to +20
    ): NutritionTargets {
        // Mifflin-St Jeor (Section 7)
        val rawBmr = if (sex == Sex.HOMME) {
            10.0 * weightKg + 6.25 * heightCm - 5.0 * age + 5.0
        } else {
            10.0 * weightKg + 6.25 * heightCm - 5.0 * age - 161.0
        }
        val bmr = rawBmr.roundToInt()

        val factor = when {
            daysPerWeek >= 6 && physicalJob -> 1.9
            daysPerWeek >= 6 -> 1.725
            daysPerWeek in 4..5 -> 1.55
            else -> 1.375
        }

        val tdee = (bmr * factor).roundToInt()

        // Guardrail: < 18 years old -> no caloric deficit allowed (Maintien or Prise only)
        val effectiveGoal = if (age < 18 && (goal == FitnessGoal.PERTE_POIDS || goal == FitnessGoal.RECOMPOSITION)) {
            FitnessGoal.MAINTIEN
        } else {
            goal
        }

        val deltaPercent = when (effectiveGoal) {
            FitnessGoal.PERTE_POIDS -> customLossPercent.coerceIn(-25, -10)
            FitnessGoal.MAINTIEN -> 0
            FitnessGoal.PRISE_MASSE -> customGainPercent.coerceIn(5, 20)
            FitnessGoal.RECOMPOSITION -> -5
        }

        val rawCalories = (tdee * (1.0 + deltaPercent / 100.0)).roundToInt()
        val floorKcal = max(bmr, if (sex == Sex.HOMME) 1500 else 1200)
        val clamped = rawCalories < floorKcal
        val targetCalories = max(rawCalories, floorKcal)

        // Proteins (g/kg) + Endomorphe (+0.2 g/kg)
        val baseProteinPerKg = when (effectiveGoal) {
            FitnessGoal.PERTE_POIDS -> 2.0
            FitnessGoal.MAINTIEN -> 1.6
            FitnessGoal.PRISE_MASSE -> 1.8
            FitnessGoal.RECOMPOSITION -> 2.0
        }
        val proteinPerKg = baseProteinPerKg + if (morphotype == Morphotype.ENDOMORPHE) 0.2 else 0.0
        val proteinG = (weightKg * proteinPerKg).roundToInt()

        // Fat (% of calories) + Ectomorphe (-5 pts, floor 20%)
        val baseFatPct = when (effectiveGoal) {
            FitnessGoal.PERTE_POIDS -> 30
            FitnessGoal.MAINTIEN -> 30
            FitnessGoal.PRISE_MASSE -> 25
            FitnessGoal.RECOMPOSITION -> 30
        }
        val fatPct = max(20, baseFatPct - if (morphotype == Morphotype.ECTOMORPHE) 5 else 0)
        val fatG = ((targetCalories * (fatPct / 100.0)) / 9.0).roundToInt()

        // Carbs = remaining calories / 4
        val carbsG = max(0, ((targetCalories - proteinG * 4.0 - fatG * 9.0) / 4.0).roundToInt())

        val sexOffsetStr = if (sex == Sex.HOMME) "+ 5" else "− 161"
        val bmrFormula = "BMR = 10 × ${formatKg(weightKg)} + 6,25 × $heightCm − 5 × $age $sexOffsetStr = $bmr kcal"
        val tdeeFormula = "TDEE = $bmr × $factor ($daysPerWeek séances/sem.${if (physicalJob && daysPerWeek >= 6) ", métier physique" else ""}) = $tdee kcal"
        val caloriesFormula = buildString {
            append("Objectif ${effectiveGoal.label} (${if (deltaPercent >= 0) "+$deltaPercent" else "$deltaPercent"} %) : $tdee × ${1.0 + deltaPercent / 100.0} = $rawCalories kcal")
            if (clamped) append(" → relevé au plancher de sécurité ($floorKcal kcal).")
        }
        val proteinFormula = "Protéines = ${formatKg(weightKg)} kg × $proteinPerKg g/kg${if (morphotype == Morphotype.ENDOMORPHE) " (+0,2 endomorphe)" else ""} = $proteinG g"
        val fatFormula = "Lipides = $fatPct % de $targetCalories kcal ÷ 9${if (morphotype == Morphotype.ECTOMORPHE) " (−5 pts ectomorphe)" else ""} = $fatG g"
        val carbsFormula = "Glucides = ($targetCalories − $proteinG×4 − $fatG×9) ÷ 4 = $carbsG g"

        return NutritionTargets(
            bmr = bmr,
            tdee = tdee,
            targetCalories = targetCalories,
            proteinG = proteinG,
            fatG = fatG,
            carbsG = carbsG,
            activityFactor = factor,
            goalDeltaPercent = deltaPercent,
            bmrFormulaText = bmrFormula,
            tdeeFormulaText = tdeeFormula,
            caloriesFormulaText = caloriesFormula,
            proteinFormulaText = proteinFormula,
            fatFormulaText = fatFormula,
            carbsFormulaText = carbsFormula,
            clampedByGuardrail = clamped,
        )
    }

    fun calculateBmi(weightKg: Double, heightCm: Int): Double {
        val hM = heightCm / 100.0
        return if (hM > 0) weightKg / (hM * hM) else 0.0
    }

    fun validateSafeWeightTarget(
        currentWeightKg: Double,
        targetWeightKg: Double,
        heightCm: Int,
        weeksToGoal: Int,
        goal: FitnessGoal,
    ): String? {
        if (goal == FitnessGoal.MAINTIEN || goal == FitnessGoal.RECOMPOSITION) return null
        val targetBmi = calculateBmi(targetWeightKg, heightCm)
        if (targetBmi < 18.5) {
            val minSafeKg = (18.5 * (heightCm / 100.0) * (heightCm / 100.0))
            return "Ce poids cible donne un indice corporel inférieur au seuil de précaution (${"%.1f".format(minSafeKg)} kg min.). Consulte un professionnel de santé."
        }
        if (goal == FitnessGoal.PERTE_POIDS && targetWeightKg < currentWeightKg) {
            val totalLoss = currentWeightKg - targetWeightKg
            val safeWeeks = max(1, kotlin.math.ceil(totalLoss / (currentWeightKg * 0.01)).toInt())
            if (weeksToGoal < safeWeeks) {
                return "Une perte supérieure à 1 % du poids par semaine est trop rapide. Délai sûr conseillé : au moins $safeWeeks semaines."
            }
        }
        return null
    }

    fun estimateWorkoutCalories(
        weightKg: Double,
        completedExercises: List<ExerciseDef>,
        difficulty: DifficultyLevel,
    ): Int {
        if (completedExercises.isEmpty()) return 0
        var totalKcal = 0.0
        for (ex in completedExercises) {
            val adjustedSets = max(1, (ex.baseSets * difficulty.multiplier).roundToInt())
            // Estimate ~2.5 min per set (effort + transition without timer)
            val durationHours = (adjustedSets * 2.5) / 60.0
            totalKcal += ex.met * weightKg * durationHours
        }
        return totalKcal.roundToInt()
    }

    private fun formatKg(w: Double): String =
        if (w % 1.0 == 0.0) w.toInt().toString() else "%.1f".format(w)
}

object RankedCalculator {

    val tests: List<RankedTestDef> = listOf(
        // Pilier 1 : Maîtrise du corps
        RankedTestDef("pompes", RankedPillar.MAITRISE, "Pompes strictes", "répétitions", 0f, 100f, 1f, false, "Nombre maximal en une série, poitrine proche du sol."),
        RankedTestDef("tractions", RankedPillar.MAITRISE, "Tractions strictes", "répétitions", 0f, 40f, 1f, false, "Menton au-dessus de la barre, sans élan."),
        RankedTestDef("dips", RankedPillar.MAITRISE, "Dips parallèles", "répétitions", 0f, 55f, 1f, false, "Descente complète épaules sous les coudes."),
        RankedTestDef("muscle_ups", RankedPillar.MAITRISE, "Muscle-ups", "répétitions", 0f, 20f, 1f, false, "Passage strict barre ou anneaux (0 = non testé)."),
        RankedTestDef("gainage", RankedPillar.MAITRISE, "Gainage planche", "secondes", 0f, 360f, 5f, false, "Tenue propre sur avant-bras, dos aligné."),
        // Pilier 2 : Force brute (charge max en kg, normalisée par le poids du corps)
        RankedTestDef("bench", RankedPillar.FORCE, "Développé couché (1RM)", "kg", 0f, 220f, 2.5f, false, "Charge maximale estimée ou réalisée sur 1 répétition."),
        RankedTestDef("deadlift", RankedPillar.FORCE, "Soulevé de terre (1RM)", "kg", 0f, 300f, 2.5f, false, "Charge maximale au sol, dos neutre."),
        RankedTestDef("squat_kg", RankedPillar.FORCE, "Squat barre (1RM)", "kg", 0f, 260f, 2.5f, false, "Squat complet sous la parallèle."),
        // Pilier 3 : Explosivité
        RankedTestDef("detente_v", RankedPillar.EXPLOSIVITE, "Détente verticale", "cm", 0f, 100f, 1f, false, "Hauteur de saut sans élan (différence main levée / touche)."),
        RankedTestDef("detente_h", RankedPillar.EXPLOSIVITE, "Détente horizontale", "cm", 0f, 330f, 5f, false, "Saut en longueur pieds joints sans élan."),
        // Pilier 4 : Vitesse pure (plus bas = meilleur)
        RankedTestDef("run_100", RankedPillar.VITESSE, "Course de 100 m", "secondes", 0f, 25f, 0.2f, true, "Chrono sur 100 m départ arrêté (0 = non testé)."),
        RankedTestDef("run_400", RankedPillar.VITESSE, "Course de 400 m", "secondes", 0f, 140f, 1f, true, "Tour de piste complet à allure maximale (0 = non testé)."),
        // Pilier 5 : Endurance (minutes, plus bas = meilleur)
        RankedTestDef("run_5k", RankedPillar.ENDURANCE, "Course de 5 km", "minutes", 0f, 50f, 0.5f, true, "Temps total sur 5 km (0 = non testé)."),
        RankedTestDef("run_10k", RankedPillar.ENDURANCE, "Course de 10 km", "minutes", 0f, 100f, 1f, true, "Temps total sur 10 km (0 = non testé)."),
    )

    fun scoreTest(testId: String, value: Float, sex: Sex, weightKg: Double): Double {
        if (value <= 0f) return 0.0
        val v = value.toDouble()
        val bw = max(35.0, weightKg)
        val isFemale = sex == Sex.FEMME

        // Linear piecewise interpolation against realistic benchmarks (0 -> 100 scale matching F..Z+)
        return when (testId) {
            "pompes" -> {
                val maxBench = if (isFemale) 55.0 else 80.0
                scaleUp(v, 2.0, maxBench)
            }
            "tractions" -> {
                val maxBench = if (isFemale) 18.0 else 28.0
                scaleUp(v, 1.0, maxBench)
            }
            "dips" -> {
                val maxBench = if (isFemale) 28.0 else 42.0
                scaleUp(v, 1.0, maxBench)
            }
            "muscle_ups" -> {
                val maxBench = if (isFemale) 8.0 else 14.0
                scaleUp(v, 1.0, maxBench, minScore = 45.0)
            }
            "gainage" -> {
                val maxBench = if (isFemale) 220.0 else 250.0
                scaleUp(v, 15.0, maxBench)
            }
            "bench" -> {
                val ratio = v / bw
                val maxRatio = if (isFemale) 1.35 else 1.85
                scaleUp(ratio, 0.35, maxRatio)
            }
            "deadlift" -> {
                val ratio = v / bw
                val maxRatio = if (isFemale) 2.0 else 2.65
                scaleUp(ratio, 0.60, maxRatio)
            }
            "squat_kg" -> {
                val ratio = v / bw
                val maxRatio = if (isFemale) 1.75 else 2.30
                scaleUp(ratio, 0.50, maxRatio)
            }
            "detente_v" -> {
                val maxCm = if (isFemale) 62.0 else 78.0
                scaleUp(v, 20.0, maxCm)
            }
            "detente_h" -> {
                val maxCm = if (isFemale) 245.0 else 290.0
                scaleUp(v, 120.0, maxCm)
            }
            "run_100" -> {
                val bestS = if (isFemale) 12.2 else 10.9
                val worstS = if (isFemale) 21.0 else 19.0
                scaleDown(v, bestS, worstS)
            }
            "run_400" -> {
                val bestS = if (isFemale) 58.0 else 50.0
                val worstS = if (isFemale) 115.0 else 100.0
                scaleDown(v, bestS, worstS)
            }
            "run_5k" -> {
                val bestM = if (isFemale) 20.0 else 17.5
                val worstM = if (isFemale) 45.0 else 42.0
                scaleDown(v, bestM, worstM)
            }
            "run_10k" -> {
                val bestM = if (isFemale) 43.0 else 37.0
                val worstM = if (isFemale) 90.0 else 84.0
                scaleDown(v, bestM, worstM)
            }
            else -> 0.0
        }
    }

    fun evaluate(
        values: Map<String, Float>,
        sex: Sex,
        weightKg: Double,
    ): Pair<List<PillarScore>, RankTier> {
        val pillarScores = RankedPillar.entries.map { pillar ->
            val pillarTests = tests.filter { it.pillar == pillar }
            val countedScores = pillarTests.mapNotNull { def ->
                val raw = values[def.id] ?: 0f
                if (raw > 0f) scoreTest(def.id, raw, sex, weightKg) else null
            }
            val avg = if (countedScores.isEmpty()) 0.0 else countedScores.average()
            PillarScore(pillar = pillar, score = avg, countedTests = countedScores.size)
        }

        // Equal weight across the 5 pillars (Section 8)
        val globalAvg = pillarScores.map { it.score }.average()
        val rank = RankTier.fromScore(globalAvg)
        return pillarScores to rank
    }

    private fun scaleUp(value: Double, minVal: Double, maxVal: Double, minScore: Double = 8.0): Double {
        if (value <= 0.0) return 0.0
        val norm = ((value - minVal) / (maxVal - minVal)).coerceIn(0.0, 1.0)
        return minScore + norm * (100.0 - minScore)
    }

    private fun scaleDown(value: Double, bestVal: Double, worstVal: Double): Double {
        if (value <= 0.0) return 0.0
        val norm = ((worstVal - value) / (worstVal - bestVal)).coerceIn(0.0, 1.0)
        return 8.0 + norm * 92.0
    }
}
