package com.example

import com.example.data.catalog.FoodCatalog
import com.example.data.catalog.NutritionCalculator
import com.example.data.catalog.PunchlinesCatalog
import com.example.data.catalog.RankedCalculator
import com.example.data.model.*
import org.junit.Assert.*
import org.junit.Test

class CausaSuiCalculatorsTest {

    @Test
    fun nutritionMatchesSpecVerifiedExample() {
        // Section 7 example: Homme 75 kg, 175 cm, 22 ans, 4 séances -> BMR 1739, TDEE 2695
        // Perte (-20%): 2156 kcal, 150 g P, 72 g L, 227 g G
        val loss = NutritionCalculator.calculateTargets(
            sex = Sex.HOMME,
            age = 22,
            heightCm = 175,
            weightKg = 75.0,
            morphotype = Morphotype.MESOMORPHE,
            goal = FitnessGoal.PERTE_POIDS,
            daysPerWeek = 4,
            physicalJob = false,
        )
        assertEquals(1739, loss.bmr)
        assertEquals(2695, loss.tdee)
        assertEquals(2156, loss.targetCalories)
        assertEquals(150, loss.proteinG)
        assertEquals(72, loss.fatG)
        assertEquals(227, loss.carbsG)
    }

    @Test
    fun under18NeverReceivesCaloricDeficit() {
        val teen = NutritionCalculator.calculateTargets(
            sex = Sex.HOMME,
            age = 17,
            heightCm = 172,
            weightKg = 68.0,
            morphotype = Morphotype.MESOMORPHE,
            goal = FitnessGoal.PERTE_POIDS,
            daysPerWeek = 4,
            physicalJob = false,
        )
        assertEquals(0, teen.goalDeltaPercent)
        assertEquals(teen.tdee, teen.targetCalories)
    }

    @Test
    fun rapidLossAndLowBmiAreRejected() {
        val tooFast = NutritionCalculator.validateSafeWeightTarget(
            currentWeightKg = 80.0,
            targetWeightKg = 70.0,
            heightCm = 175,
            weeksToGoal = 4,
            goal = FitnessGoal.PERTE_POIDS,
        )
        assertNotNull(tooFast)

        val tooThin = NutritionCalculator.validateSafeWeightTarget(
            currentWeightKg = 60.0,
            targetWeightKg = 50.0,
            heightCm = 175,
            weeksToGoal = 20,
            goal = FitnessGoal.PERTE_POIDS,
        )
        assertNotNull(tooThin)
    }

    @Test
    fun catalogsMeetMinimumCounts() {
        assertTrue("Food catalog should have >= 200 items, got ${FoodCatalog.foods.size}", FoodCatalog.foods.size >= 200)
        assertTrue("Punchlines should have >= 200 items, got ${PunchlinesCatalog.allPunchlines.size}", PunchlinesCatalog.allPunchlines.size >= 200)
    }

    @Test
    fun rankedEvaluationProducesExpectedScale() {
        val eliteValues = mapOf(
            "pompes" to 70f,
            "tractions" to 24f,
            "dips" to 38f,
            "muscle_ups" to 12f,
            "gainage" to 230f,
            "bench" to 135f,
            "deadlift" to 195f,
            "squat_kg" to 170f,
            "detente_v" to 72f,
            "detente_h" to 275f,
            "run_100" to 11.4f,
            "run_400" to 53f,
            "run_5k" to 19.0f,
            "run_10k" to 40.0f,
        )
        val (pillars, rank) = RankedCalculator.evaluate(eliteValues, Sex.HOMME, 75.0)
        assertEquals(5, pillars.size)
        assertTrue(rank.levelIndex >= RankTier.S.levelIndex)
    }
}
