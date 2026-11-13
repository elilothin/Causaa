package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.catalog.NutritionCalculator
import com.example.data.local.UserProfileEntity
import com.example.data.model.*
import com.example.ui.components.BentoCard
import com.example.ui.components.BentoLabel
import com.example.ui.theme.LocalCausaSuiColors
import kotlin.math.ceil
import kotlin.math.max

@Composable
fun OnboardingScreen(
    currentTheme: AppThemeMode,
    onSelectTheme: (AppThemeMode) -> Unit,
    onComplete: (UserProfileEntity) -> Unit,
) {
    val colors = LocalCausaSuiColors.current
    var step by remember { mutableIntStateOf(0) }

    var pseudo by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf(Sex.HOMME) }
    var ageStr by remember { mutableStateOf("22") }
    var heightStr by remember { mutableStateOf("175") }
    var weightStr by remember { mutableStateOf("75") }
    var morphotype by remember { mutableStateOf(Morphotype.MESOMORPHE) }
    var showQuiz by remember { mutableStateOf(false) }
    var quizQ1 by remember { mutableIntStateOf(1) } // 0 ecto, 1 meso, 2 endo
    var quizQ2 by remember { mutableIntStateOf(1) }
    var quizQ3 by remember { mutableIntStateOf(1) }

    var goal by remember { mutableStateOf(FitnessGoal.MAINTIEN) }
    var targetWeightStr by remember { mutableStateOf("75") }
    var weeksStr by remember { mutableStateOf("12") }
    var daysPerWeek by remember { mutableIntStateOf(4) }
    var physicalJob by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "CAUSA SUI",
            style = MaterialTheme.typography.labelLarge,
            color = colors.workout,
        )
        Text(
            text = when (step) {
                0 -> "Choisis ton thème"
                1 -> "Ton profil"
                2 -> "Ta morphologie"
                else -> "Ton objectif"
            },
            style = MaterialTheme.typography.displayMedium,
            color = colors.text,
        )
        Text(
            text = "Étape ${step + 1} sur 4 · 100 % hors-ligne, tout reste sur ton téléphone.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textMuted,
        )

        when (step) {
            0 -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ThemeOptionCard(
                        title = "Beige",
                        subtitle = "Chaleureux, défaut",
                        selected = currentTheme == AppThemeMode.BEIGE,
                        previewBg = androidx.compose.ui.graphics.Color(0xFFEFE6D2),
                        previewAccent = androidx.compose.ui.graphics.Color(0xFFD9622B),
                        previewText = androidx.compose.ui.graphics.Color(0xFF1E1E22),
                        onClick = { onSelectTheme(AppThemeMode.BEIGE) },
                        modifier = Modifier.weight(1f).testTag("onboarding_theme_beige")
                    )
                    ThemeOptionCard(
                        title = "Sombre",
                        subtitle = "Noir mat, contraste fort",
                        selected = currentTheme == AppThemeMode.DARK,
                        previewBg = androidx.compose.ui.graphics.Color(0xFF0E0E10),
                        previewAccent = androidx.compose.ui.graphics.Color(0xFFE8522B),
                        previewText = androidx.compose.ui.graphics.Color(0xFFF4F2EE),
                        onClick = { onSelectTheme(AppThemeMode.DARK) },
                        modifier = Modifier.weight(1f).testTag("onboarding_theme_dark")
                    )
                }
            }

            1 -> {
                BentoCard(backgroundColor = colors.card) {
                    BentoLabel("PSEUDO (3 À 20 CARACTÈRES)", colors.textMuted)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pseudo,
                        onValueChange = { if (it.length <= 20) pseudo = it; errorMsg = null },
                        placeholder = { Text("Ex. Laurence") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("onboarding_pseudo_input")
                    )
                    Spacer(Modifier.height(14.dp))
                    BentoLabel("SEXE (FORMULES & SEUILS)", colors.textMuted)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Sex.entries.forEach { s ->
                            FilterChip(
                                selected = sex == s,
                                onClick = { sex = s },
                                label = { Text(s.label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = ageStr,
                            onValueChange = { ageStr = it.filter { c -> c.isDigit() }; errorMsg = null },
                            label = { Text("Âge (16-90)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = heightStr,
                            onValueChange = { heightStr = it.filter { c -> c.isDigit() }; errorMsg = null },
                            label = { Text("Taille (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = weightStr,
                        onValueChange = {
                            weightStr = it.replace(',', '.')
                            targetWeightStr = weightStr
                            errorMsg = null
                        },
                        label = { Text("Poids actuel (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            2 -> {
                Morphotype.entries.forEach { m ->
                    val selected = morphotype == m
                    BentoCard(
                        backgroundColor = if (selected) colors.workout else colors.card,
                        onClick = { morphotype = m }
                    ) {
                        val fg = if (selected) colors.onWorkout else colors.text
                        val sub = if (selected) colors.onWorkout.copy(alpha = 0.85f) else colors.textMuted
                        Text(m.label, style = MaterialTheme.typography.headlineMedium, color = fg)
                        Spacer(Modifier.height(4.dp))
                        Text(m.description, style = MaterialTheme.typography.bodyMedium, color = sub)
                    }
                }

                TextButton(onClick = { showQuiz = !showQuiz }) {
                    Text(
                        if (showQuiz) "Masquer le mini-quiz" else "Tu ne sais pas ? Mini-quiz en 3 questions",
                        color = colors.workout
                    )
                }

                if (showQuiz) {
                    BentoCard(backgroundColor = colors.card) {
                        Text("1. Tour de poignet & ossature", style = MaterialTheme.typography.titleMedium, color = colors.text)
                        QuizRow(listOf("Fin", "Moyen", "Large"), quizQ1) { quizQ1 = it }
                        Spacer(Modifier.height(10.dp))
                        Text("2. Évolution de ton poids", style = MaterialTheme.typography.titleMedium, color = colors.text)
                        QuizRow(listOf("Prend peu", "Équilibré", "Stocke vite"), quizQ2) { quizQ2 = it }
                        Spacer(Modifier.height(10.dp))
                        Text("3. Carrure naturelle", style = MaterialTheme.typography.titleMedium, color = colors.text)
                        QuizRow(listOf("Élancée", "Athlétique", "Massive"), quizQ3) { quizQ3 = it }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val avg = (quizQ1 + quizQ2 + quizQ3) / 3.0
                                morphotype = when {
                                    avg < 0.7 -> Morphotype.ECTOMORPHE
                                    avg > 1.3 -> Morphotype.ENDOMORPHE
                                    else -> Morphotype.MESOMORPHE
                                }
                                showQuiz = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.workout)
                        ) {
                            Text("Appliquer le résultat", color = colors.onWorkout)
                        }
                    }
                }
            }

            3 -> {
                val age = ageStr.toIntOrNull() ?: 22
                val availableGoals = if (age < 18) {
                    listOf(FitnessGoal.MAINTIEN, FitnessGoal.PRISE_MASSE)
                } else {
                    FitnessGoal.entries
                }
                if (goal !in availableGoals) goal = FitnessGoal.MAINTIEN

                BentoCard(backgroundColor = colors.card) {
                    BentoLabel("OBJECTIF", colors.textMuted)
                    if (age < 18) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Moins de 18 ans : maintien et prise de masse uniquement (aucun déficit calorique).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.workout
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    availableGoals.forEach { g ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { goal = g; errorMsg = null }
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = goal == g, onClick = { goal = g; errorMsg = null })
                            Spacer(Modifier.width(8.dp))
                            Text(g.label, style = MaterialTheme.typography.titleMedium, color = colors.text)
                        }
                    }

                    if (goal == FitnessGoal.PERTE_POIDS || goal == FitnessGoal.PRISE_MASSE) {
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = targetWeightStr,
                                onValueChange = { targetWeightStr = it.replace(',', '.'); errorMsg = null },
                                label = { Text("Poids visé (kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = weeksStr,
                                onValueChange = { weeksStr = it.filter { c -> c.isDigit() }; errorMsg = null },
                                label = { Text("Délai (semaines)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    BentoLabel("JOURS D'ENTRAÎNEMENT PAR SEMAINE ($daysPerWeek)", colors.textMuted)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (2..6).forEach { d ->
                            val sel = daysPerWeek == d
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (sel) colors.workout else colors.cardElevated)
                                    .clickable { daysPerWeek = d },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$d",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (sel) colors.onWorkout else colors.text
                                )
                            }
                        }
                    }

                    if (daysPerWeek == 6) {
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = physicalJob, onCheckedChange = { physicalJob = it })
                            Text("Métier physique (facteur ×1,9)", style = MaterialTheme.typography.bodyMedium, color = colors.text)
                        }
                    }
                }

                Text(
                    text = "Estimations indicatives, pas un avis médical.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted,
                )
            }
        }

        if (errorMsg != null) {
            BentoCard(backgroundColor = colors.workout.copy(alpha = 0.18f)) {
                Text(errorMsg!!, style = MaterialTheme.typography.bodyMedium, color = colors.workout)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (step > 0) {
                OutlinedButton(
                    onClick = { step--; errorMsg = null },
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Précédent")
                }
            }
            Button(
                onClick = {
                    errorMsg = null
                    when (step) {
                        0 -> step = 1
                        1 -> {
                            val trimmed = pseudo.trim()
                            val age = ageStr.toIntOrNull()
                            val h = heightStr.toIntOrNull()
                            val w = weightStr.toDoubleOrNull()
                            when {
                                trimmed.length !in 3..20 -> errorMsg = "Le pseudo doit contenir entre 3 et 20 caractères."
                                age == null || age !in 16..90 -> errorMsg = "L'âge doit être compris entre 16 et 90 ans."
                                h == null || h !in 130..220 -> errorMsg = "Taille refusée : saisis une valeur réaliste entre 130 et 220 cm."
                                w == null || w !in 35.0..250.0 -> errorMsg = "Poids refusé : saisis une valeur réaliste entre 35 et 250 kg."
                                else -> step = 2
                            }
                        }
                        2 -> step = 3
                        3 -> {
                            val age = ageStr.toIntOrNull() ?: 22
                            val h = heightStr.toIntOrNull() ?: 175
                            val w = weightStr.toDoubleOrNull() ?: 75.0
                            val targetW = targetWeightStr.toDoubleOrNull() ?: w
                            val weeks = weeksStr.toIntOrNull() ?: 12

                            if ((goal == FitnessGoal.PERTE_POIDS || goal == FitnessGoal.PRISE_MASSE) && targetW !in 35.0..250.0) {
                                errorMsg = "Poids cible invalide (35 à 250 kg)."
                                return@Button
                            }
                            val guardrailErr = NutritionCalculator.validateSafeWeightTarget(w, targetW, h, weeks, goal)
                            if (guardrailErr != null) {
                                errorMsg = guardrailErr
                                if (goal == FitnessGoal.PERTE_POIDS && targetW < w) {
                                    val safeWeeks = max(1, ceil((w - targetW) / (w * 0.01)).toInt())
                                    weeksStr = safeWeeks.toString()
                                }
                                return@Button
                            }

                            onComplete(
                                UserProfileEntity(
                                    id = 1,
                                    pseudo = pseudo.trim(),
                                    sex = sex.name,
                                    age = age,
                                    heightCm = h,
                                    weightKg = w,
                                    morphotype = morphotype.name,
                                    goal = goal.name,
                                    targetWeightKg = if (goal == FitnessGoal.MAINTIEN || goal == FitnessGoal.RECOMPOSITION) w else targetW,
                                    daysPerWeek = daysPerWeek,
                                    physicalJob = physicalJob,
                                    themeMode = currentTheme.name,
                                    onboardingCompleted = true,
                                )
                            )
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.workout),
                modifier = Modifier.weight(1f).height(54.dp).testTag("onboarding_next_btn"),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = if (step == 3) "Générer mon programme" else "Continuer",
                    color = colors.onWorkout,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    previewBg: androidx.compose.ui.graphics.Color,
    previewAccent: androidx.compose.ui.graphics.Color,
    previewText: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalCausaSuiColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) colors.workout else colors.card)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(previewBg)
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(previewAccent)
            )
            Text(
                text = "75,2",
                color = previewText,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
        val fg = if (selected) colors.onWorkout else colors.text
        Text(title, style = MaterialTheme.typography.titleLarge, color = fg)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = fg.copy(alpha = 0.8f))
    }
}

@Composable
private fun QuizRow(
    options: List<String>,
    selectedIdx: Int,
    onSelect: (Int) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
        options.forEachIndexed { idx, label ->
            FilterChip(
                selected = selectedIdx == idx,
                onClick = { onSelect(idx) },
                label = { Text(label) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
