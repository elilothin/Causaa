package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.catalog.PunchlinesCatalog
import com.example.data.local.DateUtilsCompat
import com.example.data.model.AnimationKind
import com.example.data.model.BadgeIconType
import com.example.data.model.Morphotype
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CausaSuiUiState

@Composable
fun MeScreen(
    state: CausaSuiUiState,
    reduceMotion: Boolean,
    onOpenRankedQuestionnaire: () -> Unit,
    onOpenAvatarBuilder: () -> Unit,
    onUpdateCharacterColors: (skinIdx: Int, outfitIdx: Int, shoeIdx: Int) -> Unit,
    onToggleTheme: () -> Unit,
) {
    val colors = LocalCausaSuiColors.current
    val rank = state.latestRank
    val rankTextColor = if (rank.code in listOf("Z+", "C", "B")) Color(0xFF1E1E22) else Color(0xFFF7F1E3)
    val lastRankDate = state.rankedSnapshots.lastOrNull()?.dateIso?.let { DateUtilsCompat.formatShortDisplay(it) }
    val morphLabel = runCatching { Morphotype.valueOf(state.profile.morphotype).label }.getOrDefault("")
    var cameraAngle by remember { mutableStateOf(CameraViewAngle.FACE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Carte Badge Ranked (fidèle maquette page 2 & 8)
        BentoCard(
            backgroundColor = rank.color,
            cornerRadius = 28.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    BitmojiAvatarView(
                        config = state.avatarConfig,
                        backgroundColor = Color.Black.copy(alpha = 0.16f),
                        size = 64.dp,
                        modifier = Modifier
                            .testTag("btn_edit_avatar")
                            .clickable(onClick = onOpenAvatarBuilder)
                    )
                    Column {
                        Text(
                            text = state.profile.pseudo.uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = rankTextColor
                        )
                        Text(
                            text = "${state.profile.age} ans · ${"%.1f".format(state.profile.weightKg)} kg · ${state.profile.heightCm} cm",
                            style = MaterialTheme.typography.bodyMedium,
                            color = rankTextColor.copy(alpha = 0.85f)
                        )
                        if (morphLabel.isNotBlank()) {
                            Text(
                                text = "$morphLabel · Toucher l'avatar",
                                style = MaterialTheme.typography.labelSmall,
                                color = rankTextColor.copy(alpha = 0.72f)
                            )
                        }
                    }
                }
                Text(
                    text = rank.code,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 54.sp),
                    color = rankTextColor
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (lastRankDate != null) "Test refait le $lastRankDate" else "Aucun test Ranked enregistré",
                    style = MaterialTheme.typography.labelMedium,
                    color = rankTextColor.copy(alpha = 0.82f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.20f))
                        .testTag("btn_start_ranked")
                        .clickable(onClick = onOpenRankedQuestionnaire)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (lastRankDate != null) "REFAIRE LE TEST" else "PASSER LE TEST",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = rankTextColor
                    )
                }
            }
        }

        // Radar des 5 piliers + Personnage 3D adaptatif côte à côte (fidèle maquette page 2 & 8)
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BentoCard(
                backgroundColor = colors.card,
                modifier = Modifier.weight(1.1f).fillMaxHeight()
            ) {
                BentoLabel("RADAR 5 PILIERS", colors.textMuted)
                FivePillarRadarChart(
                    pillars = state.latestPillarScores,
                    accentColor = colors.workout,
                    gridColor = colors.textMuted.copy(alpha = 0.25f),
                    labelColor = colors.textMuted,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            BentoCard(
                backgroundColor = colors.workout,
                modifier = Modifier.weight(0.95f).fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BentoLabel("3D", colors.onWorkout.copy(alpha = 0.8f))
                    Text(
                        text = cameraAngle.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onWorkout,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.Black.copy(alpha = 0.22f))
                            .clickable {
                                val next = (cameraAngle.ordinal + 1) % CameraViewAngle.entries.size
                                cameraAngle = CameraViewAngle.entries[next]
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Character3DView(
                        params = state.characterParams,
                        animation = AnimationKind.IDLE_BREATHE,
                        cameraAngle = cameraAngle,
                        reduceMotion = reduceMotion,
                        economy30Fps = state.profile.economy30Fps,
                        interactiveOrbit = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Text(
                    text = "Stade ${state.characterParams.muscleStage}/5",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onWorkout.copy(alpha = 0.85f)
                )
            }
        }

        // Personnalisation couleurs 3D (peau, tenue, accent chaussures)
        BentoCard(backgroundColor = colors.card) {
            BentoLabel("PERSONNAGE 3D · COULEURS", colors.textMuted)
            Spacer(Modifier.height(10.dp))
            ColorPaletteRow("Peau", CharacterSkinOptions, state.profile.skinIndex) { idx ->
                onUpdateCharacterColors(idx, state.profile.outfitIndex, state.profile.shoeAccentIndex)
            }
            Spacer(Modifier.height(8.dp))
            ColorPaletteRow("Tenue", CharacterOutfitOptions, state.profile.outfitIndex) { idx ->
                onUpdateCharacterColors(state.profile.skinIndex, idx, state.profile.shoeAccentIndex)
            }
            Spacer(Modifier.height(8.dp))
            ColorPaletteRow("Baskets", CharacterShoeAccentOptions, state.profile.shoeAccentIndex) { idx ->
                onUpdateCharacterColors(state.profile.skinIndex, state.profile.outfitIndex, idx)
            }
        }

        // XP & Niveau + Thème rapide
        BentoCard(backgroundColor = colors.card) {
            val xp = state.profile.xp
            val level = (xp / 250) + 1
            val progressInLevel = (xp % 250) / 250f
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    BentoLabel("EXPÉRIENCE (DISTINCTE DU RANKED)", colors.textMuted)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Niveau $level · $xp XP",
                        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = colors.text
                    )
                }
                Text(
                    text = "Thème : ${if (colors.isDark) "Sombre" else "Beige"}",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.workout,
                    modifier = Modifier.clickable(onClick = onToggleTheme)
                )
            }
            Spacer(Modifier.height(10.dp))
            ThickProgressBar(
                progress = progressInLevel,
                trackColor = colors.cardElevated,
                fillColor = colors.rankGold
            )
        }

        // Badges de réussite (poids dorés, flammes, gouttes d'eau, trophées)
        BentoCard(backgroundColor = colors.card) {
            BentoLabel("BADGES DE RÉUSSITE (${state.unlockedBadgeIds.size}/${PunchlinesCatalog.badges.size})", colors.textMuted)
            Spacer(Modifier.height(10.dp))
            PunchlinesCatalog.badges.chunked(2).forEach { rowBadges ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowBadges.forEach { badge ->
                        val unlocked = state.unlockedBadgeIds.contains(badge.id)
                        val icon = when (badge.iconType) {
                            BadgeIconType.WEIGHT -> Icons.Outlined.FitnessCenter
                            BadgeIconType.FLAME -> Icons.Outlined.LocalFireDepartment
                            BadgeIconType.WATER -> Icons.Outlined.WaterDrop
                            BadgeIconType.TROPHY -> Icons.Outlined.EmojiEvents
                        }
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (unlocked) colors.rankGold.copy(alpha = 0.18f) else colors.cardElevated)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = badge.title,
                                tint = if (unlocked) colors.rankGold else colors.textMuted.copy(alpha = 0.5f),
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = badge.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (unlocked) colors.text else colors.textMuted
                                )
                                Text(
                                    text = badge.description,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                    color = colors.textMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorPaletteRow(
    label: String,
    options: List<Color>,
    selectedIdx: Int,
    onSelect: (Int) -> Unit,
) {
    val colors = LocalCausaSuiColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.text)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEachIndexed { idx, col ->
                val sel = idx == selectedIdx
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(col)
                        .then(
                            if (sel) Modifier.border(2.5.dp, colors.text, CircleShape) else Modifier
                        )
                        .clickable { onSelect(idx) }
                )
            }
        }
    }
}
