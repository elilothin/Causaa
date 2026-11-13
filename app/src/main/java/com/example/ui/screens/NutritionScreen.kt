package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.HelpOutline
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
import com.example.data.catalog.FoodCatalog
import com.example.data.model.FoodItem
import com.example.ui.components.*
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalCausaSuiColors
import com.example.ui.viewmodel.CausaSuiUiState
import kotlin.math.roundToInt

@Composable
fun NutritionScreen(
    state: CausaSuiUiState,
    onAddWater: (Double) -> Unit,
    onUndoWater: () -> Unit,
    onAddFoodFromCatalog: (FoodItem, Double) -> Unit,
    onAddDirectCalories: (String, Int) -> Unit,
    onDeleteFood: (Long) -> Unit,
) {
    val colors = LocalCausaSuiColors.current
    val targets = state.nutritionTargets
    val gainedKcal = state.todayFoods.sumOf { it.calories }
    val proteinG = state.todayFoods.sumOf { it.proteinG }.roundToInt()
    val fatG = state.todayFoods.sumOf { it.fatG }.roundToInt()
    val carbsG = state.todayFoods.sumOf { it.carbsG }.roundToInt()

    var showFormulaDialog by remember { mutableStateOf(false) }
    var showFoodCatalogModal by remember { mutableStateOf(false) }
    var showDirectKcalModal by remember { mutableStateOf(false) }

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
                text = "Nutrition",
                style = MaterialTheme.typography.displayMedium,
                color = colors.text
            )
            TextButton(onClick = { showFormulaDialog = true }) {
                Icon(Icons.Outlined.HelpOutline, contentDescription = "Pourquoi ces chiffres ?", tint = colors.nutrition, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Pourquoi ?", color = colors.nutrition)
            }
        }

        if (state.rapidWeightLossWarning) {
            BentoCard(backgroundColor = colors.workout.copy(alpha = 0.16f)) {
                Text(
                    text = "Alerte rythme : ton poids baisse de plus de 1 % par semaine depuis 3 semaines. Pense à remonter tes calories.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.workout
                )
            }
        }

        // Carte Moutarde Calories Gagnées / Dépensées (fidèle maquette page 7)
        BentoCard(
            backgroundColor = colors.nutrition,
            cornerRadius = 28.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1.3f)) {
                    BentoLabel("GAGNÉES", colors.onNutrition.copy(alpha = 0.75f))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "$gainedKcal",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 42.sp),
                        color = colors.onNutrition
                    )
                    Text(
                        text = "kcal sur ${targets.targetCalories} estimées",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onNutrition.copy(alpha = 0.8f)
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(0.9f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.onNutrition.copy(alpha = 0.12f))
                        .padding(14.dp)
                ) {
                    Column {
                        BentoLabel("DÉPENSÉES", colors.onNutrition.copy(alpha = 0.75f))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${state.todayBurnedKcal}",
                            style = MaterialTheme.typography.headlineLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = colors.onNutrition
                        )
                        Text("kcal séance", style = MaterialTheme.typography.labelSmall, color = colors.onNutrition.copy(alpha = 0.75f))
                    }
                }
            }
        }

        // Jauges épaisses et arrondies des macros
        BentoCard(backgroundColor = colors.card) {
            MacroBarRow("Protéines", proteinG, targets.proteinG, colors.workout, colors.cardElevated, colors.text) { showFormulaDialog = true }
            Spacer(Modifier.height(14.dp))
            MacroBarRow("Lipides", fatG, targets.fatG, colors.nutrition, colors.cardElevated, colors.text) { showFormulaDialog = true }
            Spacer(Modifier.height(14.dp))
            MacroBarRow("Glucides", carbsG, targets.carbsG, colors.progress, colors.cardElevated, colors.text) { showFormulaDialog = true }
        }

        // Compteur d'eau complet (8 segments + boutons rapides + ma bouteille + annuler)
        BentoCard(backgroundColor = colors.water) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BentoLabel("COMPTEUR D'EAU", colors.onWater.copy(alpha = 0.8f))
                Text(
                    text = "Annuler",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onWater.copy(alpha = 0.85f),
                    modifier = Modifier.clickable(onClick = onUndoWater)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${"%.2f".format(state.todayWaterLiters).replace('.', ',')} / ${"%.1f".format(state.profile.waterGoalLiters).replace('.', ',')} L",
                style = MaterialTheme.typography.headlineLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                color = colors.onWater
            )
            Spacer(Modifier.height(10.dp))
            SegmentedWaterGauge(
                currentLiters = state.todayWaterLiters,
                goalLiters = state.profile.waterGoalLiters,
                filledColor = colors.onWater,
                emptyColor = colors.onWater.copy(alpha = 0.24f)
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WaterAddButton("+0,25 L", colors.onWater, colors.water, Modifier.weight(1f)) { onAddWater(0.25) }
                WaterAddButton("+0,5 L", colors.onWater, colors.water, Modifier.weight(1f)) { onAddWater(0.5) }
                WaterAddButton("+1 L", colors.onWater, colors.water, Modifier.weight(1f)) { onAddWater(1.0) }
                WaterAddButton(
                    "Bouteille",
                    colors.onWater,
                    colors.water,
                    Modifier.weight(1.2f)
                ) { onAddWater(state.profile.bottleVolumeLiters) }
            }
        }

        // Aliments du jour + ajout catalogue (200+ aliments) ou saisie directe kcal
        BentoCard(backgroundColor = colors.card) {
            BentoLabel("ALIMENTS DU JOUR", colors.textMuted)
            Spacer(Modifier.height(10.dp))
            if (state.todayFoods.isEmpty()) {
                Text(
                    text = "Aucun aliment saisi aujourd'hui.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted
                )
            } else {
                state.todayFoods.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, style = MaterialTheme.typography.titleMedium, color = colors.text)
                            val macroText = if (item.isDirectEntry) {
                                "Saisie directe · ${item.calories} kcal"
                            } else {
                                "${item.calories} kcal · P ${item.proteinG.roundToInt()}g · L ${item.fatG.roundToInt()}g · G ${item.carbsG.roundToInt()}g"
                            }
                            Text(macroText, style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
                        }
                        IconButton(onClick = { onDeleteFood(item.id) }) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Supprimer", tint = colors.textMuted)
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { showFoodCatalogModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.nutrition),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).testTag("btn_add_food_catalog")
                ) {
                    Text("+ Aliment", color = colors.onNutrition)
                }
                OutlinedButton(
                    onClick = { showDirectKcalModal = true },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).testTag("btn_add_direct_kcal")
                ) {
                    Text("+ Calories", color = colors.text)
                }
            }
        }

        Text(
            text = "Estimations indicatives, pas un avis médical.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textMuted,
        )
    }

    if (showFormulaDialog) {
        FormulaWhyDialog(targets = targets, onDismiss = { showFormulaDialog = false })
    }

    if (showFoodCatalogModal) {
        FoodSearchDialog(
            onSelectFood = { item, mult ->
                onAddFoodFromCatalog(item, mult)
                showFoodCatalogModal = false
            },
            onDismiss = { showFoodCatalogModal = false }
        )
    }

    if (showDirectKcalModal) {
        DirectCaloriesDialog(
            onConfirm = { label, kcal ->
                onAddDirectCalories(label, kcal)
                showDirectKcalModal = false
            },
            onDismiss = { showDirectKcalModal = false }
        )
    }
}

@Composable
private fun MacroBarRow(
    name: String,
    currentG: Int,
    targetG: Int,
    fillColor: androidx.compose.ui.graphics.Color,
    trackColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    onWhy: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$name  $currentG / $targetG g",
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                color = textColor
            )
            Text(
                text = "pourquoi ?",
                style = MaterialTheme.typography.labelMedium,
                color = fillColor,
                modifier = Modifier.clickable(onClick = onWhy)
            )
        }
        Spacer(Modifier.height(8.dp))
        ThickProgressBar(
            progress = if (targetG > 0) currentG.toFloat() / targetG else 0f,
            trackColor = trackColor,
            fillColor = fillColor,
            height = 12.dp
        )
    }
}

@Composable
private fun WaterAddButton(
    label: String,
    bgColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor.copy(alpha = 0.92f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
private fun FoodSearchDialog(
    onSelectFood: (FoodItem, Double) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalCausaSuiColors.current
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tous") }
    var pendingFood by remember { mutableStateOf<FoodItem?>(null) }
    var multiplierStr by remember { mutableStateOf("1") }

    val categories = remember { listOf("Tous", "Togo & Afrique de l'Ouest", "Protéines", "Féculents", "Laitiers", "Légumes", "Fruits", "Lipides", "Plats") }
    val filtered = remember(query, selectedCategory) {
        FoodCatalog.foods.filter { f ->
            (selectedCategory == "Tous" || f.category == selectedCategory) &&
                (query.isBlank() || f.name.contains(query, ignoreCase = true) || f.category.contains(query, ignoreCase = true))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.card,
        title = {
            Text(
                if (pendingFood == null) "Aliments (${FoodCatalog.foods.size})" else pendingFood!!.name,
                color = colors.text
            )
        },
        text = {
            if (pendingFood == null) {
                Column(modifier = Modifier.heightIn(max = 420.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Rechercher (riz au gras, œufs, poulet...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Tous", "Togo & Afrique de l'Ouest", "Protéines", "Féculents").forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(if (cat.startsWith("Togo")) "Togo" else cat) },
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(filtered, key = { it.id }) { food ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.cardElevated)
                                    .clickable { pendingFood = food }
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(food.name, style = MaterialTheme.typography.titleMedium, color = colors.text)
                                    Text("${food.portionLabel} · P ${food.proteinG.roundToInt()}g · L ${food.fatG.roundToInt()}g · G ${food.carbsG.roundToInt()}g", style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
                                }
                                Text("${food.calories} kcal", style = MaterialTheme.typography.labelLarge, color = colors.nutrition)
                            }
                        }
                    }
                }
            } else {
                val f = pendingFood!!
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Portion de référence : ${f.portionLabel} (${f.calories} kcal)", color = colors.textMuted)
                    OutlinedTextField(
                        value = multiplierStr,
                        onValueChange = { multiplierStr = it.replace(',', '.') },
                        label = { Text("Nombre de portions (ex. 0.5, 1, 1.5)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            if (pendingFood != null) {
                Button(
                    onClick = {
                        val mult = multiplierStr.toDoubleOrNull() ?: 1.0
                        onSelectFood(pendingFood!!, mult)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.nutrition)
                ) {
                    Text("Ajouter", color = colors.onNutrition)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (pendingFood != null) pendingFood = null else onDismiss()
            }) {
                Text(if (pendingFood != null) "Retour" else "Fermer", color = colors.textMuted)
            }
        }
    )
}

@Composable
private fun DirectCaloriesDialog(
    onConfirm: (String, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalCausaSuiColors.current
    var label by remember { mutableStateOf("") }
    var kcalStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.card,
        title = { Text("Saisie directe de calories", color = colors.text) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Ajoute uniquement des calories si l'aliment manque au catalogue (sans macros).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted
                )
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Nom (facultatif)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = kcalStr,
                    onValueChange = { kcalStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Calories (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val kcal = kcalStr.toIntOrNull() ?: 0
                    if (kcal > 0) onConfirm(label, kcal)
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.nutrition)
            ) {
                Text("Ajouter", color = colors.onNutrition)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = colors.textMuted)
            }
        }
    )
}
