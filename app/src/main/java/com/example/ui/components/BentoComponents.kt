package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayStatus
import com.example.data.model.NutritionTargets
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalCausaSuiColors
import kotlin.math.roundToInt

@Composable
fun TopHeaderBar(
    subtitle: String,
    onOpenSettings: () -> Unit,
    onOpenBackup: () -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalCausaSuiColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "CAUSA SUI",
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 2.sp,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = colors.text,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeaderRoundButton(
                icon = Icons.Outlined.Settings,
                contentDescription = "Réglages",
                testTag = "btn_header_settings",
                onClick = onOpenSettings,
            )
            HeaderRoundButton(
                icon = Icons.Outlined.SaveAlt,
                contentDescription = "Sauvegarde export et import",
                testTag = "btn_header_backup",
                onClick = onOpenBackup,
            )
            HeaderRoundButton(
                icon = if (colors.isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                contentDescription = "Changer le thème beige ou sombre",
                testTag = "btn_header_theme",
                onClick = onToggleTheme,
            )
        }
    }
}

@Composable
private fun HeaderRoundButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit,
) {
    val colors = LocalCausaSuiColors.current
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(colors.card)
            .testTag(testTag)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.text,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun BentoCard(
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val baseMod = modifier
        .clip(shape)
        .background(backgroundColor)
    val clickableMod = if (onClick != null) {
        baseMod.clickable(onClick = onClick)
    } else {
        baseMod
    }
    Column(
        modifier = clickableMod.padding(18.dp),
        content = content,
    )
}

@Composable
fun BentoLabel(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = JetBrainsMonoFontFamily,
            letterSpacing = 1.4.sp,
            fontWeight = FontWeight.Bold,
        ),
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@Composable
fun ThickProgressBar(
    progress: Float,
    trackColor: Color,
    fillColor: Color,
    height: Dp = 12.dp,
    modifier: Modifier = Modifier,
) {
    val clamped = progress.coerceIn(0f, 1f)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val radius = size.height / 2f
        drawRoundRect(
            color = trackColor,
            size = size,
            cornerRadius = CornerRadius(radius, radius),
        )
        if (clamped > 0.01f) {
            drawRoundRect(
                color = fillColor,
                size = Size((size.width * clamped).coerceAtLeast(size.height), size.height),
                cornerRadius = CornerRadius(radius, radius),
            )
        }
    }
}

@Composable
fun SegmentedWaterGauge(
    currentLiters: Double,
    goalLiters: Double,
    filledColor: Color,
    emptyColor: Color,
    modifier: Modifier = Modifier,
) {
    val segments = 8
    val step = (goalLiters / segments).coerceAtLeast(0.1)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until segments) {
            val threshold = (i + 1) * step - (step * 0.15)
            val filled = currentLiters >= threshold
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (filled) filledColor else emptyColor)
            )
        }
    }
}

@Composable
fun DotsCalendar(
    days: List<Pair<Int, DayStatus>>,
    workoutColor: Color,
    restColor: Color,
    missedColor: Color,
    upcomingColor: Color,
    todayRingColor: Color,
    onDayClick: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val rows = days.chunked(7)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        rows.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (col in 0 until 7) {
                    val item = week.getOrNull(col)
                    if (item == null) {
                        Spacer(modifier = Modifier.size(32.dp))
                    } else {
                        val (dayNum, status) = item
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .then(
                                    if (onDayClick != null) Modifier.clickable { onDayClick(dayNum) } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(20.dp)) {
                                val c = Offset(size.width / 2f, size.height / 2f)
                                when (status) {
                                    DayStatus.WORKOUT_DONE -> {
                                        drawCircle(color = workoutColor, radius = size.minDimension * 0.42f)
                                    }
                                    DayStatus.REST_VALIDATED -> {
                                        drawCircle(color = restColor, radius = size.minDimension * 0.22f)
                                    }
                                    DayStatus.MISSED -> {
                                        // Losange (diamond) so shape doubles color
                                        val r = size.minDimension * 0.36f
                                        val p = Path().apply {
                                            moveTo(c.x, c.y - r)
                                            lineTo(c.x + r, c.y)
                                            lineTo(c.x, c.y + r)
                                            lineTo(c.x - r, c.y)
                                            close()
                                        }
                                        drawPath(p, color = missedColor)
                                    }
                                    DayStatus.TODAY_PENDING -> {
                                        drawCircle(
                                            color = todayRingColor,
                                            radius = size.minDimension * 0.42f,
                                            style = Stroke(width = 3.dp.toPx())
                                        )
                                    }
                                    DayStatus.UPCOMING -> {
                                        drawCircle(
                                            color = upcomingColor,
                                            radius = size.minDimension * 0.34f,
                                            style = Stroke(width = 1.6.dp.toPx())
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GraduatedRulerSlider(
    value: Float,
    minVal: Float,
    maxVal: Float,
    step: Float,
    accentColor: Color,
    trackColor: Color,
    textColor: Color,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val norm = ((value - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .pointerInput(minVal, maxVal, step) {
                    detectTapGestures { offset ->
                        val frac = (offset.x / size.width).coerceIn(0f, 1f)
                        val raw = minVal + frac * (maxVal - minVal)
                        val snapped = ((raw / step).roundToInt() * step).coerceIn(minVal, maxVal)
                        onValueChange(snapped)
                    }
                }
                .pointerInput(minVal, maxVal, step) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        val frac = (change.position.x / size.width).coerceIn(0f, 1f)
                        val raw = minVal + frac * (maxVal - minVal)
                        val snapped = ((raw / step).roundToInt() * step).coerceIn(minVal, maxVal)
                        onValueChange(snapped)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cy = h * 0.52f

                // Track
                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(0f, cy - 6.dp.toPx()),
                    size = Size(w, 12.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                // Active fill
                if (norm > 0f) {
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(0f, cy - 6.dp.toPx()),
                        size = Size(w * norm, 12.dp.toPx()),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }

                // Graduated ruler ticks below track
                val tickCount = 20
                for (i in 0..tickCount) {
                    val tx = (w / tickCount) * i
                    val isMajor = i % 5 == 0
                    val tickH = if (isMajor) 14.dp.toPx() else 7.dp.toPx()
                    drawLine(
                        color = textColor.copy(alpha = if (isMajor) 0.55f else 0.25f),
                        start = Offset(tx, cy + 12.dp.toPx()),
                        end = Offset(tx, cy + 12.dp.toPx() + tickH),
                        strokeWidth = if (isMajor) 2.dp.toPx() else 1.2.dp.toPx()
                    )
                }

                // Thumb handle
                val thumbX = w * norm
                drawCircle(
                    color = accentColor,
                    radius = 14.dp.toPx(),
                    center = Offset(thumbX, cy)
                )
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(thumbX, cy)
                )
            }
        }
    }
}

@Composable
fun FormulaWhyDialog(
    targets: NutritionTargets,
    onDismiss: () -> Unit,
) {
    val colors = LocalCausaSuiColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.card,
        titleContentColor = colors.text,
        textContentColor = colors.text,
        title = {
            Text(
                text = "Calcul des besoins (pourquoi ?)",
                style = MaterialTheme.typography.headlineMedium,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(targets.bmrFormulaText, style = MaterialTheme.typography.bodyMedium)
                Text(targets.tdeeFormulaText, style = MaterialTheme.typography.bodyMedium)
                Text(targets.caloriesFormulaText, style = MaterialTheme.typography.bodyMedium)
                Text(targets.proteinFormulaText, style = MaterialTheme.typography.bodyMedium)
                Text(targets.fatFormulaText, style = MaterialTheme.typography.bodyMedium)
                Text(targets.carbsFormulaText, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Estimations indicatives, pas un avis médical.",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textMuted,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Compris", color = colors.workout)
            }
        }
    )
}
