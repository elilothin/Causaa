package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class AppThemeMode { BEIGE, DARK }

enum class Sex(val label: String) {
    HOMME("Homme"),
    FEMME("Femme"),
}

enum class Morphotype(val label: String, val description: String) {
    ECTOMORPHE("Ectomorphe", "Ossature fine, métabolisme rapide, silhouette élancée"),
    MESOMORPHE("Mésomorphe", "Épaules larges, prise musculaire facilitée, ossature moyenne"),
    ENDOMORPHE("Endomorphe", "Ossature large, gabarit plus massif, stockage facilité"),
}

enum class FitnessGoal(val label: String, val shortLabel: String) {
    PERTE_POIDS("Perte de poids", "Perte"),
    MAINTIEN("Maintien", "Maintien"),
    PRISE_MASSE("Prise de masse", "Prise"),
    RECOMPOSITION("Recomposition", "Recomp."),
}

enum class DifficultyLevel(val label: String, val multiplier: Double) {
    FACILE("Facile", 0.7),
    MOYEN("Moyen", 1.0),
    DIFFICILE("Difficile", 1.3),
}

enum class WorkoutLocation(val label: String) {
    SALLE("Salle"),
    MAISON("Maison"),
}

enum class RankTier(
    val code: String,
    val levelIndex: Int,
    val muscleStage: Int,
    val hasGoldAura: Boolean,
    val color: Color,
    val title: String,
) {
    F("F", 0, 1, false, RankColorF, "Départ"),
    E("E", 1, 1, false, RankColorE, "Initié"),
    D("D", 2, 2, false, RankColorD, "Apprenti"),
    C("C", 3, 3, false, RankColorC, "Régulier"),
    B("B", 4, 4, false, RankColorB, "Solide"),
    A("A", 5, 5, false, RankColorA, "Athlète"),
    S_MINUS("S-", 6, 5, false, RankColorSMinus, "Élite"),
    S("S", 7, 5, true, RankColorS, "Supérieur"),
    Z("Z", 8, 5, true, RankColorZ, "Souverain"),
    Z_PLUS("Z+", 9, 5, true, RankColorZPlus, "Causa Sui");

    companion object {
        fun fromScore(score: Double): RankTier = when {
            score >= 95.0 -> Z_PLUS
            score >= 88.0 -> Z
            score >= 80.0 -> S
            score >= 72.0 -> S_MINUS
            score >= 62.0 -> A
            score >= 50.0 -> B
            score >= 38.0 -> C
            score >= 26.0 -> D
            score >= 14.0 -> E
            else -> F
        }

        fun fromCode(code: String): RankTier =
            entries.firstOrNull { it.code == code } ?: F
    }
}

enum class RankedPillar(val label: String) {
    MAITRISE("Maîtrise du corps"),
    FORCE("Force brute"),
    EXPLOSIVITE("Explosivité"),
    VITESSE("Vitesse pure"),
    ENDURANCE("Endurance"),
}

enum class AnimationKind {
    IDLE_BREATHE,
    SHOULDER_PRESS,
    LATERAL_RAISE,
    BICEP_CURL,
    TRICEP_EXTENSION,
    SQUAT,
    JUMPING_JACK,
    CALF_RAISE,
    PUSHUP,
    PULLUP,
    DIP,
    PLANK,
    ROWING,
    LUNGE,
    BENCH_PRESS,
    DEADLIFT,
    HIP_THRUST,
    CRUNCH,
}

enum class PunchlineCategory(val label: String) {
    DEBUT("Début"),
    VALIDATION("Validation"),
    RANG("Rang"),
    SERIE("Série"),
    REPOS("Repos"),
    DESTIN("Destin"),
}

enum class DayStatus {
    WORKOUT_DONE,
    REST_VALIDATED,
    MISSED,
    TODAY_PENDING,
    UPCOMING,
}

enum class MeasurementPart(val label: String, val unit: String = "cm") {
    BRAS("Bras"),
    POITRINE("Poitrine"),
    TAILLE("Taille"),
    CUISSES("Cuisses"),
    HANCHES("Hanches"),
}

data class ExerciseDef(
    val id: String,
    val gymName: String,
    val homeVariants: List<String>, // Ordered from easiest to hardest
    val muscleGroup: String,
    val baseSets: Int,
    val baseReps: String,
    val restSeconds: Int,
    val met: Double,
    val animGym: AnimationKind,
    val animHome: AnimationKind,
    val tipNoEquipment: String,
    val tipDumbbellHome: String,
    val approximateHomeEquivalence: Boolean = false,
)

data class DayPlanItem(
    val dayOfWeekIndex: Int, // 0 = LUN .. 6 = DIM
    val shortDay: String,
    val sessionName: String, // e.g. "UPPER A", "REPOS"
    val isRestDay: Boolean,
    val exerciseIds: List<String>,
)

data class FoodItem(
    val id: String,
    val name: String,
    val category: String,
    val portionLabel: String,
    val calories: Int,
    val proteinG: Double,
    val fatG: Double,
    val carbsG: Double,
)

data class NutritionTargets(
    val bmr: Int,
    val tdee: Int,
    val targetCalories: Int,
    val proteinG: Int,
    val fatG: Int,
    val carbsG: Int,
    val activityFactor: Double,
    val goalDeltaPercent: Int,
    val bmrFormulaText: String,
    val tdeeFormulaText: String,
    val caloriesFormulaText: String,
    val proteinFormulaText: String,
    val fatFormulaText: String,
    val carbsFormulaText: String,
    val clampedByGuardrail: Boolean,
)

data class RankedTestDef(
    val id: String,
    val pillar: RankedPillar,
    val name: String,
    val unit: String,
    val minVal: Float,
    val maxVal: Float,
    val step: Float,
    val lowerIsBetter: Boolean = false,
    val helper: String,
)

data class PillarScore(
    val pillar: RankedPillar,
    val score: Double, // 0..100
    val countedTests: Int,
)

data class AvatarConfig(
    val skinIndex: Int = 0,
    val faceShapeIndex: Int = 0, // 0 oval, 1 square, 2 angular
    val hairStyleIndex: Int = 1, // 0 rasé, 1 court, 2 afro/bouclé, 3 tresses/long, 4 chignon, 5 fade
    val hairColorIndex: Int = 0,
    val eyeStyleIndex: Int = 0, // 0 déterminé, 1 calme, 2 vif
    val browStyleIndex: Int = 0, // 0 droit, 1 marqué, 2 arqué
    val mouthStyleIndex: Int = 0, // 0 neutre, 1 sourire léger, 2 concentré
    val beardStyleIndex: Int = 0, // 0 aucune, 1 bouc, 2 barbe courte
    val glassesIndex: Int = 0, // 0 aucune, 1 rectangulaire, 2 ronde
    val earringIndex: Int = 0, // 0 aucune, 1 anneau gauche, 2 deux puces
)

data class BadgeDef(
    val id: String,
    val title: String,
    val description: String,
    val iconType: BadgeIconType,
)

enum class BadgeIconType {
    WEIGHT,
    FLAME,
    WATER,
    TROPHY,
}

data class CharacterVisualParams(
    val sex: Sex,
    val age: Int,
    val heightCm: Int,
    val weightKg: Double,
    val morphotype: Morphotype,
    val muscleStage: Int,
    val hasGoldAura: Boolean,
    val skinColor: Color,
    val outfitColor: Color,
    val shoeAccentColor: Color,
)
