package com.example.data.catalog

import com.example.data.model.*
import kotlin.math.max
import kotlin.math.roundToInt

object ExerciseCatalog {

    val exercises: Map<String, ExerciseDef> = listOf(
        ExerciseDef(
            id = "bench_press",
            gymName = "Développé couché",
            homeVariants = listOf("Pompes surélevées", "Pompes classiques", "Pompes déclinées"),
            muscleGroup = "Pectoraux",
            baseSets = 4,
            baseReps = "8 à 12",
            restSeconds = 90,
            met = 5.0,
            animGym = AnimationKind.BENCH_PRESS,
            animHome = AnimationKind.PUSHUP,
            tipNoEquipment = "Utilise une chaise stable pour surélever les pieds si les pompes classiques deviennent trop faciles.",
            tipDumbbellHome = "Avec des haltères à la maison : allonge-toi au sol (floor press), contrôle la descente 3 s et augmente les séries.",
        ),
        ExerciseDef(
            id = "military_press",
            gymName = "Développé militaire",
            homeVariants = listOf("Pompes piquées", "Pompes piquées pieds surélevés", "Pompes contre un mur"),
            muscleGroup = "Épaules",
            baseSets = 4,
            baseReps = "10",
            restSeconds = 90,
            met = 5.0,
            animGym = AnimationKind.SHOULDER_PRESS,
            animHome = AnimationKind.PUSHUP,
            tipNoEquipment = "Tu n'as pas de barre ? Passe aux pompes piquées, hanches hautes, ou utilise des haltères.",
            tipDumbbellHome = "Avec des haltères à la maison : pousse debout ou assis dos droit, gainage serré, sans cambrer.",
        ),
        ExerciseDef(
            id = "lateral_raise",
            gymName = "Élévations latérales",
            homeVariants = listOf("Élévations bouteilles d'eau", "Élévations sac à dos lesté", "Élévations tempo lent"),
            muscleGroup = "Épaules",
            baseSets = 3,
            baseReps = "12 à 15",
            restSeconds = 60,
            met = 3.5,
            animGym = AnimationKind.LATERAL_RAISE,
            animHome = AnimationKind.LATERAL_RAISE,
            tipNoEquipment = "Pas d'haltères ? Prends deux bouteilles d'eau de 1,5 L ou un élastique sous les pieds.",
            tipDumbbellHome = "Avec des haltères légers : bloque 1 s en haut et descends en 3 secondes pour garder la tension.",
        ),
        ExerciseDef(
            id = "bicep_curl",
            gymName = "Curl haltères",
            homeVariants = listOf("Curl élastique ou bouteilles", "Curl sac à dos chargé", "Tractions supination"),
            muscleGroup = "Biceps",
            baseSets = 3,
            baseReps = "10 à 12",
            restSeconds = 60,
            met = 4.0,
            animGym = AnimationKind.BICEP_CURL,
            animHome = AnimationKind.BICEP_CURL,
            tipNoEquipment = "Un sac à dos rempli de livres fait une charge réglable efficace pour le curl.",
            tipDumbbellHome = "Avec des haltères : coudes collés au buste, supination complète en haut sans balancer le dos.",
        ),
        ExerciseDef(
            id = "tricep_ext",
            gymName = "Extension triceps",
            homeVariants = listOf("Dips sur chaise", "Pompes diamant", "Extension derrière la nuque lestée"),
            muscleGroup = "Triceps",
            baseSets = 3,
            baseReps = "12",
            restSeconds = 60,
            met = 4.0,
            animGym = AnimationKind.TRICEP_EXTENSION,
            animHome = AnimationKind.DIP,
            tipNoEquipment = "Mains serrées en triangle au sol (pompes diamant) ou dips entre deux chaises solides.",
            tipDumbbellHome = "Avec un seul haltère tenu à deux mains derrière la tête : descends lentement derrière la nuque.",
        ),
        ExerciseDef(
            id = "squat",
            gymName = "Squat à la barre",
            homeVariants = listOf("Squat poids du corps", "Squat bulgare", "Pistol squat assisté"),
            muscleGroup = "Quadriceps",
            baseSets = 4,
            baseReps = "8 à 12",
            restSeconds = 120,
            met = 6.0,
            animGym = AnimationKind.SQUAT,
            animHome = AnimationKind.SQUAT,
            tipNoEquipment = "Quand le squat à deux jambes devient facile, passe au squat bulgare pied arrière sur chaise.",
            tipDumbbellHome = "Avec des haltères : tiens un haltère contre la poitrine (goblet squat) ou deux sur les côtés en fente bulgare.",
        ),
        ExerciseDef(
            id = "leg_press",
            gymName = "Presse à cuisses",
            homeVariants = listOf("Fentes alternées", "Squat bulgare tempo", "Fentes sautées"),
            muscleGroup = "Jambes",
            baseSets = 4,
            baseReps = "10 à 12",
            restSeconds = 90,
            met = 5.5,
            animGym = AnimationKind.SQUAT,
            animHome = AnimationKind.LUNGE,
            tipNoEquipment = "Les fentes profondes ou le squat bulgare chargent chaque jambe séparément à intensité élevée.",
            tipDumbbellHome = "Avec des haltères : tiens une charge dans chaque main durant tes fentes pour augmenter le travail.",
        ),
        ExerciseDef(
            id = "deadlift",
            gymName = "Soulevé de terre",
            homeVariants = listOf("Pont fessier au sol", "Hip thrust sac à dos", "Soulevé de terre unilatéral lesté"),
            muscleGroup = "Chaîne postérieure",
            baseSets = 4,
            baseReps = "8",
            restSeconds = 120,
            met = 6.0,
            animGym = AnimationKind.DEADLIFT,
            animHome = AnimationKind.HIP_THRUST,
            tipNoEquipment = "Charge un sac à dos sur tes hanches en hip thrust épaules sur le bord du lit ou canapé.",
            tipDumbbellHome = "Avec des haltères : réalise du soulevé de terre jambes semi-tendues en descendant jusqu'aux tibias.",
            approximateHomeEquivalence = true,
        ),
        ExerciseDef(
            id = "lat_pulldown",
            gymName = "Tirage vertical",
            homeVariants = listOf("Rowing inversé sous table", "Tractions assistées", "Tractions strictes"),
            muscleGroup = "Dos",
            baseSets = 4,
            baseReps = "8 à 12",
            restSeconds = 90,
            met = 5.0,
            animGym = AnimationKind.PULLUP,
            animHome = AnimationKind.PULLUP,
            tipNoEquipment = "Équivalence approximative sans barre de traction : glisse-toi sous une table solide et tire la poitrine.",
            tipDumbbellHome = "Avec des haltères : complète par du rowing buste penché un bras à la fois en tirant vers la hanche.",
            approximateHomeEquivalence = true,
        ),
        ExerciseDef(
            id = "barbell_row",
            gymName = "Rowing à la barre",
            homeVariants = listOf("Rowing inversé sous table solide", "Rowing serviette / sac lesté", "Rowing inversé pieds surélevés"),
            muscleGroup = "Dos",
            baseSets = 4,
            baseReps = "10",
            restSeconds = 90,
            met = 5.0,
            animGym = AnimationKind.ROWING,
            animHome = AnimationKind.ROWING,
            tipNoEquipment = "Teste d'abord la solidité de la table. Sinon, tire un sac à dos lourd buste penché à 45°.",
            tipDumbbellHome = "Avec des haltères : appuie une main sur une chaise et tire l'haltère vers la hanche.",
        ),
        ExerciseDef(
            id = "leg_curl",
            gymName = "Leg curl",
            homeVariants = listOf("Nordic curl assisté", "Glissement talons au sol", "Pont fessier une jambe"),
            muscleGroup = "Ischio-jambiers",
            baseSets = 3,
            baseReps = "10 à 12",
            restSeconds = 75,
            met = 4.5,
            animGym = AnimationKind.HIP_THRUST,
            animHome = AnimationKind.HIP_THRUST,
            tipNoEquipment = "Équivalence approximative : bloque tes pieds sous un meuble lourd et freine la descente au maximum.",
            tipDumbbellHome = "Avec un haltère : coince un haltère léger entre les pieds allongé sur le ventre, ou fais du hip thrust unilatéral.",
            approximateHomeEquivalence = true,
        ),
        ExerciseDef(
            id = "calf_raise",
            gymName = "Mollets debout",
            homeVariants = listOf("Mollets sur une marche", "Mollets unilatéraux sur marche", "Mollets explosifs"),
            muscleGroup = "Mollets",
            baseSets = 4,
            baseReps = "15",
            restSeconds = 45,
            met = 3.5,
            animGym = AnimationKind.CALF_RAISE,
            animHome = AnimationKind.CALF_RAISE,
            tipNoEquipment = "Pose l'avant-pied sur une marche d'escalier ou un gros livre, descends le talon à fond et monte 1 s en haut.",
            tipDumbbellHome = "Avec un haltère : tiens-le du côté de la jambe qui travaille en appui unilatéral.",
        ),
        ExerciseDef(
            id = "crunch_machine",
            gymName = "Crunch machine",
            homeVariants = listOf("Crunch au sol contrôlé", "Planche gainage", "Planche dynamique"),
            muscleGroup = "Abdominaux",
            baseSets = 3,
            baseReps = "15",
            restSeconds = 45,
            met = 3.5,
            animGym = AnimationKind.CRUNCH,
            animHome = AnimationKind.PLANK,
            tipNoEquipment = "Contracte fort les abdominaux en expirant à chaque montée, sans tirer sur la nuque.",
            tipDumbbellHome = "Avec un haltère léger : tiens-le contre la poitrine durant les crunchs.",
        ),
        ExerciseDef(
            id = "dips_chest",
            gymName = "Dips parallèles",
            homeVariants = listOf("Pompes serrées", "Dips entre deux chaises", "Pompes déclinées serrées"),
            muscleGroup = "Pectoraux & Triceps",
            baseSets = 3,
            baseReps = "10",
            restSeconds = 90,
            met = 5.0,
            animGym = AnimationKind.DIP,
            animHome = AnimationKind.DIP,
            tipNoEquipment = "Place deux chaises stables dos à dos, vérifie l'équilibre avant de charger.",
            tipDumbbellHome = "Avec un sac à dos : ajoute du lest si tu dépasses facilement 15 répétitions propres.",
        ),
        ExerciseDef(
            id = "jumping_jacks",
            gymName = "Échauffement / Cardio actif",
            homeVariants = listOf("Jumping jacks", "Jumping jacks rapides", "Burpees contrôlés"),
            muscleGroup = "Conditionnement",
            baseSets = 3,
            baseReps = "30",
            restSeconds = 45,
            met = 6.0,
            animGym = AnimationKind.JUMPING_JACK,
            animHome = AnimationKind.JUMPING_JACK,
            tipNoEquipment = "Amortis sur l'avant du pied, garde les épaules détendues et respire régulièrement.",
            tipDumbbellHome = "Reste sans charge sur ce mouvement pour garder la fluidité articulaire.",
        ),
    ).associateBy { it.id }

    fun getWeeklyPlan(daysPerWeek: Int, usePplForThreeDays: Boolean = false): List<DayPlanItem> {
        val shortDays = listOf("LUN", "MAR", "MER", "JEU", "VEN", "SAM", "DIM")
        val fullBodyA = listOf("squat", "bench_press", "barbell_row", "military_press", "crunch_machine")
        val fullBodyB = listOf("deadlift", "dips_chest", "lat_pulldown", "lateral_raise", "calf_raise")
        val upperA = listOf("bench_press", "barbell_row", "military_press", "bicep_curl", "tricep_ext", "crunch_machine")
        val lowerA = listOf("squat", "leg_curl", "leg_press", "calf_raise", "jumping_jacks")
        val upperB = listOf("dips_chest", "lat_pulldown", "military_press", "lateral_raise", "bicep_curl", "tricep_ext", "crunch_machine")
        val lowerB = listOf("deadlift", "leg_press", "leg_curl", "calf_raise", "jumping_jacks")
        val push = listOf("bench_press", "military_press", "dips_chest", "lateral_raise", "tricep_ext")
        val pull = listOf("lat_pulldown", "barbell_row", "deadlift", "bicep_curl", "crunch_machine")
        val legs = listOf("squat", "leg_press", "leg_curl", "calf_raise", "jumping_jacks")

        val templateByDay: Map<Int, Pair<String, List<String>>> = when (daysPerWeek.coerceIn(2, 6)) {
            2 -> mapOf(
                0 to ("CORPS ENTIER A" to fullBodyA),
                3 to ("CORPS ENTIER B" to fullBodyB),
            )
            3 -> if (usePplForThreeDays) {
                mapOf(
                    0 to ("PUSH" to push),
                    2 to ("PULL" to pull),
                    4 to ("LEGS" to legs),
                )
            } else {
                mapOf(
                    0 to ("CORPS ENTIER A" to fullBodyA),
                    2 to ("CORPS ENTIER B" to fullBodyB),
                    4 to ("CORPS ENTIER C" to fullBodyA),
                )
            }
            4 -> mapOf(
                0 to ("UPPER A" to upperA),
                2 to ("LOWER A" to lowerA),
                4 to ("UPPER B" to upperB),
                5 to ("LOWER B" to lowerB),
            )
            5 -> mapOf(
                0 to ("PUSH" to push),
                1 to ("PULL" to pull),
                2 to ("LEGS" to legs),
                4 to ("UPPER" to upperB),
                5 to ("LOWER" to lowerB),
            )
            else -> mapOf(
                0 to ("PUSH A" to push),
                1 to ("PULL A" to pull),
                2 to ("LEGS A" to legs),
                3 to ("PUSH B" to push),
                4 to ("PULL B" to pull),
                5 to ("LEGS B" to legs),
            )
        }

        return (0..6).map { idx ->
            val session = templateByDay[idx]
            if (session == null) {
                DayPlanItem(
                    dayOfWeekIndex = idx,
                    shortDay = shortDays[idx],
                    sessionName = "REPOS",
                    isRestDay = true,
                    exerciseIds = emptyList(),
                )
            } else {
                DayPlanItem(
                    dayOfWeekIndex = idx,
                    shortDay = shortDays[idx],
                    sessionName = session.first,
                    isRestDay = false,
                    exerciseIds = session.second,
                )
            }
        }
    }

    fun adjustedSets(baseSets: Int, difficulty: DifficultyLevel): Int =
        max(1, (baseSets * difficulty.multiplier).roundToInt())

    fun displayExerciseName(
        def: ExerciseDef,
        location: WorkoutLocation,
        homeTierIndex: Int,
    ): String {
        if (location == WorkoutLocation.SALLE) return def.gymName
        val idx = homeTierIndex.coerceIn(0, def.homeVariants.lastIndex)
        return def.homeVariants[idx]
    }
}
