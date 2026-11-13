package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.catalog.RankedCalculator
import com.example.data.local.BackupManager
import com.example.ui.components.BentoCard
import com.example.ui.components.BentoLabel
import com.example.ui.components.GraduatedRulerSlider
import com.example.ui.components.ThickProgressBar
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalCausaSuiColors
import com.example.ui.viewmodel.CausaSuiUiState
import kotlin.math.roundToInt

@Composable
fun RankedQuestionnaireScreen(
    state: CausaSuiUiState,
    onSubmit: (Map<String, Float>) -> Unit,
    onClose: () -> Unit,
) {
    val colors = LocalCausaSuiColors.current
    val tests = RankedCalculator.tests
    val initialValues = remember(state.rankedSnapshots) {
        state.rankedSnapshots.lastOrNull()?.let {
            BackupManager.rankedValuesFromJson(it.rawValuesJson)
        } ?: emptyMap()
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    val valuesMap = remember {
        mutableStateMapOf<String, Float>().apply {
            tests.forEach { put(it.id, initialValues[it.id] ?: 0f) }
        }
    }

    BackHandler { onClose() }

    val test = tests[currentIndex]
    val curVal = valuesMap[test.id] ?: 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RANKED · 5 PILIERS",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.rankGold
                    )
                    Text(
                        text = "Étape ${currentIndex + 1} / ${tests.size} · ${test.pillar.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colors.card)
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = "Fermer", tint = colors.text)
                }
            }

            ThickProgressBar(
                progress = (currentIndex + 1).toFloat() / tests.size,
                trackColor = colors.card,
                fillColor = colors.rankGold,
                height = 10.dp
            )

            Spacer(Modifier.height(8.dp))

            BentoCard(backgroundColor = colors.card, cornerRadius = 28.dp) {
                BentoLabel(test.pillar.label, colors.workout)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = test.name,
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 26.sp),
                    color = colors.text
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = test.helper,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted
                )

                Spacer(Modifier.height(26.dp))

                // Valeur en grand + règle graduée (0 = test non compté, aucun bouton valider)
                val formatted = if (curVal <= 0f) {
                    "0 (non compté)"
                } else if (test.step < 1f) {
                    "${"%.1f".format(curVal)} ${test.unit}"
                } else {
                    "${curVal.roundToInt()} ${test.unit}"
                }

                Text(
                    text = formatted,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 36.sp,
                        fontFamily = JetBrainsMonoFontFamily
                    ),
                    color = if (curVal <= 0f) colors.textMuted else colors.workout
                )

                Spacer(Modifier.height(14.dp))

                GraduatedRulerSlider(
                    value = curVal,
                    minVal = test.minVal,
                    maxVal = test.maxVal,
                    step = test.step,
                    accentColor = colors.workout,
                    trackColor = colors.cardElevated,
                    textColor = colors.text,
                    onValueChange = { valuesMap[test.id] = it },
                )

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0 = test non compté", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                    Text(
                        text = "Remettre à 0",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.workout,
                        modifier = Modifier.clickable { valuesMap[test.id] = 0f }
                    )
                }
            }
        }

        // Boutons Précédent et Suivant uniquement (pas de bouton de validation intermédiaire)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentIndex > 0) {
                OutlinedButton(
                    onClick = { currentIndex-- },
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Précédent", color = colors.text)
                }
            }
            Button(
                onClick = {
                    if (currentIndex < tests.lastIndex) {
                        currentIndex++
                    } else {
                        onSubmit(valuesMap.toMap())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.rankGold),
                modifier = Modifier.weight(1.2f).height(54.dp).testTag("btn_ranked_next"),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = if (currentIndex == tests.lastIndex) "Calculer mon rang" else "Suivant",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onRankGold
                )
            }
        }
    }
}
