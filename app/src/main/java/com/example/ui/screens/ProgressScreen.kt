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
import com.example.data.local.DateUtilsCompat
import com.example.data.model.DayStatus
import com.example.data.model.MeasurementPart
import com.example.data.model.RankTier
import com.example.ui.components.*
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalCausaSuiColors
import com.example.ui.viewmodel.CausaSuiUiState

@Composable
fun ProgressScreen(
    state: CausaSuiUiState,
    onAddMeasurement: (MeasurementPart, Double) -> Unit,
    onSaveNote: (String, String) -> Unit,
) {
    val colors = LocalCausaSuiColors.current
    var periodDays by remember { mutableIntStateOf(7) } // 7 = Semaine, 30 = Mois
    var selectedPart by remember { mutableStateOf(MeasurementPart.BRAS) }
    var showMeasureDialog by remember { mutableStateOf(false) }

    val todayIso = DateUtilsCompat.todayIso()
    var noteDateIso by remember { mutableStateOf(todayIso) }
    val existingNote = state.notes.firstOrNull { it.dateIso == noteDateIso }?.note ?: ""
    var noteText by remember(noteDateIso, existingNote) { mutableStateOf(existingNote) }

    // Données filtrées sur la période (Semaine = 7 j, Mois = 30 j)
    val startIso = DateUtilsCompat.addDays(todayIso, -(periodDays - 1))
    val dateRange = remember(periodDays, todayIso) {
        (0 until periodDays).map { DateUtilsCompat.addDays(startIso, it) }
    }

    val waterByDay = remember(state.allWater, dateRange) {
        val grouped = state.allWater.groupBy { it.dateIso }
        dateRange.map { d -> d to (grouped[d]?.sumOf { it.amountLiters } ?: 0.0) }
    }
    val sessionsByDay = remember(state.workouts, dateRange) {
        val grouped = state.workouts.groupBy { it.dateIso }
        dateRange.map { d -> grouped[d]?.size ?: 0 }
    }
    val weightPoints = remember(state.weights, startIso) {
        val recent = state.weights.filter { it.dateIso >= startIso }
        (recent.ifEmpty { state.weights.takeLast(10) }).map { it.dateIso to it.weightKg }
    }
    val rankHistory = remember(state.rankedSnapshots) {
        state.rankedSnapshots.map { RankTier.fromCode(it.rankCode) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progrès",
                style = MaterialTheme.typography.displayMedium,
                color = colors.text
            )
            // Sélecteur Semaine · Mois
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(colors.card)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(7 to "Semaine", 30 to "Mois").forEach { (days, label) ->
                    val sel = periodDays == days
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (sel) colors.progress else colors.card)
                            .testTag("progress_period_$days")
                            .clickable { periodDays = days }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (sel) colors.onProgress else colors.textMuted
                        )
                    }
                }
            }
        }

        // Courbe 1 : Poids (Vert)
        BentoCard(backgroundColor = colors.progress) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BentoLabel("POIDS · TRAJECTOIRE", colors.onProgress.copy(alpha = 0.8f))
                Text(
                    text = "Cible ${"%.1f".format(state.profile.targetWeightKg)} kg",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onProgress.copy(alpha = 0.8f)
                )
            }
            Spacer(Modifier.height(10.dp))
            WeightLineChart(
                points = weightPoints,
                targetWeightKg = state.profile.targetWeightKg,
                lineColor = colors.onProgress,
                gridColor = colors.onProgress.copy(alpha = 0.35f)
            )
        }

        // Courbes 2 & 3 côte à côte : Eau (Bleu/Marine) + Séances faites (Orange)
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BentoCard(
                backgroundColor = colors.water,
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                BentoLabel("EAU / JOUR", colors.onWater.copy(alpha = 0.8f))
                Spacer(Modifier.height(8.dp))
                WaterBarChart(
                    dailyLiters = waterByDay,
                    goalLiters = state.profile.waterGoalLiters,
                    barColor = colors.onWater,
                    goalLineColor = colors.onWater.copy(alpha = 0.45f)
                )
            }

            BentoCard(
                backgroundColor = colors.workout,
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                BentoLabel("SÉANCES", colors.onWorkout.copy(alpha = 0.8f))
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${state.workouts.size}",
                    style = MaterialTheme.typography.headlineLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = colors.onWorkout
                )
                SessionsLineChart(
                    counts = sessionsByDay,
                    lineColor = colors.onWorkout
                )
            }
        }

        // Courbe 4 : Évolution du Rang (courbe en marches)
        BentoCard(backgroundColor = colors.card) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BentoLabel("ÉVOLUTION DU RANG", colors.textMuted)
                Text(
                    text = "Actuel : ${state.latestRank.code}",
                    style = MaterialTheme.typography.labelLarge,
                    color = state.latestRank.color
                )
            }
            Spacer(Modifier.height(8.dp))
            RankStepChart(
                ranks = rankHistory,
                lineColor = colors.rankGold
            )
        }

        // Calendrier en points du mois + Séries
        BentoCard(backgroundColor = colors.card) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BentoLabel("CALENDRIER · ${DateUtilsCompat.currentMonthLabel()}", colors.textMuted)
                Text(
                    text = "${state.completionRatePct} % assiduité",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.progress
                )
            }
            Spacer(Modifier.height(12.dp))

            val checkinMap = state.checkins.associateBy { it.dateIso }
            val firstRecordedDate = state.checkins.minOfOrNull { it.dateIso } ?: todayIso
            val monthDays = DateUtilsCompat.currentMonthDays().map { (dayNum, iso) ->
                val checkin = checkinMap[iso]
                val status = when {
                    checkin?.status == DayStatus.WORKOUT_DONE.name -> DayStatus.WORKOUT_DONE
                    checkin?.status == DayStatus.REST_VALIDATED.name -> DayStatus.REST_VALIDATED
                    iso == todayIso -> DayStatus.TODAY_PENDING
                    iso > todayIso -> DayStatus.UPCOMING
                    iso < firstRecordedDate -> DayStatus.UPCOMING
                    else -> DayStatus.MISSED
                }
                dayNum to status
            }

            DotsCalendar(
                days = monthDays,
                workoutColor = colors.workout,
                restColor = colors.text,
                missedColor = colors.textMuted.copy(alpha = 0.65f),
                upcomingColor = colors.textMuted.copy(alpha = 0.35f),
                todayRingColor = colors.rankGold,
                onDayClick = { dayNum ->
                    val clickedIso = monthDays.getOrNull(dayNum - 1)?.let {
                        DateUtilsCompat.currentMonthDays().getOrNull(dayNum - 1)?.second
                    }
                    if (clickedIso != null) noteDateIso = clickedIso
                }
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Plein = séance · Point = repos · Losange = raté · Anneau = aujourd'hui · Série : ${state.currentStreak} j (record ${state.bestStreak} j)",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted
            )
        }

        // Mensurations (sans photo !) + courbe par mesure
        BentoCard(backgroundColor = colors.card) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BentoLabel("MENSURATIONS (CM)", colors.textMuted)
                TextButton(onClick = { showMeasureDialog = true }) {
                    Text("+ Saisir", color = colors.progress)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MeasurementPart.entries.forEach { part ->
                    val sel = selectedPart == part
                    FilterChip(
                        selected = sel,
                        onClick = { selectedPart = part },
                        label = { Text(part.label) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            val partPoints = state.measurements
                .filter { it.part == selectedPart.name }
                .map { it.dateIso to it.valueCm }
            val latestVal = partPoints.lastOrNull()?.second
            if (latestVal != null) {
                Text(
                    text = "${selectedPart.label} : ${"%.1f".format(latestVal)} cm",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = colors.text
                )
            }
            WeightLineChart(
                points = partPoints,
                targetWeightKg = latestVal ?: 35.0,
                lineColor = colors.progress,
                gridColor = colors.textMuted.copy(alpha = 0.25f)
            )
        }

        // Notes libres par date (journal sans photo)
        BentoCard(backgroundColor = colors.card) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BentoLabel("NOTES DU ${DateUtilsCompat.formatShortDisplay(noteDateIso)}", colors.textMuted)
                if (noteDateIso != todayIso) {
                    Text(
                        "Aujourd'hui",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.progress,
                        modifier = Modifier.clickable { noteDateIso = todayIso }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                placeholder = { Text("Sensations, charges du jour, fatigue...") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onSaveNote(noteDateIso, noteText) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.progress),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Enregistrer la note", color = colors.onProgress)
            }
        }
    }

    if (showMeasureDialog) {
        var valueStr by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showMeasureDialog = false },
            containerColor = colors.card,
            title = { Text("Ajouter : ${selectedPart.label}", color = colors.text) },
            text = {
                OutlinedTextField(
                    value = valueStr,
                    onValueChange = { valueStr = it.replace(',', '.') },
                    label = { Text("Mesure en cm") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        valueStr.toDoubleOrNull()?.let { onAddMeasurement(selectedPart, it) }
                        showMeasureDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.progress)
                ) {
                    Text("Enregistrer", color = colors.onProgress)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMeasureDialog = false }) {
                    Text("Annuler", color = colors.textMuted)
                }
            }
        )
    }
}
