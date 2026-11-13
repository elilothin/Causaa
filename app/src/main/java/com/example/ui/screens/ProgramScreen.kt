package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.catalog.ExerciseCatalog
import com.example.data.local.DateUtilsCompat
import com.example.data.model.DayPlanItem
import com.example.data.model.DifficultyLevel
import com.example.data.model.WorkoutLocation
import com.example.ui.components.BentoCard
import com.example.ui.components.BentoLabel
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalCausaSuiColors
import com.example.ui.viewmodel.CausaSuiUiState

@Composable
fun ProgramScreen(
    state: CausaSuiUiState,
    onSetDaysPerWeek: (Int) -> Unit,
    onSetThreeDayPpl: (Boolean) -> Unit,
    onSetLocation: (WorkoutLocation) -> Unit,
    onSetDifficulty: (DifficultyLevel) -> Unit,
    onLaunchWorkout: (DayPlanItem) -> Unit,
) {
    val colors = LocalCausaSuiColors.current
    val location = runCatching { WorkoutLocation.valueOf(state.profile.preferredLocation) }
        .getOrDefault(WorkoutLocation.SALLE)
    val difficulty = runCatching { DifficultyLevel.valueOf(state.profile.difficulty) }
        .getOrDefault(DifficultyLevel.MOYEN)
    val todayDow = DateUtilsCompat.dayOfWeekIndex()
    var inspectedDay by remember { mutableStateOf<DayPlanItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Programme",
            style = MaterialTheme.typography.displayMedium,
            color = colors.text
        )

        // Réglages rapides : Jours, Salle/Maison, Difficulté
        BentoCard(backgroundColor = colors.card) {
            BentoLabel("JOURS PAR SEMAINE", colors.textMuted)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (2..6).forEach { d ->
                    val sel = state.profile.daysPerWeek == d
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (sel) colors.workout else colors.cardElevated)
                            .testTag("prog_days_$d")
                            .clickable { onSetDaysPerWeek(d) },
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

            if (state.profile.daysPerWeek == 3) {
                Spacer(Modifier.height(12.dp))
                BentoLabel("RÉPARTITION 3 JOURS", colors.textMuted)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !state.profile.usePplForThreeDays,
                        onClick = { onSetThreeDayPpl(false) },
                        label = { Text("Corps entier ×3") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = state.profile.usePplForThreeDays,
                        onClick = { onSetThreeDayPpl(true) },
                        label = { Text("Push / Pull / Legs") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            BentoLabel("LIEU PAR DÉFAUT & DIFFICULTÉ", colors.textMuted)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WorkoutLocation.entries.forEach { loc ->
                    val sel = location == loc
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (sel) colors.workout else colors.cardElevated)
                            .clickable { onSetLocation(loc) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(loc.label, style = MaterialTheme.typography.titleMedium, color = if (sel) colors.onWorkout else colors.text)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DifficultyLevel.entries.forEach { diff ->
                    val sel = difficulty == diff
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (sel) colors.workout else colors.cardElevated)
                            .clickable { onSetDifficulty(diff) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${diff.label} ×${diff.multiplier}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (sel) colors.onWorkout else colors.text
                        )
                    }
                }
            }
        }

        // Liste de la semaine LUN..DIM (fidèle à la maquette page 7)
        state.weeklyPlan.forEach { item ->
            val isToday = item.dayOfWeekIndex == todayDow
            val bg = if (item.isRestDay) colors.card else colors.workout
            val fg = if (item.isRestDay) colors.textMuted else colors.onWorkout

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(bg)
                    .clickable {
                        if (!item.isRestDay) inspectedDay = item
                    }
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = item.shortDay,
                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = fg.copy(alpha = 0.78f)
                    )
                    Text(
                        text = item.sessionName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = fg
                    )
                }
                if (isToday) {
                    Text(
                        text = "AUJOURD'HUI",
                        style = MaterialTheme.typography.labelSmall,
                        color = fg.copy(alpha = 0.9f)
                    )
                } else if (!item.isRestDay) {
                    Text(
                        text = "${item.exerciseIds.size} EXOS",
                        style = MaterialTheme.typography.labelSmall,
                        color = fg.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }

    inspectedDay?.let { day ->
        AlertDialog(
            onDismissRequest = { inspectedDay = null },
            containerColor = colors.card,
            title = {
                Text("${day.shortDay} · ${day.sessionName} (${location.label})", color = colors.text)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    day.exerciseIds.forEachIndexed { idx, exId ->
                        val def = ExerciseCatalog.exercises[exId]
                        if (def != null) {
                            val tier = state.homeTiers[exId] ?: 0
                            val name = ExerciseCatalog.displayExerciseName(def, location, tier)
                            val sets = ExerciseCatalog.adjustedSets(def.baseSets, difficulty)
                            Text(
                                text = "${idx + 1}. $name — $sets × ${def.baseReps}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.text
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = day
                        inspectedDay = null
                        onLaunchWorkout(target)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.workout)
                ) {
                    Text("Lancer", color = colors.onWorkout)
                }
            },
            dismissButton = {
                TextButton(onClick = { inspectedDay = null }) {
                    Text("Fermer", color = colors.textMuted)
                }
            }
        )
    }
}
