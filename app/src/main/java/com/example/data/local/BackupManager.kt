package com.example.data.local

import androidx.room.withTransaction
import com.example.data.model.AvatarConfig
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    const val SCHEMA_VERSION = 1

    suspend fun exportJson(db: CausaSuiDatabase): String {
        val root = JSONObject()
        root.put("app", "CAUSA SUI")
        root.put("schemaVersion", SCHEMA_VERSION)
        root.put("exportedAtMs", System.currentTimeMillis())

        val profile = db.profileDao().getProfileOnce()
        if (profile != null) {
            val p = JSONObject().apply {
                put("pseudo", profile.pseudo)
                put("sex", profile.sex)
                put("age", profile.age)
                put("heightCm", profile.heightCm)
                put("weightKg", profile.weightKg)
                put("morphotype", profile.morphotype)
                put("goal", profile.goal)
                put("targetWeightKg", profile.targetWeightKg)
                put("targetDateIso", profile.targetDateIso)
                put("daysPerWeek", profile.daysPerWeek)
                put("physicalJob", profile.physicalJob)
                put("usePplForThreeDays", profile.usePplForThreeDays)
                put("difficulty", profile.difficulty)
                put("preferredLocation", profile.preferredLocation)
                put("themeMode", profile.themeMode)
                put("customCalorieDeltaPct", profile.customCalorieDeltaPct)
                put("waterGoalLiters", profile.waterGoalLiters)
                put("bottleVolumeLiters", profile.bottleVolumeLiters)
                put("xp", profile.xp)
                put("skinIndex", profile.skinIndex)
                put("outfitIndex", profile.outfitIndex)
                put("shoeAccentIndex", profile.shoeAccentIndex)
                put("avatarJson", profile.avatarJson)
                put("homeTiersJson", profile.homeTiersJson)
                put("soundsEnabled", profile.soundsEnabled)
                put("hapticsEnabled", profile.hapticsEnabled)
                put("reduceMotion", profile.reduceMotion)
                put("economy30Fps", profile.economy30Fps)
                put("workoutReminderEnabled", profile.workoutReminderEnabled)
                put("workoutReminderHour", profile.workoutReminderHour)
                put("workoutReminderMinute", profile.workoutReminderMinute)
                put("workoutReminderDaysCsv", profile.workoutReminderDaysCsv)
                put("waterReminderEnabled", profile.waterReminderEnabled)
                put("waterReminderStartHour", profile.waterReminderStartHour)
                put("waterReminderEndHour", profile.waterReminderEndHour)
                put("waterReminderIntervalHours", profile.waterReminderIntervalHours)
                put("backupReminderMonthly", profile.backupReminderMonthly)
                put("onboardingCompleted", profile.onboardingCompleted)
            }
            root.put("profile", p)
        }

        val workouts = JSONArray().apply {
            db.workoutDao().getAllWorkoutsOnce().forEach { w ->
                put(JSONObject().apply {
                    put("dateIso", w.dateIso)
                    put("timestampMs", w.timestampMs)
                    put("sessionName", w.sessionName)
                    put("location", w.location)
                    put("difficulty", w.difficulty)
                    put("completedExerciseIdsCsv", w.completedExerciseIdsCsv)
                    put("skippedExerciseIdsCsv", w.skippedExerciseIdsCsv)
                    put("estimatedCaloriesBurned", w.estimatedCaloriesBurned)
                    put("xpEarned", w.xpEarned)
                })
            }
        }
        root.put("workouts", workouts)

        val foods = JSONArray().apply {
            db.foodDao().getAllFoodsOnce().forEach { f ->
                put(JSONObject().apply {
                    put("dateIso", f.dateIso)
                    put("timestampMs", f.timestampMs)
                    put("name", f.name)
                    put("calories", f.calories)
                    put("proteinG", f.proteinG)
                    put("fatG", f.fatG)
                    put("carbsG", f.carbsG)
                    put("isDirectEntry", f.isDirectEntry)
                })
            }
        }
        root.put("foods", foods)

        val waters = JSONArray().apply {
            db.waterDao().getAllWaterOnce().forEach { wa ->
                put(JSONObject().apply {
                    put("dateIso", wa.dateIso)
                    put("timestampMs", wa.timestampMs)
                    put("amountLiters", wa.amountLiters)
                })
            }
        }
        root.put("waters", waters)

        val weights = JSONArray().apply {
            db.weightDao().getAllWeightsOnce().forEach { wt ->
                put(JSONObject().apply {
                    put("dateIso", wt.dateIso)
                    put("timestampMs", wt.timestampMs)
                    put("weightKg", wt.weightKg)
                })
            }
        }
        root.put("weights", weights)

        val measurements = JSONArray().apply {
            db.measurementDao().getAllMeasurementsOnce().forEach { m ->
                put(JSONObject().apply {
                    put("dateIso", m.dateIso)
                    put("timestampMs", m.timestampMs)
                    put("part", m.part)
                    put("valueCm", m.valueCm)
                })
            }
        }
        root.put("measurements", measurements)

        val notes = JSONArray().apply {
            db.noteDao().getAllNotesOnce().forEach { n ->
                put(JSONObject().apply {
                    put("dateIso", n.dateIso)
                    put("note", n.note)
                    put("updatedMs", n.updatedMs)
                })
            }
        }
        root.put("notes", notes)

        val ranked = JSONArray().apply {
            db.rankedDao().getAllSnapshotsOnce().forEach { r ->
                put(JSONObject().apply {
                    put("dateIso", r.dateIso)
                    put("timestampMs", r.timestampMs)
                    put("rankCode", r.rankCode)
                    put("globalScore", r.globalScore)
                    put("maitriseScore", r.maitriseScore)
                    put("forceScore", r.forceScore)
                    put("explosiviteScore", r.explosiviteScore)
                    put("vitesseScore", r.vitesseScore)
                    put("enduranceScore", r.enduranceScore)
                    put("rawValuesJson", r.rawValuesJson)
                })
            }
        }
        root.put("ranked", ranked)

        val checkins = JSONArray().apply {
            db.checkinDao().getAllCheckinsOnce().forEach { c ->
                put(JSONObject().apply {
                    put("dateIso", c.dateIso)
                    put("status", c.status)
                    put("timestampMs", c.timestampMs)
                })
            }
        }
        root.put("checkins", checkins)

        val badges = JSONArray().apply {
            db.badgeDao().getAllBadgesOnce().forEach { b ->
                put(JSONObject().apply {
                    put("badgeId", b.badgeId)
                    put("unlockedMs", b.unlockedMs)
                })
            }
        }
        root.put("badges", badges)

        return root.toString(2)
    }

    suspend fun importJson(db: CausaSuiDatabase, jsonString: String): Result<Unit> = runCatching {
        val root = JSONObject(jsonString)
        require(root.optString("app") == "CAUSA SUI") {
            "Ce fichier n'est pas une sauvegarde CAUSA SUI valide."
        }
        val schemaVersion = root.optInt("schemaVersion", 1)
        require(schemaVersion <= SCHEMA_VERSION) {
            "Version de sauvegarde ($schemaVersion) plus récente que l'application."
        }

        val profileObj = root.optJSONObject("profile")
        val profile = profileObj?.let { p ->
            UserProfileEntity(
                id = 1,
                pseudo = p.optString("pseudo", "Athlète"),
                sex = p.optString("sex", "HOMME"),
                age = p.optInt("age", 22).coerceIn(16, 90),
                heightCm = p.optInt("heightCm", 175).coerceIn(130, 220),
                weightKg = p.optDouble("weightKg", 75.0).coerceIn(35.0, 250.0),
                morphotype = p.optString("morphotype", "MESOMORPHE"),
                goal = p.optString("goal", "MAINTIEN"),
                targetWeightKg = p.optDouble("targetWeightKg", 75.0),
                targetDateIso = p.optString("targetDateIso", ""),
                daysPerWeek = p.optInt("daysPerWeek", 4).coerceIn(2, 6),
                physicalJob = p.optBoolean("physicalJob", false),
                usePplForThreeDays = p.optBoolean("usePplForThreeDays", false),
                difficulty = p.optString("difficulty", "MOYEN"),
                preferredLocation = p.optString("preferredLocation", "SALLE"),
                themeMode = p.optString("themeMode", "BEIGE"),
                customCalorieDeltaPct = p.optInt("customCalorieDeltaPct", -20),
                waterGoalLiters = p.optDouble("waterGoalLiters", 2.0),
                bottleVolumeLiters = p.optDouble("bottleVolumeLiters", 0.75),
                xp = p.optInt("xp", 0),
                skinIndex = p.optInt("skinIndex", 0),
                outfitIndex = p.optInt("outfitIndex", 0),
                shoeAccentIndex = p.optInt("shoeAccentIndex", 0),
                avatarJson = p.optString("avatarJson", "{}"),
                homeTiersJson = p.optString("homeTiersJson", "{}"),
                soundsEnabled = p.optBoolean("soundsEnabled", true),
                hapticsEnabled = p.optBoolean("hapticsEnabled", true),
                reduceMotion = p.optBoolean("reduceMotion", false),
                economy30Fps = p.optBoolean("economy30Fps", false),
                workoutReminderEnabled = p.optBoolean("workoutReminderEnabled", false),
                workoutReminderHour = p.optInt("workoutReminderHour", 18),
                workoutReminderMinute = p.optInt("workoutReminderMinute", 0),
                workoutReminderDaysCsv = p.optString("workoutReminderDaysCsv", "0,2,4,5"),
                waterReminderEnabled = p.optBoolean("waterReminderEnabled", false),
                waterReminderStartHour = p.optInt("waterReminderStartHour", 8),
                waterReminderEndHour = p.optInt("waterReminderEndHour", 21),
                waterReminderIntervalHours = p.optInt("waterReminderIntervalHours", 2),
                backupReminderMonthly = p.optBoolean("backupReminderMonthly", false),
                onboardingCompleted = p.optBoolean("onboardingCompleted", true),
            )
        }

        val workouts = buildList {
            val arr = root.optJSONArray("workouts") ?: JSONArray()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(WorkoutLogEntity(
                    dateIso = o.getString("dateIso"),
                    timestampMs = o.getLong("timestampMs"),
                    sessionName = o.getString("sessionName"),
                    location = o.getString("location"),
                    difficulty = o.getString("difficulty"),
                    completedExerciseIdsCsv = o.optString("completedExerciseIdsCsv", ""),
                    skippedExerciseIdsCsv = o.optString("skippedExerciseIdsCsv", ""),
                    estimatedCaloriesBurned = o.optInt("estimatedCaloriesBurned", 0),
                    xpEarned = o.optInt("xpEarned", 0),
                ))
            }
        }

        val foods = buildList {
            val arr = root.optJSONArray("foods") ?: JSONArray()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(FoodLogEntity(
                    dateIso = o.getString("dateIso"),
                    timestampMs = o.getLong("timestampMs"),
                    name = o.getString("name"),
                    calories = o.getInt("calories"),
                    proteinG = o.optDouble("proteinG", 0.0),
                    fatG = o.optDouble("fatG", 0.0),
                    carbsG = o.optDouble("carbsG", 0.0),
                    isDirectEntry = o.optBoolean("isDirectEntry", false),
                ))
            }
        }

        val waters = buildList {
            val arr = root.optJSONArray("waters") ?: JSONArray()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(WaterLogEntity(
                    dateIso = o.getString("dateIso"),
                    timestampMs = o.getLong("timestampMs"),
                    amountLiters = o.getDouble("amountLiters"),
                ))
            }
        }

        val weights = buildList {
            val arr = root.optJSONArray("weights") ?: JSONArray()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(WeightLogEntity(
                    dateIso = o.getString("dateIso"),
                    timestampMs = o.getLong("timestampMs"),
                    weightKg = o.getDouble("weightKg"),
                ))
            }
        }

        val measurements = buildList {
            val arr = root.optJSONArray("measurements") ?: JSONArray()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(MeasurementLogEntity(
                    dateIso = o.getString("dateIso"),
                    timestampMs = o.getLong("timestampMs"),
                    part = o.getString("part"),
                    valueCm = o.getDouble("valueCm"),
                ))
            }
        }

        val notes = buildList {
            val arr = root.optJSONArray("notes") ?: JSONArray()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(DailyNoteEntity(
                    dateIso = o.getString("dateIso"),
                    note = o.getString("note"),
                    updatedMs = o.getLong("updatedMs"),
                ))
            }
        }

        val ranked = buildList {
            val arr = root.optJSONArray("ranked") ?: JSONArray()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(RankedSnapshotEntity(
                    dateIso = o.getString("dateIso"),
                    timestampMs = o.getLong("timestampMs"),
                    rankCode = o.getString("rankCode"),
                    globalScore = o.getDouble("globalScore"),
                    maitriseScore = o.getDouble("maitriseScore"),
                    forceScore = o.getDouble("forceScore"),
                    explosiviteScore = o.getDouble("explosiviteScore"),
                    vitesseScore = o.getDouble("vitesseScore"),
                    enduranceScore = o.getDouble("enduranceScore"),
                    rawValuesJson = o.optString("rawValuesJson", "{}"),
                ))
            }
        }

        val checkins = buildList {
            val arr = root.optJSONArray("checkins") ?: JSONArray()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(DayCheckinEntity(
                    dateIso = o.getString("dateIso"),
                    status = o.getString("status"),
                    timestampMs = o.getLong("timestampMs"),
                ))
            }
        }

        val badges = buildList {
            val arr = root.optJSONArray("badges") ?: JSONArray()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(UnlockedBadgeEntity(
                    badgeId = o.getString("badgeId"),
                    unlockedMs = o.getLong("unlockedMs"),
                ))
            }
        }

        db.withTransaction {
            db.workoutDao().clearAll()
            db.foodDao().clearAll()
            db.waterDao().clearAll()
            db.weightDao().clearAll()
            db.measurementDao().clearAll()
            db.noteDao().clearAll()
            db.rankedDao().clearAll()
            db.checkinDao().clearAll()
            db.badgeDao().clearAll()

            if (profile != null) db.profileDao().upsertProfile(profile)
            if (workouts.isNotEmpty()) db.workoutDao().insertAll(workouts)
            if (foods.isNotEmpty()) db.foodDao().insertAll(foods)
            if (waters.isNotEmpty()) db.waterDao().insertAll(waters)
            if (weights.isNotEmpty()) db.weightDao().insertAll(weights)
            if (measurements.isNotEmpty()) db.measurementDao().insertAll(measurements)
            if (notes.isNotEmpty()) db.noteDao().insertAll(notes)
            if (ranked.isNotEmpty()) db.rankedDao().insertAll(ranked)
            if (checkins.isNotEmpty()) db.checkinDao().insertAll(checkins)
            if (badges.isNotEmpty()) db.badgeDao().insertAll(badges)
        }
    }

    fun avatarToJson(avatar: AvatarConfig): String = JSONObject().apply {
        put("skinIndex", avatar.skinIndex)
        put("faceShapeIndex", avatar.faceShapeIndex)
        put("hairStyleIndex", avatar.hairStyleIndex)
        put("hairColorIndex", avatar.hairColorIndex)
        put("eyeStyleIndex", avatar.eyeStyleIndex)
        put("browStyleIndex", avatar.browStyleIndex)
        put("mouthStyleIndex", avatar.mouthStyleIndex)
        put("beardStyleIndex", avatar.beardStyleIndex)
        put("glassesIndex", avatar.glassesIndex)
        put("earringIndex", avatar.earringIndex)
    }.toString()

    fun avatarFromJson(json: String): AvatarConfig = runCatching {
        val o = JSONObject(json)
        AvatarConfig(
            skinIndex = o.optInt("skinIndex", 0),
            faceShapeIndex = o.optInt("faceShapeIndex", 0),
            hairStyleIndex = o.optInt("hairStyleIndex", 1),
            hairColorIndex = o.optInt("hairColorIndex", 0),
            eyeStyleIndex = o.optInt("eyeStyleIndex", 0),
            browStyleIndex = o.optInt("browStyleIndex", 0),
            mouthStyleIndex = o.optInt("mouthStyleIndex", 0),
            beardStyleIndex = o.optInt("beardStyleIndex", 0),
            glassesIndex = o.optInt("glassesIndex", 0),
            earringIndex = o.optInt("earringIndex", 0),
        )
    }.getOrDefault(AvatarConfig())

    fun tiersToJson(tiers: Map<String, Int>): String = JSONObject().apply {
        tiers.forEach { (k, v) -> put(k, v) }
    }.toString()

    fun tiersFromJson(json: String): Map<String, Int> = runCatching {
        val o = JSONObject(json)
        buildMap {
            o.keys().forEach { key -> put(key, o.optInt(key, 0)) }
        }
    }.getOrDefault(emptyMap())

    fun rankedValuesToJson(values: Map<String, Float>): String = JSONObject().apply {
        values.forEach { (k, v) -> put(k, v.toDouble()) }
    }.toString()

    fun rankedValuesFromJson(json: String): Map<String, Float> = runCatching {
        val o = JSONObject(json)
        buildMap {
            o.keys().forEach { key -> put(key, o.optDouble(key, 0.0).toFloat()) }
        }
    }.getOrDefault(emptyMap())
}
