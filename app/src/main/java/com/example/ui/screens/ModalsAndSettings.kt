package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.ReminderScheduler
import com.example.data.catalog.NutritionCalculator
import com.example.data.local.UserProfileEntity
import com.example.data.model.*
import com.example.ui.components.AvatarHairColors
import com.example.ui.components.BentoCard
import com.example.ui.components.BentoLabel
import com.example.ui.components.BitmojiAvatarView
import com.example.ui.theme.LocalCausaSuiColors
import com.example.ui.viewmodel.CausaSuiUiState
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    state: CausaSuiUiState,
    onSaveProfile: (UserProfileEntity) -> Unit,
    onToggleTheme: () -> Unit,
    onClose: () -> Unit,
) {
    val colors = LocalCausaSuiColors.current
    val context = LocalContext.current
    var p by remember(state.profile) { mutableStateOf(state.profile) }
    var ageStr by remember(state.profile) { mutableStateOf(state.profile.age.toString()) }
    var heightStr by remember(state.profile) { mutableStateOf(state.profile.heightCm.toString()) }
    var weightStr by remember(state.profile) { mutableStateOf(state.profile.weightKg.toString()) }
    var targetWStr by remember(state.profile) { mutableStateOf(state.profile.targetWeightKg.toString()) }
    var waterGoalStr by remember(state.profile) { mutableStateOf(state.profile.waterGoalLiters.toString()) }
    var bottleStr by remember(state.profile) { mutableStateOf(state.profile.bottleVolumeLiters.toString()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                errorMsg = "Permission de notification refusée : les rappels ne pourront pas s'afficher."
            }
        }
    )

    BackHandler { onClose() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Réglages", style = MaterialTheme.typography.displayMedium, color = colors.text)
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

        // Profil & Objectif
        BentoCard(backgroundColor = colors.card) {
            BentoLabel("PROFIL & MENSURATIONS", colors.textMuted)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = p.pseudo,
                onValueChange = { if (it.length <= 20) p = p.copy(pseudo = it) },
                label = { Text("Pseudo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = ageStr,
                    onValueChange = { ageStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Âge") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = heightStr,
                    onValueChange = { heightStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Taille (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = weightStr,
                    onValueChange = { weightStr = it.replace(',', '.') },
                    label = { Text("Poids (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = targetWStr,
                    onValueChange = { targetWStr = it.replace(',', '.') },
                    label = { Text("Cible (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(10.dp))
            BentoLabel("OBJECTIF", colors.textMuted)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FitnessGoal.entries.forEach { g ->
                    FilterChip(
                        selected = p.goal == g.name,
                        onClick = { p = p.copy(goal = g.name) },
                        label = { Text(g.shortLabel) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Hydratation
        BentoCard(backgroundColor = colors.card) {
            BentoLabel("HYDRATATION", colors.textMuted)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = waterGoalStr,
                    onValueChange = { waterGoalStr = it.replace(',', '.') },
                    label = { Text("Objectif (L/j)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = bottleStr,
                    onValueChange = { bottleStr = it.replace(',', '.') },
                    label = { Text("Ma bouteille (L)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Sons, vibrations, accessibilité & économie batterie
        BentoCard(backgroundColor = colors.card) {
            BentoLabel("SONS, VIBRATIONS & AFFICHAGE", colors.textMuted)
            SettingSwitch("Sons de validation (sans musique)", p.soundsEnabled) { p = p.copy(soundsEnabled = it) }
            SettingSwitch("Vibrations courtes et de fin", p.hapticsEnabled) { p = p.copy(hapticsEnabled = it) }
            SettingSwitch("Réduire les animations", p.reduceMotion) { p = p.copy(reduceMotion = it) }
            SettingSwitch("Mode économie 3D (30 ips)", p.economy30Fps) { p = p.copy(economy30Fps = it) }
            Spacer(Modifier.height(6.dp))
            OutlinedButton(onClick = onToggleTheme, shape = RoundedCornerShape(14.dp)) {
                Text("Basculer thème (${if (colors.isDark) "Sombre → Beige" else "Beige → Sombre"})", color = colors.text)
            }
        }

        // Rappels & permissions Android 12/13
        BentoCard(backgroundColor = colors.card) {
            BentoLabel("RAPPELS HORS-LIGNE", colors.textMuted)
            SettingSwitch("Rappel entraînement (${p.workoutReminderHour}h00)", p.workoutReminderEnabled) { enabled ->
                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                p = p.copy(workoutReminderEnabled = enabled)
            }
            SettingSwitch("Rappel « Bois de l'eau » (toutes les ${p.waterReminderIntervalHours} h)", p.waterReminderEnabled) { enabled ->
                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                p = p.copy(waterReminderEnabled = enabled)
            }
            SettingSwitch("Rappel mensuel de sauvegarde", p.backupReminderMonthly) { p = p.copy(backupReminderMonthly = it) }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !ReminderScheduler.canUseExactAlarms(context)) {
                Spacer(Modifier.height(6.dp))
                TextButton(onClick = {
                    runCatching {
                        context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                    }
                }) {
                    Text("Autoriser les alarmes exactes (Android 12+)", color = colors.workout)
                }
            }
        }

        if (errorMsg != null) {
            BentoCard(backgroundColor = colors.workout.copy(alpha = 0.16f)) {
                Text(errorMsg!!, style = MaterialTheme.typography.bodyMedium, color = colors.workout)
            }
        }

        Button(
            onClick = {
                val age = ageStr.toIntOrNull()
                val h = heightStr.toIntOrNull()
                val w = weightStr.toDoubleOrNull()
                val tw = targetWStr.toDoubleOrNull() ?: w
                val wg = waterGoalStr.toDoubleOrNull() ?: 2.0
                val bv = bottleStr.toDoubleOrNull() ?: 0.75
                val goal = runCatching { FitnessGoal.valueOf(p.goal) }.getOrDefault(FitnessGoal.MAINTIEN)

                when {
                    p.pseudo.trim().length !in 3..20 -> errorMsg = "Pseudo : 3 à 20 caractères."
                    age == null || age !in 16..90 -> errorMsg = "Âge : 16 à 90 ans."
                    h == null || h !in 130..220 -> errorMsg = "Taille : 130 à 220 cm."
                    w == null || w !in 35.0..250.0 -> errorMsg = "Poids : 35 à 250 kg."
                    tw == null || tw !in 35.0..250.0 -> errorMsg = "Poids cible : 35 à 250 kg."
                    age < 18 && goal == FitnessGoal.PERTE_POIDS -> errorMsg = "Moins de 18 ans : aucun déficit calorique proposé."
                    else -> {
                        val safeErr = NutritionCalculator.validateSafeWeightTarget(w, tw, h, 12, goal)
                        if (safeErr != null) {
                            errorMsg = safeErr
                            return@Button
                        }
                        onSaveProfile(
                            p.copy(
                                pseudo = p.pseudo.trim(),
                                age = age,
                                heightCm = h,
                                weightKg = w,
                                targetWeightKg = tw,
                                waterGoalLiters = wg.coerceIn(0.5, 6.0),
                                bottleVolumeLiters = bv.coerceIn(0.1, 2.5),
                            )
                        )
                        onClose()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = colors.workout),
            modifier = Modifier.fillMaxWidth().height(54.dp).testTag("btn_save_settings"),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("Enregistrer les réglages", color = colors.onWorkout)
        }
    }
}

@Composable
private fun SettingSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = LocalCausaSuiColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.text, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun BackupModal(
    onExportJson: suspend () -> String,
    onImportJson: (String) -> Unit,
    onToast: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalCausaSuiColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    runCatching {
                        val json = onExportJson()
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(json.toByteArray(Charsets.UTF_8))
                        }
                    }.onSuccess {
                        onToast("Sauvegarde JSON exportée.")
                        onDismiss()
                    }.onFailure {
                        onToast("Erreur lors de l'export.")
                    }
                }
            }
        }
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    }.onSuccess { content ->
                        if (!content.isNullOrBlank()) {
                            onImportJson(content)
                        } else {
                            onToast("Fichier vide.")
                        }
                    }.onFailure {
                        onToast("Impossible de lire le fichier.")
                    }
                }
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.card,
        title = { Text("Sauvegarde locale (JSON)", color = colors.text) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Tout reste sur ton téléphone. Exporte régulièrement ton fichier JSON pour conserver ton profil, tes séances, ton rang et tes courbes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted
                )
                Button(
                    onClick = { exportLauncher.launch("causa-sui-sauvegarde.json") },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.workout),
                    modifier = Modifier.fillMaxWidth().testTag("btn_export_json"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Exporter la sauvegarde (.json)", color = colors.onWorkout)
                }
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                    modifier = Modifier.fillMaxWidth().testTag("btn_import_json"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Importer une sauvegarde (.json)", color = colors.text)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", color = colors.textMuted)
            }
        }
    )
}

@Composable
fun AvatarBuilderScreen(
    initial: AvatarConfig,
    onSave: (AvatarConfig) -> Unit,
    onClose: () -> Unit,
) {
    val colors = LocalCausaSuiColors.current
    var cfg by remember(initial) { mutableStateOf(initial) }

    BackHandler { onClose() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mon avatar", style = MaterialTheme.typography.displayMedium, color = colors.text)
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

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            BitmojiAvatarView(
                config = cfg,
                backgroundColor = colors.rankGold,
                size = 132.dp
            )
        }

        BentoCard(backgroundColor = colors.card) {
            AvatarOptionRow("Peau", listOf("Orange", "Clair", "Doré", "Ambre", "Ébène", "Profond"), cfg.skinIndex) {
                cfg = cfg.copy(skinIndex = it)
            }
            AvatarOptionRow("Visage", listOf("Ovale", "Carré", "Anguleux"), cfg.faceShapeIndex) {
                cfg = cfg.copy(faceShapeIndex = it)
            }
            AvatarOptionRow("Coiffure", listOf("Rasé", "Court", "Bouclé", "Long", "Chignon", "Fade"), cfg.hairStyleIndex) {
                cfg = cfg.copy(hairStyleIndex = it)
            }
            AvatarOptionRow("Cheveux", List(AvatarHairColors.size) { "Teinte ${it + 1}" }, cfg.hairColorIndex) {
                cfg = cfg.copy(hairColorIndex = it)
            }
            AvatarOptionRow("Yeux", listOf("Déterminé", "Calme", "Vif"), cfg.eyeStyleIndex) {
                cfg = cfg.copy(eyeStyleIndex = it)
            }
            AvatarOptionRow("Expression", listOf("Neutre", "Sourire"), cfg.mouthStyleIndex) {
                cfg = cfg.copy(mouthStyleIndex = it)
            }
            AvatarOptionRow("Barbe", listOf("Aucune", "Bouc", "Courte"), cfg.beardStyleIndex) {
                cfg = cfg.copy(beardStyleIndex = it)
            }
            AvatarOptionRow("Lunettes", listOf("Aucune", "Carrées", "Rondes"), cfg.glassesIndex) {
                cfg = cfg.copy(glassesIndex = it)
            }
            AvatarOptionRow("Boucles", listOf("Aucune", "1 anneau", "2 puces"), cfg.earringIndex) {
                cfg = cfg.copy(earringIndex = it)
            }
        }

        Button(
            onClick = { onSave(cfg) },
            colors = ButtonDefaults.buttonColors(containerColor = colors.rankGold),
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("Enregistrer l'avatar", color = colors.onRankGold)
        }
    }
}

@Composable
private fun AvatarOptionRow(
    title: String,
    labels: List<String>,
    selectedIdx: Int,
    onSelect: (Int) -> Unit,
) {
    val colors = LocalCausaSuiColors.current
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        BentoLabel(title, colors.textMuted)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            labels.take(4).forEachIndexed { idx, lbl ->
                val sel = idx == selectedIdx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (sel) colors.workout else colors.cardElevated)
                        .clickable { onSelect(idx) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        lbl,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (sel) colors.onWorkout else colors.text,
                        maxLines = 1
                    )
                }
            }
        }
        if (labels.size > 4) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                labels.drop(4).forEachIndexed { offset, lbl ->
                    val idx = offset + 4
                    val sel = idx == selectedIdx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (sel) colors.workout else colors.cardElevated)
                            .clickable { onSelect(idx) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            lbl,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (sel) colors.onWorkout else colors.text,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
