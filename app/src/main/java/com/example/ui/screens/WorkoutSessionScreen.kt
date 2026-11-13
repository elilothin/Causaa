package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.catalog.ExerciseCatalog
import com.example.data.model.WorkoutLocation
import com.example.ui.components.BentoCard
import com.example.ui.components.BentoLabel
import com.example.ui.components.CameraViewAngle
import com.example.ui.components.Character3DView
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalCausaSuiColors
import com.example.ui.viewmodel.ActiveWorkoutState
import com.example.ui.viewmodel.CausaSuiUiState

@Composable
fun WorkoutSessionScreen(
    state: CausaSuiUiState,
    session: ActiveWorkoutState,
    reduceMotion: Boolean,
    onAdvance: (skip: Boolean) -> Unit,
    onUpgradeHomeVariant: (exerciseId: String) -> Unit,
    onUpdateCalories: (Int) -> Unit,
    onValidateSession: () -> Unit,
    onQuitConfirmed: () -> Unit,
) {
    val colors = LocalCausaSuiColors.current
    var showQuitConfirm by remember { mutableStateOf(false) }
    var cameraAngle by remember { mutableStateOf(CameraViewAngle.FACE) }

    BackHandler { showQuitConfirm = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header avec croix de sortie (aucun enregistrement si quitté)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CAUSA SUI · ${session.sessionName}",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.text
                )
                Text(
                    text = if (session.isSummaryStage) {
                        "Bilan final"
                    } else {
                        "Exercice ${session.currentIndex + 1} / ${session.exerciseIds.size} · ${session.location.label}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.card)
                    .testTag("btn_quit_workout")
                    .clickable { showQuitConfirm = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Close, contentDescription = "Quitter la séance", tint = colors.text)
            }
        }

        if (!session.isSummaryStage) {
            val exId = session.exerciseIds.getOrElse(session.currentIndex) { session.exerciseIds.first() }
            val def = ExerciseCatalog.exercises[exId]!!
            val tier = state.homeTiers[exId] ?: 0
            val displayName = ExerciseCatalog.displayExerciseName(def, session.location, tier)
            val sets = ExerciseCatalog.adjustedSets(def.baseSets, session.difficulty)
            val animKind = if (session.location == WorkoutLocation.SALLE) def.animGym else def.animHome

            Text(
                text = displayName,
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 26.sp),
                color = colors.text
            )

            // Carte 3D Orange avec mouvement en boucle + sélecteur Face / Profil / Dos
            BentoCard(
                backgroundColor = colors.workout,
                cornerRadius = 28.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BentoLabel(def.muscleGroup, colors.onWorkout.copy(alpha = 0.8f))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CameraViewAngle.entries.forEach { angle ->
                            val sel = cameraAngle == angle
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (sel) colors.onWorkout else colors.onWorkout.copy(alpha = 0.18f)
                                    )
                                    .clickable { cameraAngle = angle }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = angle.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (sel) colors.workout else colors.onWorkout
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Character3DView(
                        params = state.characterParams,
                        animation = animKind,
                        cameraAngle = cameraAngle,
                        reduceMotion = reduceMotion,
                        economy30Fps = state.profile.economy30Fps,
                        interactiveOrbit = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Séries × Répétitions + Repos conseillé (sans aucun minuteur !)
            BentoCard(backgroundColor = colors.card) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        BentoLabel("SÉRIES × RÉPÉTITIONS", colors.textMuted)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "$sets × ${def.baseReps}",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 34.sp,
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            color = colors.text
                        )
                    }
                    Text(
                        text = "Repos ~${def.restSeconds} s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            // Conseil matériel & équivalence maison
            BentoCard(backgroundColor = colors.card) {
                val tipText = if (session.location == WorkoutLocation.MAISON) {
                    "${def.tipNoEquipment} ${def.tipDumbbellHome}"
                } else {
                    "Conseil : ${def.tipNoEquipment}"
                }
                Text(
                    text = tipText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.text
                )
                if (session.location == WorkoutLocation.MAISON && def.approximateHomeEquivalence) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Note : équivalence approximative sans poulie ni machine guidée.",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.workout
                    )
                }
                if (session.location == WorkoutLocation.MAISON && tier < def.homeVariants.lastIndex) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Trop facile ? Monter d'un cran (${def.homeVariants[tier + 1]}) →",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.workout,
                        modifier = Modifier.clickable { onUpgradeHomeVariant(exId) }
                    )
                }
            }

            // Boutons PASSER et SUIVANT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onAdvance(true) },
                    modifier = Modifier.weight(1f).height(54.dp).testTag("btn_workout_skip"),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("PASSER", style = MaterialTheme.typography.labelLarge, color = colors.text)
                }
                Button(
                    onClick = { onAdvance(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.workout),
                    modifier = Modifier.weight(1.3f).height(54.dp).testTag("btn_workout_next"),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    val isLast = session.currentIndex == session.exerciseIds.lastIndex
                    Text(
                        text = if (isLast) "TERMINER" else "SUIVANT",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.onWorkout
                    )
                }
            }
        } else {
            // Bilan final
            var kcalText by remember(session.editableCaloriesBurned) {
                mutableStateOf(session.editableCaloriesBurned.toString())
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                BentoCard(backgroundColor = colors.workout) {
                    BentoLabel("BILAN DE LA SÉANCE", colors.onWorkout.copy(alpha = 0.8f))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "+${session.earnedXp} XP",
                        style = MaterialTheme.typography.displayLarge,
                        color = colors.onWorkout
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "« ${session.summaryPunchline} »",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onWorkout
                    )
                }

                BentoCard(backgroundColor = colors.card) {
                    BentoLabel("CALORIES DÉPENSÉES ESTIMÉES", colors.textMuted)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Estimation calculée selon ton poids, les séries réalisées et le MET des exercices. Tu peux la corriger :",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = kcalText,
                        onValueChange = { raw ->
                            val clean = raw.filter { it.isDigit() }
                            kcalText = clean
                            clean.toIntOrNull()?.let { onUpdateCalories(it) }
                        },
                        label = { Text("kcal estimées") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                BentoCard(backgroundColor = colors.card) {
                    BentoLabel("EXERCICES FAITS (${session.completedIds.size})", colors.progress)
                    Spacer(Modifier.height(6.dp))
                    if (session.completedIds.isEmpty()) {
                        Text("Aucun exercice validé.", style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
                    } else {
                        session.completedIds.forEach { id ->
                            val def = ExerciseCatalog.exercises[id]
                            if (def != null) {
                                Text(
                                    "• ${ExerciseCatalog.displayExerciseName(def, session.location, state.homeTiers[id] ?: 0)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.text
                                )
                            }
                        }
                    }

                    if (session.skippedIds.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        BentoLabel("EXERCICES PASSÉS (${session.skippedIds.size})", colors.workout)
                        Spacer(Modifier.height(6.dp))
                        session.skippedIds.forEach { id ->
                            val def = ExerciseCatalog.exercises[id]
                            if (def != null) {
                                Text(
                                    "• ${ExerciseCatalog.displayExerciseName(def, session.location, state.homeTiers[id] ?: 0)} (non validé)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textMuted
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onValidateSession,
                colors = ButtonDefaults.buttonColors(containerColor = colors.progress),
                modifier = Modifier.fillMaxWidth().height(56.dp).testTag("btn_validate_workout"),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    "VALIDER LA JOURNÉE",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onProgress
                )
            }
        }
    }

    if (showQuitConfirm) {
        AlertDialog(
            onDismissRequest = { showQuitConfirm = false },
            containerColor = colors.card,
            title = { Text("Quitter la séance ?", color = colors.text) },
            text = {
                Text(
                    "Quitter en cours n'enregistre rien dans ton historique ni ta série.",
                    color = colors.textMuted
                )
            },
            confirmButton = {
                TextButton(onClick = { showQuitConfirm = false; onQuitConfirmed() }) {
                    Text("Quitter sans enregistrer", color = colors.workout)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitConfirm = false }) {
                    Text("Continuer la séance", color = colors.text)
                }
            }
        )
    }
}
