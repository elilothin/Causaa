package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.data.catalog.NutritionCalculator
import com.example.data.model.*
import com.example.ui.theme.RankColorZPlus
import kotlinx.coroutines.delay
import kotlin.math.*

enum class CameraViewAngle(val label: String, val yawRad: Float) {
    FACE("Face", 0.18f),
    PROFIL("Profil", 1.38f),
    DOS("Dos", 3.14f),
}

private data class Vec3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(o: Vec3) = Vec3(x + o.x, y + o.y, z + o.z)
    operator fun minus(o: Vec3) = Vec3(x - o.x, y - o.y, z - o.z)
    operator fun times(s: Float) = Vec3(x * s, y * s, z * s)
    fun normalized(): Vec3 {
        val len = sqrt(x * x + y * y + z * z)
        return if (len > 1e-5f) Vec3(x / len, y / len, z / len) else Vec3(0f, 1f, 0f)
    }
    fun cross(o: Vec3) = Vec3(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x)
    fun dot(o: Vec3) = x * o.x + y * o.y + z * o.z
}

private data class ProjectedTri(
    val p1: Offset,
    val p2: Offset,
    val p3: Offset,
    val depth: Float,
    val color: Color,
)

@Composable
fun Character3DView(
    params: CharacterVisualParams,
    animation: AnimationKind = AnimationKind.IDLE_BREATHE,
    cameraAngle: CameraViewAngle = CameraViewAngle.FACE,
    reduceMotion: Boolean = false,
    economy30Fps: Boolean = false,
    interactiveOrbit: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var yawOffset by remember(cameraAngle) { mutableFloatStateOf(0f) }
    var phase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(animation, reduceMotion, economy30Fps) {
        if (reduceMotion) {
            phase = 0.25f
            return@LaunchedEffect
        }
        val frameDelayMs = if (economy30Fps) 33L else 16L
        val step = if (economy30Fps) 0.055f else 0.028f
        while (true) {
            phase = (phase + step) % (2f * PI.toFloat())
            delay(frameDelayMs)
        }
    }

    val dragModifier = if (interactiveOrbit) {
        Modifier.pointerInput(cameraAngle) {
            detectHorizontalDragGestures { change, dragAmount ->
                change.consume()
                yawOffset += dragAmount * 0.012f
            }
        }
    } else {
        Modifier
    }

    Canvas(modifier = modifier.then(dragModifier)) {
        val totalYaw = cameraAngle.yawRad + yawOffset
        drawCharacter3D(
            params = params,
            animation = animation,
            phase = phase,
            yaw = totalYaw,
            pitch = -0.06f,
        )
    }
}

private fun DrawScope.drawCharacter3D(
    params: CharacterVisualParams,
    animation: AnimationKind,
    phase: Float,
    yaw: Float,
    pitch: Float,
) {
    val triangles = mutableListOf<ProjectedTri>()

    // Morph targets from Section 10
    val heightScale = (0.86f + ((params.heightCm.coerceIn(160, 200) - 160) / 40f) * 0.26f)
        .coerceIn(0.82f, 1.15f)
    val bmi = NutritionCalculator.calculateBmi(params.weightKg, params.heightCm).toFloat()
        .coerceIn(16.5f, 36f)
    val bmiFactor = ((bmi - 18f) / 14f).coerceIn(0f, 1.2f)

    val morphWidth = when (params.morphotype) {
        Morphotype.ECTOMORPHE -> 0.88f
        Morphotype.MESOMORPHE -> 1.04f
        Morphotype.ENDOMORPHE -> 1.18f
    }
    val muscleScale = 0.90f + (params.muscleStage * 0.045f)
    val seniorBlend = ((params.age - 55).coerceAtLeast(0) / 35f).coerceIn(0f, 1f)

    val isMale = params.sex == Sex.HOMME
    val shoulderHalf = (if (isMale) 0.225f else 0.185f) *
        (if (params.morphotype == Morphotype.MESOMORPHE) 1.08f else 1f) *
        (1f + bmiFactor * 0.14f) * muscleScale
    val waistHalf = (if (isMale) 0.155f else 0.135f) * morphWidth * (1f + bmiFactor * 0.34f)
    val hipHalf = (if (isMale) 0.165f else 0.185f) * morphWidth * (1f + bmiFactor * 0.28f)
    val limbThickness = (0.056f + bmiFactor * 0.022f) * morphWidth * (0.94f + params.muscleStage * 0.03f)

    // Biomechanical cycle t in [0, 1]
    val s = (sin(phase) * 0.5f + 0.5f)
    val c = cos(phase)

    // Joint state variables
    var pelvisY = 0.90f
    var torsoPitch = 0.05f * seniorBlend
    var torsoYaw = 0f
    var kneeFlexL = 0.05f
    var kneeFlexR = 0.05f
    var hipFlexL = 0.03f
    var hipFlexR = 0.03f
    var hipAbduct = 0.06f
    var armAbductL = 0.14f
    var armAbductR = 0.14f
    var armRaiseFwdL = 0.04f
    var armRaiseFwdR = 0.04f
    var elbowFlexL = 0.15f
    var elbowFlexR = 0.15f
    var bodyFloorTilt = 0f // 0 upright, ~1.4 horizontal for pushup/plank/bench
    var musclePump = 1.0f

    when (animation) {
        AnimationKind.IDLE_BREATHE -> {
            val breath = sin(phase) * 0.015f
            armAbductL = 0.14f + breath
            armAbductR = 0.14f + breath
            elbowFlexL = 0.18f
            elbowFlexR = 0.18f
        }
        AnimationKind.SHOULDER_PRESS -> {
            armAbductL = 1.45f + s * 1.35f
            armAbductR = 1.45f + s * 1.35f
            armRaiseFwdL = 0.25f
            armRaiseFwdR = 0.25f
            elbowFlexL = 1.55f - s * 1.30f
            elbowFlexR = 1.55f - s * 1.30f
            musclePump = 1f + s * 0.06f
        }
        AnimationKind.LATERAL_RAISE -> {
            armAbductL = 0.18f + s * 1.35f
            armAbductR = 0.18f + s * 1.35f
            elbowFlexL = 0.22f
            elbowFlexR = 0.22f
            musclePump = 1f + s * 0.05f
        }
        AnimationKind.BICEP_CURL -> {
            armAbductL = 0.14f
            armAbductR = 0.14f
            armRaiseFwdL = 0.10f
            armRaiseFwdR = 0.10f
            elbowFlexL = 0.20f + s * 2.05f
            elbowFlexR = 0.20f + s * 2.05f
            musclePump = 1f + s * 0.08f
        }
        AnimationKind.TRICEP_EXTENSION -> {
            armAbductL = 0.25f
            armAbductR = 0.25f
            armRaiseFwdL = 2.75f
            armRaiseFwdR = 2.75f
            elbowFlexL = 1.85f - s * 1.55f
            elbowFlexR = 1.85f - s * 1.55f
            musclePump = 1f + s * 0.06f
        }
        AnimationKind.SQUAT -> {
            // Feet remain planted on ground! Pelvis drops while hip/knee flex
            val squatDepth = s
            pelvisY = 0.90f - squatDepth * 0.34f
            hipFlexL = squatDepth * 1.32f
            hipFlexR = squatDepth * 1.32f
            kneeFlexL = squatDepth * 1.42f
            kneeFlexR = squatDepth * 1.42f
            torsoPitch = 0.10f + squatDepth * 0.35f
            armRaiseFwdL = 0.30f + squatDepth * 0.85f
            armRaiseFwdR = 0.30f + squatDepth * 0.85f
            musclePump = 1f + squatDepth * 0.05f
        }
        AnimationKind.LUNGE -> {
            val depth = s
            pelvisY = 0.90f - depth * 0.24f
            hipFlexL = depth * 1.15f
            kneeFlexL = depth * 1.25f
            hipFlexR = -0.35f * depth
            kneeFlexR = depth * 1.10f
            torsoPitch = 0.08f
            armAbductL = 0.20f
            armAbductR = 0.20f
        }
        AnimationKind.JUMPING_JACK -> {
            pelvisY = 0.90f + s * 0.07f
            hipAbduct = 0.06f + s * 0.36f
            armAbductL = 0.15f + s * 2.65f
            armAbductR = 0.15f + s * 2.65f
            kneeFlexL = s * 0.18f
            kneeFlexR = s * 0.18f
        }
        AnimationKind.CALF_RAISE -> {
            pelvisY = 0.90f + s * 0.09f
            armAbductL = 0.14f
            armAbductR = 0.14f
        }
        AnimationKind.PUSHUP, AnimationKind.BENCH_PRESS -> {
            bodyFloorTilt = if (animation == AnimationKind.PUSHUP) 1.32f else -1.45f
            pelvisY = 0.42f + if (animation == AnimationKind.PUSHUP) s * 0.12f else 0f
            armRaiseFwdL = 1.45f
            armRaiseFwdR = 1.45f
            armAbductL = 0.38f
            armAbductR = 0.38f
            elbowFlexL = 1.45f - s * 1.25f
            elbowFlexR = 1.45f - s * 1.25f
            musclePump = 1f + s * 0.06f
        }
        AnimationKind.PLANK -> {
            bodyFloorTilt = 1.38f
            pelvisY = 0.38f + sin(phase) * 0.01f
            armRaiseFwdL = 1.40f
            armRaiseFwdR = 1.40f
            elbowFlexL = 1.45f
            elbowFlexR = 1.45f
        }
        AnimationKind.PULLUP -> {
            pelvisY = 0.92f + s * 0.22f
            armRaiseFwdL = 2.65f - s * 0.35f
            armRaiseFwdR = 2.65f - s * 0.35f
            armAbductL = 0.55f
            armAbductR = 0.55f
            elbowFlexL = 0.20f + s * 1.75f
            elbowFlexR = 0.20f + s * 1.75f
            kneeFlexL = 0.35f
            kneeFlexR = 0.35f
            musclePump = 1f + s * 0.07f
        }
        AnimationKind.DIP -> {
            pelvisY = 0.96f - s * 0.18f
            armAbductL = 0.32f
            armAbductR = 0.32f
            armRaiseFwdL = -0.22f
            armRaiseFwdR = -0.22f
            elbowFlexL = 0.18f + s * 1.45f
            elbowFlexR = 0.18f + s * 1.45f
            kneeFlexL = 0.45f
            kneeFlexR = 0.45f
        }
        AnimationKind.ROWING -> {
            torsoPitch = 0.72f
            pelvisY = 0.86f
            hipFlexL = 0.28f
            hipFlexR = 0.28f
            kneeFlexL = 0.28f
            kneeFlexR = 0.28f
            armRaiseFwdL = 1.15f - s * 0.75f
            armRaiseFwdR = 1.15f - s * 0.75f
            elbowFlexL = 0.20f + s * 1.55f
            elbowFlexR = 0.20f + s * 1.55f
        }
        AnimationKind.DEADLIFT -> {
            val bend = 1f - s
            pelvisY = 0.90f - bend * 0.18f
            torsoPitch = 0.06f + bend * 0.88f
            hipFlexL = bend * 0.72f
            hipFlexR = bend * 0.72f
            kneeFlexL = bend * 0.55f
            kneeFlexR = bend * 0.55f
            armRaiseFwdL = 0.15f + bend * 0.75f
            armRaiseFwdR = 0.15f + bend * 0.75f
        }
        AnimationKind.HIP_THRUST -> {
            bodyFloorTilt = -1.25f
            pelvisY = 0.35f + s * 0.20f
            hipFlexL = 1.25f - s * 1.05f
            hipFlexR = 1.25f - s * 1.05f
            kneeFlexL = 1.35f
            kneeFlexR = 1.35f
        }
        AnimationKind.CRUNCH -> {
            bodyFloorTilt = -1.45f + s * 0.55f
            pelvisY = 0.30f
            hipFlexL = 1.10f
            hipFlexR = 1.10f
            kneeFlexL = 1.35f
            kneeFlexR = 1.35f
            armRaiseFwdL = 2.15f
            armRaiseFwdR = 2.15f
            elbowFlexL = 1.65f
            elbowFlexR = 1.65f
        }
    }

    // Build 3D skeleton positions in world space (Y up, X right, Z forward)
    val root = Vec3(0f, pelvisY * heightScale, 0f)
    val torsoLen = 0.54f * heightScale
    val neckLen = 0.08f * heightScale
    val thighLen = 0.42f * heightScale
    val shinLen = 0.41f * heightScale
    val upperArmLen = 0.28f * heightScale
    val foreArmLen = 0.26f * heightScale

    fun rotateAround(v: Vec3, rx: Float, ry: Float, rz: Float): Vec3 {
        // Z then X then Y
        val cz = cos(rz); val sz = sin(rz)
        val x1 = v.x * cz - v.y * sz
        val y1 = v.x * sz + v.y * cz
        val z1 = v.z

        val cx = cos(rx); val sx = sin(rx)
        val y2 = y1 * cx - z1 * sx
        val z2 = y1 * sx + z1 * cx
        val x2 = x1

        val cy = cos(ry); val sy = sin(ry)
        val x3 = x2 * cy + z2 * sy
        val z3 = -x2 * sy + z2 * cy
        return Vec3(x3, y2, z3)
    }

    val totalTorsoRx = torsoPitch + bodyFloorTilt
    val spineDir = rotateAround(Vec3(0f, 1f, 0f), totalTorsoRx, torsoYaw, 0f)
    val torsoRight = rotateAround(Vec3(1f, 0f, 0f), totalTorsoRx, torsoYaw, 0f)
    val torsoFwd = rotateAround(Vec3(0f, 0f, 1f), totalTorsoRx, torsoYaw, 0f)

    val chestMid = root + spineDir * (torsoLen * 0.58f)
    val shoulderCenter = root + spineDir * torsoLen
    val headCenter = shoulderCenter + spineDir * (neckLen + 0.12f * heightScale)

    val lShoulder = shoulderCenter - torsoRight * shoulderHalf
    val rShoulder = shoulderCenter + torsoRight * shoulderHalf

    // Left Arm
    val lUpperDir = rotateAround(Vec3(0f, -1f, 0f), totalTorsoRx - armRaiseFwdL, torsoYaw, -armAbductL)
    val lElbow = lShoulder + lUpperDir * upperArmLen
    val lForeDir = rotateAround(Vec3(0f, -1f, 0f), totalTorsoRx - armRaiseFwdL - elbowFlexL, torsoYaw, -armAbductL * 0.85f)
    val lHand = lElbow + lForeDir * foreArmLen

    // Right Arm
    val rUpperDir = rotateAround(Vec3(0f, -1f, 0f), totalTorsoRx - armRaiseFwdR, torsoYaw, armAbductR)
    val rElbow = rShoulder + rUpperDir * upperArmLen
    val rForeDir = rotateAround(Vec3(0f, -1f, 0f), totalTorsoRx - armRaiseFwdR - elbowFlexR, torsoYaw, armAbductR * 0.85f)
    val rHand = rElbow + rForeDir * foreArmLen

    // Legs
    val lHip = root - torsoRight * (hipHalf * 0.56f)
    val rHip = root + torsoRight * (hipHalf * 0.56f)

    val legBaseRx = bodyFloorTilt
    val lThighDir = rotateAround(Vec3(0f, -1f, 0f), legBaseRx - hipFlexL, 0f, -hipAbduct)
    val lKnee = lHip + lThighDir * thighLen
    val lShinDir = rotateAround(Vec3(0f, -1f, 0f), legBaseRx - hipFlexL + kneeFlexL, 0f, -hipAbduct * 0.5f)
    val lAnkle = lKnee + lShinDir * shinLen

    val rThighDir = rotateAround(Vec3(0f, -1f, 0f), legBaseRx - hipFlexR, 0f, hipAbduct)
    val rKnee = rHip + rThighDir * thighLen
    val rShinDir = rotateAround(Vec3(0f, -1f, 0f), legBaseRx - hipFlexR + kneeFlexR, 0f, hipAbduct * 0.5f)
    val rAnkle = rKnee + rShinDir * shinLen

    // Camera projection setup
    val scalePx = size.minDimension * 0.44f
    val centerScreen = Offset(size.width * 0.5f, size.height * 0.90f)
    val lightDir = Vec3(0.45f, 0.72f, 0.52f).normalized()

    fun worldToCam(p: Vec3): Vec3 {
        // Center vertically around y = 0.95
        val centered = Vec3(p.x, p.y, p.z)
        val cy = cos(yaw); val sy = sin(yaw)
        val x1 = centered.x * cy - centered.z * sy
        val z1 = centered.x * sy + centered.z * cy

        val cp = cos(pitch); val sp = sin(pitch)
        val y2 = centered.y * cp - z1 * sp
        val z2 = centered.y * sp + z1 * cp
        return Vec3(x1, y2, z2)
    }

    fun project(cam: Vec3): Offset {
        val camDist = 3.2f
        val perspective = camDist / (camDist - cam.z).coerceAtLeast(0.6f)
        return Offset(
            x = centerScreen.x + cam.x * scalePx * perspective,
            y = centerScreen.y - cam.y * scalePx * perspective,
        )
    }

    fun addQuad(a: Vec3, b: Vec3, cPt: Vec3, d: Vec3, baseColor: Color) {
        val ca = worldToCam(a)
        val cb = worldToCam(b)
        val cc = worldToCam(cPt)
        val cd = worldToCam(d)
        val normal = (cb - ca).cross(cc - ca).normalized()
        val shade = (0.58f + 0.42f * normal.dot(lightDir).coerceIn(-0.4f, 1f)).coerceIn(0.42f, 1.12f)
        val shadedColor = Color(
            red = (baseColor.red * shade).coerceIn(0f, 1f),
            green = (baseColor.green * shade).coerceIn(0f, 1f),
            blue = (baseColor.blue * shade).coerceIn(0f, 1f),
            alpha = baseColor.alpha,
        )
        val pa = project(ca); val pb = project(cb); val pc = project(cc); val pd = project(cd)
        val d1 = (ca.z + cb.z + cc.z) / 3f
        val d2 = (ca.z + cc.z + cd.z) / 3f
        triangles.add(ProjectedTri(pa, pb, pc, d1, shadedColor))
        triangles.add(ProjectedTri(pa, pc, pd, d2, shadedColor))
    }

    fun addPrism(start: Vec3, end: Vec3, rStart: Float, rEnd: Float, sides: Int, color: Color) {
        val axis = (end - start)
        if (axis.dot(axis) < 1e-6f) return
        val dir = axis.normalized()
        val ref = if (abs(dir.y) < 0.9f) Vec3(0f, 1f, 0f) else Vec3(1f, 0f, 0f)
        val u = dir.cross(ref).normalized()
        val v = dir.cross(u).normalized()

        val ringA = Array(sides) { i ->
            val ang = (2.0 * PI * i / sides).toFloat()
            start + (u * cos(ang) + v * sin(ang)) * rStart
        }
        val ringB = Array(sides) { i ->
            val ang = (2.0 * PI * i / sides).toFloat()
            end + (u * cos(ang) + v * sin(ang)) * rEnd
        }
        for (i in 0 until sides) {
            val next = (i + 1) % sides
            addQuad(ringA[i], ringA[next], ringB[next], ringB[i], color)
        }
    }

    fun addFacetedHead(center: Vec3, r: Float, color: Color) {
        // Faceless slightly faceted 3D ovoid head (zero eyes/nose/mouth)
        val rings = 4
        val sectors = 8
        val latitudes = listOf(-1.15f, -0.55f, 0.1f, 0.72f, 1.15f)
        val ringPts = latitudes.map { lat ->
            val yr = sin(lat * 0.9f) * r * 1.25f
            val rad = cos(lat * 0.9f).coerceAtLeast(0.18f) * r * 0.95f
            Array(sectors) { sIdx ->
                val ang = (2.0 * PI * sIdx / sectors).toFloat()
                center + spineDir * yr + torsoRight * (cos(ang) * rad) + torsoFwd * (sin(ang) * rad)
            }
        }
        for (rIdx in 0 until rings) {
            val aRing = ringPts[rIdx]
            val bRing = ringPts[rIdx + 1]
            for (sIdx in 0 until sectors) {
                val next = (sIdx + 1) % sectors
                addQuad(aRing[sIdx], aRing[next], bRing[next], bRing[sIdx], color)
            }
        }
    }

    val skin = params.skinColor
    val outfit = params.outfitColor
    val shoeWhite = Color(0xFFF4F2EE)
    val shoeAccent = params.shoeAccentColor

    // Torso: lower waist -> chest -> shoulders
    val waistTop = root + spineDir * (torsoLen * 0.45f)
    val shortTop = root + spineDir * (torsoLen * 0.14f)

    // Shorts (pelvis)
    addPrism(root - spineDir * (0.04f * heightScale), shortTop, hipHalf * 0.98f, waistHalf * 0.96f, 8, outfit)
    // Waist / Abs
    val absColor = if (isMale) skin else skin
    addPrism(shortTop, waistTop, waistHalf * 0.94f, waistHalf * 1.02f, 8, absColor)
    // Chest (male bare torso vs female cropped sports top)
    val chestColor = if (isMale) skin else outfit
    addPrism(waistTop, shoulderCenter, waistHalf * 1.02f, shoulderHalf * 0.98f, 8, chestColor)

    // Muscle definition detail (stages 3..5): pectoral/ab plates on front
    if (isMale && params.muscleStage >= 3 && params.morphotype != Morphotype.ENDOMORPHE) {
        val pecColor = skin.copy(
            red = (skin.red * 0.92f).coerceIn(0f, 1f),
            green = (skin.green * 0.92f).coerceIn(0f, 1f),
            blue = (skin.blue * 0.92f).coerceIn(0f, 1f),
        )
        val pecCenterL = chestMid - torsoRight * (shoulderHalf * 0.34f) + torsoFwd * (waistHalf * 0.72f)
        val pecCenterR = chestMid + torsoRight * (shoulderHalf * 0.34f) + torsoFwd * (waistHalf * 0.72f)
        addPrism(pecCenterL - spineDir * 0.05f, pecCenterL + spineDir * 0.05f, 0.06f, 0.065f, 6, pecColor)
        addPrism(pecCenterR - spineDir * 0.05f, pecCenterR + spineDir * 0.05f, 0.06f, 0.065f, 6, pecColor)
    }

    // Neck & Faceless Head
    addPrism(shoulderCenter, shoulderCenter + spineDir * neckLen, limbThickness * 0.78f, limbThickness * 0.68f, 6, skin)
    addFacetedHead(headCenter, 0.115f * heightScale, skin)

    // Arms (deltoid/bicep/forearm)
    val armR = limbThickness * musclePump
    addPrism(lShoulder, lElbow, armR * 1.06f, armR * 0.85f, 7, skin)
    addPrism(lElbow, lHand, armR * 0.82f, armR * 0.64f, 6, skin)
    addPrism(rShoulder, rElbow, armR * 1.06f, armR * 0.85f, 7, skin)
    addPrism(rElbow, rHand, armR * 0.82f, armR * 0.64f, 6, skin)

    // Legs (short upper thigh in outfit color, bare lower thigh/calf, white sneakers with accent)
    val thighR = limbThickness * 1.48f * musclePump
    val lShortEnd = lHip + lThighDir * (thighLen * 0.45f)
    val rShortEnd = rHip + rThighDir * (thighLen * 0.45f)
    addPrism(lHip, lShortEnd, thighR * 1.04f, thighR * 0.96f, 7, outfit)
    addPrism(lShortEnd, lKnee, thighR * 0.94f, thighR * 0.76f, 7, skin)
    addPrism(lKnee, lAnkle, thighR * 0.78f, thighR * 0.52f, 7, skin)

    addPrism(rHip, rShortEnd, thighR * 1.04f, thighR * 0.96f, 7, outfit)
    addPrism(rShortEnd, rKnee, thighR * 0.94f, thighR * 0.76f, 7, skin)
    addPrism(rKnee, rAnkle, thighR * 0.78f, thighR * 0.52f, 7, skin)

    // Sneakers (white base + accent collar)
    val footFwd = torsoFwd * (0.11f * heightScale)
    val lFootBack = lAnkle - torsoFwd * (0.03f * heightScale)
    val rFootBack = rAnkle - torsoFwd * (0.03f * heightScale)
    addPrism(lFootBack, lAnkle + footFwd, thighR * 0.60f, thighR * 0.50f, 6, shoeWhite)
    addPrism(lAnkle + spineDir * 0.02f, lAnkle, thighR * 0.56f, thighR * 0.56f, 6, shoeAccent)
    addPrism(rFootBack, rAnkle + footFwd, thighR * 0.60f, thighR * 0.50f, 6, shoeWhite)
    addPrism(rAnkle + spineDir * 0.02f, rAnkle, thighR * 0.56f, thighR * 0.56f, 6, shoeAccent)

    // Gold Aura ring for ranks S, Z, Z+
    if (params.hasGoldAura) {
        val auraCenter = project(worldToCam(Vec3(0f, 0.05f, 0f)))
        drawCircle(
            color = RankColorZPlus.copy(alpha = 0.45f),
            radius = scalePx * 0.46f,
            center = auraCenter,
            style = Stroke(width = 3.dp.toPx())
        )
    }

    // Painter's algorithm sort (far to near)
    triangles.sortBy { it.depth }
    triangles.forEach { tri ->
        val p = Path().apply {
            moveTo(tri.p1.x, tri.p1.y)
            lineTo(tri.p2.x, tri.p2.y)
            lineTo(tri.p3.x, tri.p3.y)
            close()
        }
        drawPath(p, color = tri.color)
    }
}
