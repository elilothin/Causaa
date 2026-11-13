package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DateUtilsCompat
import com.example.data.model.AnimationKind
import com.example.data.model.DayPlanItem
import com.example.data.model.WorkoutLocation
import com.example.ui.components.*
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalCausaSuiColors
import com.example.ui.viewmodel.CausaSuiUiState
import com.example.ui.viewmodel.MainTab

@Composable
fun HomeScreen(
    state: CausaSuiUiState,
    reduceMotion: Boolean,
    onSetLocation: (WorkoutLocation) -> Unit,
    onLaunchWorkout: (DayPlanItem) -> Unit,
    onValidateRestDay: () -> Unit,
    onAddWater: (Double) -> Unit,
    onLogWeight: (Double) -> Unit,
    onNavigateTab: (MainTab) -> Unit,
) {
    val colors = LocalCausaSuiColors.current
    val location = runCatching { WorkoutLocation.valueOf(state.profile.preferredLocation) }
        .getOrDefault(WorkoutLocation.SALLE)

    var showWeightDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Grande carte Séance du jour (orange) avec personnage 3D et choix Salle / Maison
        BentoCard(
            backgroundColor = colors.workout,
            cornerRadius = 28.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1.25f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BentoLabel(
                        text = if (state.todayPlan.isRestDay) "JOURNÉE PRÉVUE" else "SÉANCE DU JOUR",
                        color = colors.onWorkout.copy(alpha = 0.8f)
                    )
                    Text(
                        text = state.todayPlan.sessionName,
                        style = MaterialTheme.typography.displayMedium.copy(fontSize = 28.sp),
                        color = colors.onWorkout,
                    )

                    if (!state.todayPlan.isRestDay) {
                        // Interrupteur Salle / Maison
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.Black.copy(alpha = 0.20f))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            WorkoutLocation.entries.forEach { loc ->
                                val active = location == loc
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(if (active) colors.onWorkout else Color.Transparent)
                                        .testTag("home_loc_${loc.name.lowercase()}")
                                        .clickable { onSetLocation(loc) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = loc.label.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            letterSpacing = 1.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (active) colors.workout else colors.onWorkout
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = { onLaunchWorkout(state.todayPlan) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.onWorkout,
                                contentColor = colors.workout,
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.height(48.dp).testTag("btn_launch_workout")
                        ) {
                            Text(
                                text = if (state.todayValidated) "REFAIRE" else "LANCER",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    } else {
                        Text(
                            text = if (state.todayValidated) "Repos validé aujourd'hui." else "Valide ton repos pour garder ta série.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onWorkout.copy(alpha = 0.9f),
                        )
                        Button(
                            onClick = onValidateRestDay,
                            enabled = !state.todayValidated,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.onWorkout,
                                contentColor = colors.workout,
                                disabledContainerColor = colors.onWorkout.copy(alpha = 0.45f)
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.height(48.dp).testTag("btn_validate_rest")
                        ) {
                            Text(
                                text = if (state.todayValidated) "REPOS VALIDÉ" else "VALIDER REPOS",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                // Personnage 3D en direct (avec tenue/morphologie de l'utilisateur)
                Box(
                    modifier = Modifier
                        .weight(0.95f)
                        .height(172.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Character3DView(
                        params = state.characterParams,
                        animation = AnimationKind.IDLE_BREATHE,
                        reduceMotion = reduceMotion,
                        economy30Fps = state.profile.economy30Fps,
                        interactiveOrbit = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Ligne 1 Bento : Poids + Rang
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BentoCard(
                backgroundColor = colors.card,
                modifier = Modifier.weight(1f).fillMaxHeight().testTag("card_home_weight"),
                onClick = { showWeightDialog = true }
            ) {
                BentoLabel("POIDS", colors.textMuted)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "%.1f".format(state.profile.weightKg).replace('.', ','),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                        color = colors.text,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "kg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
                val diff = state.profile.targetWeightKg - state.profile.weightKg
                val diffText = when {
                    kotlin.math.abs(diff) < 0.2 -> "Objectif atteint"
                    diff > 0 -> "Cible +${"%.1f".format(diff)} kg"
                    else -> "Cible ${"%.1f".format(diff)} kg"
                }
                Text(diffText, style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
            }

            BentoCard(
                backgroundColor = colors.rankGold,
                modifier = Modifier.weight(1f).fillMaxHeight().testTag("card_home_rank"),
                onClick = { onNavigateTab(MainTab.MOI) }
            ) {
                BentoLabel("RANG", colors.onRankGold.copy(alpha = 0.75f))
                Spacer(Modifier.height(6.dp))
                Text(
                    text = state.latestRank.code,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 46.sp),
                    color = colors.onRankGold,
                )
                Text(
                    text = state.latestRank.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.onRankGold.copy(alpha = 0.85f),
                )
            }
        }

        // Ligne 2 Bento : Eau + Série
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BentoCard(
                backgroundColor = colors.water,
                modifier = Modifier.weight(1f).fillMaxHeight().testTag("card_home_water")
            ) {
                BentoLabel("EAU", colors.onWater.copy(alpha = 0.8f))
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${"%.2f".format(state.todayWaterLiters).replace('.', ',')} / ${"%.1f".format(state.profile.waterGoalLiters).replace('.', ',')} L",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.onWater
                )
                Spacer(Modifier.height(10.dp))
                SegmentedWaterGauge(
                    currentLiters = state.todayWaterLiters,
                    goalLiters = state.profile.waterGoalLiters,
                    filledColor = colors.onWater,
                    emptyColor = colors.onWater.copy(alpha = 0.25f)
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SmallWaterChip("+0,25", colors.onWater, colors.water) { onAddWater(0.25) }
                    SmallWaterChip("+0,5", colors.onWater, colors.water) { onAddWater(0.5) }
                }
            }

            BentoCard(
                backgroundColor = colors.progress,
                modifier = Modifier.weight(1f).fillMaxHeight().testTag("card_home_streak"),
                onClick = { onNavigateTab(MainTab.PROGRES) }
            ) {
                BentoLabel("SÉRIE", colors.onProgress.copy(alpha = 0.8f))
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${state.currentStreak} jours",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = colors.onProgress
                )
                Spacer(Modifier.height(10.dp))
                // Mini rangée de points 7 jours
                val checkinSet = state.checkins.map { it.dateIso }.toSet()
                val todayIso = DateUtilsCompat.todayIso()
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (offset in -6..0) {
                        val d = DateUtilsCompat.addDays(todayIso, offset)
                        val done = checkinSet.contains(d)
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(CircleShape)
                                .background(
                                    if (done) colors.onProgress else colors.onProgress.copy(alpha = 0.25f)
                                )
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Record : ${state.bestStreak} j",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onProgress.copy(alpha = 0.85f)
                )
            }
        }

        // Tuile Calories du jour
        val gainedKcal = state.todayFoods.sumOf { it.calories }
        val targetKcal = state.nutritionTargets.targetCalories
        BentoCard(
            backgroundColor = colors.card,
            modifier = Modifier.fillMaxWidth().testTag("card_home_calories"),
            onClick = { onNavigateTab(MainTab.NUTRITION) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BentoLabel("CALORIES DU JOUR", colors.textMuted)
                Text(
                    text = "Dépensées : ${state.todayBurnedKcal} kcal",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.workout
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "$gainedKcal / $targetKcal kcal",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = colors.text
            )
            Spacer(Modifier.height(10.dp))
            ThickProgressBar(
                progress = if (targetKcal > 0) gainedKcal.toFloat() / targetKcal else 0f,
                trackColor = colors.cardElevated,
                fillColor = colors.nutrition,
            )
        }

        // Tuile Punchline du jour
        BentoCard(backgroundColor = colors.card) {
            BentoLabel("PUNCHLINE DU JOUR", colors.workout)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "« ${state.dailyPunchline} »",
                style = MaterialTheme.typography.titleMedium,
                color = colors.text,
            )
        }
    }

    if (showWeightDialog) {
        var wInput by remember { mutableStateOf("%.1f".format(state.profile.weightKg)) }
        AlertDialog(
            onDismissRequest = { showWeightDialog = false },
            containerColor = colors.card,
            title = { Text("Nouvelle pesée (kg)", color = colors.text) },
            text = {
                OutlinedTextField(
                    value = wInput,
                    onValueChange = { wInput = it.replace(',', '.') },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    wInput.toDoubleOrNull()?.let { onLogWeight(it) }
                    showWeightDialog = false
                }) {
                    Text("Enregistrer", color = colors.workout)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWeightDialog = false }) {
                    Text("Annuler", color = colors.textMuted)
                }
            }
        )
    }
}

@Composable
private fun SmallWaterChip(
    label: String,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor.copy(alpha = 0.92f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
    }
}
