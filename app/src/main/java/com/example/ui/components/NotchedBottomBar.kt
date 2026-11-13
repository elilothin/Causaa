package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalCausaSuiColors
import com.example.ui.viewmodel.MainTab

@Composable
fun NotchedBottomBar(
    activeTab: MainTab,
    reduceMotion: Boolean,
    onSelectTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalCausaSuiColors.current
    val tabs = MainTab.entries
    val targetIdx = tabs.indexOf(activeTab).toFloat()

    val animatedIndex by animateFloatAsState(
        targetValue = targetIdx,
        animationSpec = if (reduceMotion) {
            spring(stiffness = Spring.StiffnessHigh, dampingRatio = Spring.DampingRatioNoBouncy)
        } else {
            spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.78f)
        },
        label = "nav_pill_slide"
    )

    val activeColor = when (activeTab) {
        MainTab.ACCUEIL, MainTab.PROGRAMME -> colors.workout
        MainTab.NUTRITION -> colors.nutrition
        MainTab.PROGRES -> colors.progress
        MainTab.MOI -> colors.rankGold
    }

    val activeIconTint = when (activeTab) {
        MainTab.NUTRITION -> colors.onNutrition
        MainTab.MOI -> colors.onRankGold
        else -> Color(0xFFF7F1E3)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .height(86.dp)
        ) {
            val totalWidth = maxWidth
            val slotWidth = totalWidth / tabs.size

            // Capsule background with smooth curved top notch sliding under active tab
            Canvas(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(66.dp)
            ) {
                val w = size.width
                val h = size.height
                val slotPx = w / tabs.size
                val notchCenter = slotPx * (animatedIndex + 0.5f)
                val notchHalfWidth = 34.dp.toPx()
                val notchDepth = 22.dp.toPx()
                val corner = 26.dp.toPx()

                val path = Path().apply {
                    moveTo(corner, 0f)
                    val leftEdge = (notchCenter - notchHalfWidth).coerceAtLeast(corner)
                    val rightEdge = (notchCenter + notchHalfWidth).coerceAtMost(w - corner)

                    lineTo(leftEdge, 0f)
                    cubicTo(
                        leftEdge + notchHalfWidth * 0.42f, 0f,
                        notchCenter - notchHalfWidth * 0.55f, notchDepth,
                        notchCenter, notchDepth
                    )
                    cubicTo(
                        notchCenter + notchHalfWidth * 0.55f, notchDepth,
                        rightEdge - notchHalfWidth * 0.42f, 0f,
                        rightEdge, 0f
                    )
                    lineTo(w - corner, 0f)
                    arcTo(
                        rect = Rect(w - 2 * corner, 0f, w, 2 * corner),
                        startAngleDegrees = -90f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                    lineTo(w, h - corner)
                    arcTo(
                        rect = Rect(w - 2 * corner, h - 2 * corner, w, h),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                    lineTo(corner, h)
                    arcTo(
                        rect = Rect(0f, h - 2 * corner, 2 * corner, h),
                        startAngleDegrees = 90f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                    lineTo(0f, corner)
                    arcTo(
                        rect = Rect(0f, 0f, 2 * corner, 2 * corner),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                    close()
                }
                drawPath(path, color = colors.navCapsule)
            }

            // Sliding floating colored pill sitting in the notch
            val pillSize = 46.dp
            val pillOffsetX = slotWidth * (animatedIndex + 0.5f) - (pillSize / 2)
            Box(
                modifier = Modifier
                    .offset(x = pillOffsetX, y = 2.dp)
                    .size(pillSize)
                    .clip(CircleShape)
                    .background(activeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconForTab(activeTab),
                    contentDescription = activeTab.fullLabel,
                    tint = activeIconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            // 5 Interactive tab slots
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(66.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    val isSelected = tab == activeTab
                    val interactionSource = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("nav_tab_${tab.name.lowercase()}")
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { onSelectTab(tab) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Text(
                                text = tab.shortLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    letterSpacing = 0.6.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = Color(0xFFF4F2EE),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 10.dp)
                            )
                        } else {
                            Icon(
                                imageVector = iconForTab(tab),
                                contentDescription = tab.fullLabel,
                                tint = colors.navInactive,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun iconForTab(tab: MainTab): ImageVector = when (tab) {
    MainTab.ACCUEIL -> Icons.Outlined.Home
    MainTab.PROGRAMME -> Icons.Outlined.CalendarToday
    MainTab.NUTRITION -> Icons.Outlined.Restaurant
    MainTab.PROGRES -> Icons.Outlined.BarChart
    MainTab.MOI -> Icons.Outlined.PersonOutline
}
