package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PillarScore
import com.example.data.model.RankTier
import com.example.ui.theme.LocalCausaSuiColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WeightLineChart(
    points: List<Pair<String, Double>>,
    targetWeightKg: Double,
    lineColor: Color,
    gridColor: Color,
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) {
        EmptyChartHint("Aucune pesée enregistrée", lineColor, modifier)
        return
    }
    Canvas(modifier = modifier.fillMaxWidth().height(110.dp)) {
        val values = points.map { it.second } + targetWeightKg
        val minVal = (values.minOrNull() ?: 50.0) - 1.5
        val maxVal = (values.maxOrNull() ?: 80.0) + 1.5
        val range = (maxVal - minVal).coerceAtLeast(1.0)

        val padH = 10.dp.toPx()
        val padV = 12.dp.toPx()
        val plotW = size.width - padH * 2
        val plotH = size.height - padV * 2

        // Target dashed line
        val targetY = padV + plotH * (1f - ((targetWeightKg - minVal) / range).toFloat())
        drawLine(
            color = gridColor,
            start = Offset(padH, targetY),
            end = Offset(size.width - padH, targetY),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
        )

        val coords = points.mapIndexed { idx, (_, w) ->
            val x = if (points.size == 1) padH + plotW / 2f else padH + plotW * (idx.toFloat() / (points.size - 1))
            val y = padV + plotH * (1f - ((w - minVal) / range).toFloat())
            Offset(x, y)
        }

        if (coords.size > 1) {
            val path = Path().apply {
                moveTo(coords.first().x, coords.first().y)
                for (i in 1 until coords.size) lineTo(coords[i].x, coords[i].y)
            }
            drawPath(path, color = lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        }
        coords.forEach { pt ->
            drawCircle(color = lineColor, radius = 4.5.dp.toPx(), center = pt)
        }
    }
}

@Composable
fun WaterBarChart(
    dailyLiters: List<Pair<String, Double>>,
    goalLiters: Double,
    barColor: Color,
    goalLineColor: Color,
    modifier: Modifier = Modifier,
) {
    if (dailyLiters.isEmpty()) {
        EmptyChartHint("Aucune donnée d'eau", barColor, modifier)
        return
    }
    Canvas(modifier = modifier.fillMaxWidth().height(95.dp)) {
        val maxV = maxOf(goalLiters * 1.15, dailyLiters.maxOf { it.second }, 1.0)
        val n = dailyLiters.size.coerceAtLeast(1)
        val slotW = size.width / n
        val barW = (slotW * 0.56f).coerceIn(6.dp.toPx(), 18.dp.toPx())

        val goalY = size.height * (1f - (goalLiters / maxV).toFloat())
        drawLine(
            color = goalLineColor,
            start = Offset(0f, goalY),
            end = Offset(size.width, goalY),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
        )

        dailyLiters.forEachIndexed { idx, (_, liters) ->
            val barH = (size.height * (liters / maxV).toFloat()).coerceAtLeast(4.dp.toPx())
            val left = idx * slotW + (slotW - barW) / 2f
            drawRoundRect(
                color = if (liters >= goalLiters) barColor else barColor.copy(alpha = 0.55f),
                topLeft = Offset(left, size.height - barH),
                size = Size(barW, barH),
                cornerRadius = CornerRadius(barW / 2f, barW / 2f)
            )
        }
    }
}

@Composable
fun SessionsLineChart(
    counts: List<Int>,
    lineColor: Color,
    modifier: Modifier = Modifier,
) {
    if (counts.isEmpty()) {
        EmptyChartHint("Aucune séance sur la période", lineColor, modifier)
        return
    }
    Canvas(modifier = modifier.fillMaxWidth().height(85.dp)) {
        val maxC = maxOf(counts.maxOrNull() ?: 1, 1)
        val pad = 8.dp.toPx()
        val w = size.width - pad * 2
        val h = size.height - pad * 2

        val pts = counts.mapIndexed { idx, c ->
            val x = if (counts.size == 1) pad + w / 2f else pad + w * (idx.toFloat() / (counts.size - 1))
            val y = pad + h * (1f - (c.toFloat() / maxC))
            Offset(x, y)
        }
        if (pts.size > 1) {
            val path = Path().apply {
                moveTo(pts.first().x, pts.first().y)
                for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y)
            }
            drawPath(path, color = lineColor, style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round))
        }
        pts.forEach { drawCircle(lineColor, radius = 3.5.dp.toPx(), center = it) }
    }
}

@Composable
fun RankStepChart(
    ranks: List<RankTier>,
    lineColor: Color,
    modifier: Modifier = Modifier,
) {
    if (ranks.isEmpty()) {
        EmptyChartHint("Passe le test Ranked pour tracer ton rang", lineColor, modifier)
        return
    }
    Canvas(modifier = modifier.fillMaxWidth().height(90.dp)) {
        val pad = 10.dp.toPx()
        val w = size.width - pad * 2
        val h = size.height - pad * 2
        val maxLevel = 9f

        val pts = ranks.mapIndexed { idx, r ->
            val x = if (ranks.size == 1) pad + w / 2f else pad + w * (idx.toFloat() / (ranks.size - 1))
            val y = pad + h * (1f - (r.levelIndex / maxLevel))
            Offset(x, y)
        }
        if (pts.size > 1) {
            val path = Path().apply {
                moveTo(pts.first().x, pts.first().y)
                for (i in 1 until pts.size) {
                    // Step curve (courbe en marches)
                    lineTo(pts[i].x, pts[i - 1].y)
                    lineTo(pts[i].x, pts[i].y)
                }
            }
            drawPath(path, color = lineColor, style = Stroke(width = 3.dp.toPx()))
        }
        pts.forEachIndexed { idx, pt ->
            drawCircle(color = ranks[idx].color, radius = 5.dp.toPx(), center = pt)
        }
    }
}

@Composable
fun FivePillarRadarChart(
    pillars: List<PillarScore>,
    accentColor: Color,
    gridColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(165.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = size.minDimension * 0.46f
            val axes = 5

            // Concentric pentagons (25%, 50%, 75%, 100%)
            for (ring in 1..4) {
                val r = radius * (ring / 4f)
                val ringPath = Path()
                for (i in 0 until axes) {
                    val angle = -PI / 2.0 + (2.0 * PI * i / axes)
                    val x = cx + (r * cos(angle)).toFloat()
                    val y = cy + (r * sin(angle)).toFloat()
                    if (i == 0) ringPath.moveTo(x, y) else ringPath.lineTo(x, y)
                }
                ringPath.close()
                drawPath(ringPath, color = gridColor, style = Stroke(width = 1.2.dp.toPx()))
            }

            // Spokes
            for (i in 0 until axes) {
                val angle = -PI / 2.0 + (2.0 * PI * i / axes)
                val x = cx + (radius * cos(angle)).toFloat()
                val y = cy + (radius * sin(angle)).toFloat()
                drawLine(color = gridColor, start = Offset(cx, cy), end = Offset(x, y), strokeWidth = 1.dp.toPx())
            }

            // Data polygon
            val safePillars = if (pillars.size == 5) pillars else List(5) { null }
            val dataPath = Path()
            val points = mutableListOf<Pair<Offset, Boolean>>()
            for (i in 0 until axes) {
                val p = safePillars[i]
                val score = ((p?.score ?: 0.0) / 100.0).toFloat().coerceIn(0f, 1f)
                val complete = (p?.countedTests ?: 0) > 0
                val r = radius * maxOf(score, 0.04f)
                val angle = -PI / 2.0 + (2.0 * PI * i / axes)
                val pt = Offset(cx + (r * cos(angle)).toFloat(), cy + (r * sin(angle)).toFloat())
                points.add(pt to complete)
                if (i == 0) dataPath.moveTo(pt.x, pt.y) else dataPath.lineTo(pt.x, pt.y)
            }
            dataPath.close()
            drawPath(dataPath, color = accentColor.copy(alpha = 0.28f))
            drawPath(dataPath, color = accentColor, style = Stroke(width = 2.5.dp.toPx()))

            points.forEach { (pt, complete) ->
                if (complete) {
                    drawCircle(color = accentColor, radius = 4.dp.toPx(), center = pt)
                } else {
                    drawCircle(
                        color = gridColor,
                        radius = 4.dp.toPx(),
                        center = pt,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }
        }

        // Clean axis labels around pentagon
        val shortLabels = listOf("MAÎTRISE", "FORCE", "EXPLOS.", "VITESSE", "ENDURANCE")
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(shortLabels[0], style = MaterialTheme.typography.labelSmall, color = labelColor)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(shortLabels[4], style = MaterialTheme.typography.labelSmall, color = labelColor)
                Text(shortLabels[1], style = MaterialTheme.typography.labelSmall, color = labelColor)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(shortLabels[3], style = MaterialTheme.typography.labelSmall, color = labelColor)
                Text(shortLabels[2], style = MaterialTheme.typography.labelSmall, color = labelColor)
            }
        }
    }
}

@Composable
private fun EmptyChartHint(
    text: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(78.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = tint.copy(alpha = 0.8f),
        )
    }
}
