package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.AvatarConfig
import com.example.ui.theme.CharacterSkinOptions

val AvatarHairColors = listOf(
    Color(0xFF1A1A1D),
    Color(0xFF3E2723),
    Color(0xFF795548),
    Color(0xFFD6A52E),
    Color(0xFFB83228),
    Color(0xFFD0CEC8),
)

@Composable
fun BitmojiAvatarView(
    config: AvatarConfig,
    backgroundColor: Color,
    size: Dp = 64.dp,
    modifier: Modifier = Modifier,
) {
    val skin = CharacterSkinOptions.getOrElse(config.skinIndex) { CharacterSkinOptions[1] }
    val hair = AvatarHairColors.getOrElse(config.hairColorIndex) { AvatarHairColors[0] }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h * 0.50f

            // Shoulders
            drawOval(
                color = Color(0xFF1E1E22),
                topLeft = Offset(cx - w * 0.34f, h * 0.76f),
                size = Size(w * 0.68f, h * 0.40f)
            )
            // Neck
            drawRect(
                color = skin,
                topLeft = Offset(cx - w * 0.10f, h * 0.66f),
                size = Size(w * 0.20f, h * 0.16f)
            )

            // Long hair back layer if style == 3 (tresses/long)
            if (config.hairStyleIndex == 3) {
                drawRoundRect(
                    color = hair,
                    topLeft = Offset(cx - w * 0.29f, cy - h * 0.24f),
                    size = Size(w * 0.58f, h * 0.60f),
                    cornerRadius = CornerRadius(w * 0.22f, w * 0.22f)
                )
            }

            // Ears
            drawCircle(color = skin, radius = w * 0.065f, center = Offset(cx - w * 0.24f, cy + h * 0.02f))
            drawCircle(color = skin, radius = w * 0.065f, center = Offset(cx + w * 0.24f, cy + h * 0.02f))

            // Earrings
            if (config.earringIndex == 1 || config.earringIndex == 2) {
                drawCircle(
                    color = Color(0xFFFFC61A),
                    radius = w * 0.028f,
                    center = Offset(cx - w * 0.25f, cy + h * 0.09f),
                    style = Stroke(width = w * 0.015f)
                )
            }
            if (config.earringIndex == 2) {
                drawCircle(
                    color = Color(0xFFFFC61A),
                    radius = w * 0.028f,
                    center = Offset(cx + w * 0.25f, cy + h * 0.09f),
                    style = Stroke(width = w * 0.015f)
                )
            }

            // Face shape
            val faceW = when (config.faceShapeIndex) {
                1 -> w * 0.48f
                2 -> w * 0.43f
                else -> w * 0.46f
            }
            val cornerF = when (config.faceShapeIndex) {
                1 -> w * 0.14f
                2 -> w * 0.18f
                else -> w * 0.23f
            }
            drawRoundRect(
                color = skin,
                topLeft = Offset(cx - faceW / 2f, cy - h * 0.25f),
                size = Size(faceW, h * 0.50f),
                cornerRadius = CornerRadius(cornerF, cornerF)
            )

            // Hair top styles (0 rasé, 1 court, 2 afro/bouclé, 3 long, 4 chignon, 5 fade)
            when (config.hairStyleIndex) {
                0 -> {} // Rasé
                1 -> {
                    drawArc(
                        color = hair,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(cx - faceW * 0.54f, cy - h * 0.30f),
                        size = Size(faceW * 1.08f, h * 0.26f)
                    )
                }
                2 -> {
                    drawCircle(color = hair, radius = w * 0.28f, center = Offset(cx, cy - h * 0.20f))
                    drawCircle(color = hair, radius = w * 0.16f, center = Offset(cx - w * 0.18f, cy - h * 0.14f))
                    drawCircle(color = hair, radius = w * 0.16f, center = Offset(cx + w * 0.18f, cy - h * 0.14f))
                    // Redraw face over back afro
                    drawRoundRect(
                        color = skin,
                        topLeft = Offset(cx - faceW / 2f, cy - h * 0.20f),
                        size = Size(faceW, h * 0.45f),
                        cornerRadius = CornerRadius(cornerF, cornerF)
                    )
                }
                3 -> {
                    drawArc(
                        color = hair,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(cx - faceW * 0.56f, cy - h * 0.29f),
                        size = Size(faceW * 1.12f, h * 0.24f)
                    )
                }
                4 -> {
                    drawCircle(color = hair, radius = w * 0.11f, center = Offset(cx, cy - h * 0.34f))
                    drawArc(
                        color = hair,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(cx - faceW * 0.52f, cy - h * 0.28f),
                        size = Size(faceW * 1.04f, h * 0.20f)
                    )
                }
                5 -> {
                    drawRoundRect(
                        color = hair,
                        topLeft = Offset(cx - faceW * 0.42f, cy - h * 0.31f),
                        size = Size(faceW * 0.84f, h * 0.14f),
                        cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
                    )
                }
            }

            // Brows
            val browY = cy - h * 0.05f
            val browStroke = w * 0.025f
            when (config.browStyleIndex) {
                1 -> {
                    drawLine(hair, Offset(cx - w * 0.15f, browY), Offset(cx - w * 0.05f, browY + h * 0.01f), strokeWidth = browStroke * 1.4f, cap = StrokeCap.Round)
                    drawLine(hair, Offset(cx + w * 0.05f, browY + h * 0.01f), Offset(cx + w * 0.15f, browY), strokeWidth = browStroke * 1.4f, cap = StrokeCap.Round)
                }
                else -> {
                    drawLine(hair, Offset(cx - w * 0.15f, browY), Offset(cx - w * 0.05f, browY), strokeWidth = browStroke, cap = StrokeCap.Round)
                    drawLine(hair, Offset(cx + w * 0.05f, browY), Offset(cx + w * 0.15f, browY), strokeWidth = browStroke, cap = StrokeCap.Round)
                }
            }

            // Eyes
            val eyeY = cy + h * 0.01f
            val eyeR = when (config.eyeStyleIndex) {
                1 -> w * 0.025f
                2 -> w * 0.036f
                else -> w * 0.030f
            }
            drawCircle(color = Color(0xFF1E1E22), radius = eyeR, center = Offset(cx - w * 0.10f, eyeY))
            drawCircle(color = Color(0xFF1E1E22), radius = eyeR, center = Offset(cx + w * 0.10f, eyeY))

            // Glasses
            if (config.glassesIndex == 1) {
                val gStroke = w * 0.018f
                drawRect(Color(0xFF1E1E22), Offset(cx - w * 0.17f, eyeY - h * 0.04f), Size(w * 0.13f, h * 0.08f), style = Stroke(gStroke))
                drawRect(Color(0xFF1E1E22), Offset(cx + w * 0.04f, eyeY - h * 0.04f), Size(w * 0.13f, h * 0.08f), style = Stroke(gStroke))
                drawLine(Color(0xFF1E1E22), Offset(cx - w * 0.04f, eyeY), Offset(cx + w * 0.04f, eyeY), strokeWidth = gStroke)
            } else if (config.glassesIndex == 2) {
                val gStroke = w * 0.018f
                drawCircle(Color(0xFF1E1E22), w * 0.068f, Offset(cx - w * 0.10f, eyeY), style = Stroke(gStroke))
                drawCircle(Color(0xFF1E1E22), w * 0.068f, Offset(cx + w * 0.10f, eyeY), style = Stroke(gStroke))
                drawLine(Color(0xFF1E1E22), Offset(cx - w * 0.03f, eyeY), Offset(cx + w * 0.03f, eyeY), strokeWidth = gStroke)
            }

            // Beard (0 aucune, 1 bouc, 2 barbe courte)
            if (config.beardStyleIndex == 1) {
                drawArc(
                    color = hair,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(cx - w * 0.10f, cy + h * 0.10f),
                    size = Size(w * 0.20f, h * 0.14f)
                )
            } else if (config.beardStyleIndex == 2) {
                drawArc(
                    color = hair,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(cx - faceW / 2f, cy + h * 0.02f),
                    size = Size(faceW, h * 0.23f)
                )
            }

            // Mouth
            val mouthY = cy + h * 0.13f
            when (config.mouthStyleIndex) {
                1 -> {
                    val p = Path().apply {
                        moveTo(cx - w * 0.08f, mouthY)
                        quadraticTo(cx, mouthY + h * 0.05f, cx + w * 0.08f, mouthY)
                    }
                    drawPath(p, color = Color(0xFF1E1E22), style = Stroke(width = w * 0.022f, cap = StrokeCap.Round))
                }
                else -> {
                    drawLine(
                        color = Color(0xFF1E1E22),
                        start = Offset(cx - w * 0.07f, mouthY),
                        end = Offset(cx + w * 0.07f, mouthY),
                        strokeWidth = w * 0.022f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
