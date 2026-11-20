package com.example.ui.stage

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.model.BiomeType
import com.example.model.CountyDefinition
import com.example.model.WorldPropType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

const val WORLD_MIN_X = 140f
const val WORLD_MAX_X = 2440f
const val WORLD_WIDTH = 2600f

@Composable
fun BiomeSceneryCanvas(
    county: CountyDefinition,
    cameraX: Float,
    completedMissionIds: Set<String>,
    modifier: Modifier = Modifier,
) {
    val infinite = rememberInfiniteTransition(label = "world_env")
    val envTime by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "env_time",
    )
    val cloudDrift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "cloud_drift",
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val groundY = h * 0.77f

        // 1. Sky & Sunlit Lighting per Biome
        val skyColors = when (county.biome) {
            BiomeType.MOUNTAIN -> listOf(Color(0xFF4FA8D5), Color(0xFFA7E0F5), Color(0xFFFFF2CC))
            BiomeType.FOREST_GROVE -> listOf(Color(0xFF56CCF2), Color(0xFFBEE9E8), Color(0xFFFEF3C7))
            BiomeType.WATERFALL_VALLEY -> listOf(Color(0xFF38BDF8), Color(0xFFBAE6FD), Color(0xFFDCFCE7))
            BiomeType.COASTAL_MANGROVE -> listOf(Color(0xFF0EA5E9), Color(0xFF7DD3FC), Color(0xFFFEF9C3))
            BiomeType.MARTIAN_CLIFFS -> listOf(Color(0xFFFB923C), Color(0xFFFDBA74), Color(0xFFFEF3C7))
            BiomeType.HISTORIC_WINDTOWER -> listOf(Color(0xFF60A5FA), Color(0xFFFDE68A), Color(0xFFFFEDD5))
            BiomeType.ISLAND_GEOPARK -> listOf(Color(0xFF0284C7), Color(0xFFFDE047).copy(alpha = 0.65f), Color(0xFFFFF8E8))
            BiomeType.OCEAN_CORAL -> listOf(Color(0xFF06B6D4), Color(0xFFA5F3FC), Color(0xFFE0F2FE))
        }
        drawRect(brush = Brush.verticalGradient(skyColors))

        // Sun & Soft Sunbeams
        val sunCenter = Offset(w * 0.78f - cameraX * 0.04f, h * 0.20f)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFFFFFBEB), Color(0xFFFDE68A).copy(alpha = 0.35f), Color.Transparent),
                center = sunCenter,
                radius = h * 0.28f,
            ),
            radius = h * 0.28f,
            center = sunCenter,
        )
        drawCircle(color = Color(0xFFFFF7D6), radius = h * 0.068f, center = sunCenter)

        // 2. Animated Drifting Clouds
        for (i in 0..4) {
            val baseCx = ((i * 0.28f + cloudDrift) % 1.25f - 0.1f) * w
            val cy = h * (0.12f + (i % 3) * 0.075f)
            val cw = 68f + (i % 2) * 24f
            drawCartoonCloud(Offset(baseCx, cy), cw)
        }

        // 3. Distant Parallax Mountains / Islands (0.25x)
        drawDistantLayer(county.biome, cameraX * 0.25f, w, groundY)

        // 4. Midground Parallax Landmarks & Trees (0.55x)
        drawMidgroundLayer(county.biome, cameraX * 0.55f, envTime, w, groundY)

        // 5. Foreground Terrain, River/Waves, Boundary Boulders & Mission Props (1.0x)
        drawForegroundTerrain(county.biome, cameraX, envTime, w, h, groundY)

        // Mission Shrines / Interactive Objects
        county.missions.forEach { mission ->
            val sx = mission.worldX - cameraX
            if (sx in -140f..(w + 140f)) {
                val isDone = mission.id in completedMissionIds
                drawMissionProp(
                    propType = mission.propType,
                    screenX = sx,
                    groundY = groundY,
                    isCompleted = isDone,
                    pulse = envTime,
                )
            }
        }

        // 6. Floating Environmental Particles (wind leaves, golden motes, sea sparkles)
        drawAmbientParticles(county.biome, envTime, w, h)
    }
}

private fun DrawScope.drawCartoonCloud(center: Offset, width: Float) {
    val c = Color.White.copy(alpha = 0.85f)
    drawCircle(c, radius = width * 0.28f, center = Offset(center.x - width * 0.24f, center.y + 4f))
    drawCircle(c, radius = width * 0.36f, center = Offset(center.x, center.y - 6f))
    drawCircle(c, radius = width * 0.25f, center = Offset(center.x + width * 0.26f, center.y + 5f))
    drawRoundRect(
        color = c,
        topLeft = Offset(center.x - width * 0.44f, center.y),
        size = Size(width * 0.88f, width * 0.26f),
        cornerRadius = CornerRadius(width * 0.13f, width * 0.13f),
    )
}

private fun DrawScope.drawDistantLayer(
    biome: BiomeType,
    parallaxX: Float,
    w: Float,
    groundY: Float,
) {
    val ridgeColor = when (biome) {
        BiomeType.MOUNTAIN -> Color(0xFFB4654A).copy(alpha = 0.65f)
        BiomeType.MARTIAN_CLIFFS -> Color(0xFFC2410C).copy(alpha = 0.60f)
        BiomeType.ISLAND_GEOPARK -> Color(0xFFD97706).copy(alpha = 0.55f)
        BiomeType.COASTAL_MANGROVE, BiomeType.OCEAN_CORAL -> Color(0xFF0E7490).copy(alpha = 0.35f)
        else -> Color(0xFF4A7C59).copy(alpha = 0.50f)
    }

    val ridgePath = Path().apply {
        moveTo(-100f, groundY)
        var x = -200f - (parallaxX % 420f)
        var idx = 0
        while (x < w + 420f) {
            val peakH = when (biome) {
                BiomeType.MOUNTAIN, BiomeType.WATERFALL_VALLEY -> 115f + (idx % 3) * 45f
                BiomeType.MARTIAN_CLIFFS, BiomeType.ISLAND_GEOPARK -> 95f + (idx % 2) * 55f
                else -> 65f + (idx % 3) * 28f
            }
            lineTo(x + 140f, groundY - peakH)
            lineTo(x + 280f, groundY - peakH * 0.45f)
            x += 280f
            idx++
        }
        lineTo(w + 200f, groundY)
        close()
    }
    drawPath(ridgePath, color = ridgeColor)
}

private fun DrawScope.drawMidgroundLayer(
    biome: BiomeType,
    parallaxX: Float,
    envTime: Float,
    w: Float,
    groundY: Float,
) {
    val sway = sin(envTime * 1.4f) * 6f
    when (biome) {
        BiomeType.MOUNTAIN, BiomeType.WATERFALL_VALLEY -> {
            // Layered rocky foothills + mountain waterfall / stream
            for (i in 0..8) {
                val tx = i * 320f - (parallaxX % 320f)
                val rockH = 74f + (i % 3) * 22f
                val p = Path().apply {
                    moveTo(tx - 60f, groundY)
                    lineTo(tx + 30f, groundY - rockH)
                    lineTo(tx + 120f, groundY)
                    close()
                }
                drawPath(p, color = Color(0xFF9C4E35).copy(alpha = 0.72f))
                if (biome == BiomeType.WATERFALL_VALLEY && i % 3 == 1) {
                    // Animated turquoise waterfall ribbon
                    drawRect(
                        brush = Brush.verticalGradient(listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8))),
                        topLeft = Offset(tx + 24f, groundY - rockH + 10f),
                        size = Size(16f, rockH - 10f),
                    )
                }
                // Native Kahoor / Bene mountain trees
                drawSwayingTree(Offset(tx + 165f, groundY), sway, trunkH = 44f, Color(0xFF2D6A4F))
            }
        }
        BiomeType.FOREST_GROVE -> {
            // Minab Date Palm Groves
            for (i in 0..10) {
                val px = i * 240f - (parallaxX % 240f)
                drawPalmTree(Offset(px, groundY), sway, height = 108f + (i % 2) * 22f)
            }
        }
        BiomeType.COASTAL_MANGROVE, BiomeType.OCEAN_CORAL -> {
            // Sparkling Gulf Sea horizon + sailboat / mangrove islands
            drawRect(
                brush = Brush.verticalGradient(listOf(Color(0xFF0E7490), Color(0xFF22B8CF))),
                topLeft = Offset(0f, groundY - 58f),
                size = Size(w, 60f),
            )
            for (i in 0..8) {
                val wx = i * 260f - (parallaxX % 260f)
                if (biome == BiomeType.COASTAL_MANGROVE) {
                    drawSwayingTree(Offset(wx + 60f, groundY - 12f), sway, trunkH = 34f, Color(0xFF1B4332))
                } else {
                    // Distant wooden lenj sail silhouette
                    val sail = Path().apply {
                        moveTo(wx + 40f, groundY - 30f)
                        lineTo(wx + 56f, groundY - 68f)
                        lineTo(wx + 75f, groundY - 30f)
                        close()
                    }
                    drawPath(sail, color = Color(0xFFFFF8E8))
                }
            }
        }
        BiomeType.HISTORIC_WINDTOWER -> {
            // Traditional southern Iranian adobe architecture, domes & Badgirs (windtowers)
            for (i in 0..7) {
                val bx = i * 330f - (parallaxX % 330f)
                // Adobe house
                drawRoundRect(
                    color = Color(0xFFE6CCB2),
                    topLeft = Offset(bx, groundY - 72f),
                    size = Size(110f, 72f),
                    cornerRadius = CornerRadius(6f, 6f),
                )
                // Badgir windtower
                drawRect(
                    color = Color(0xFFDDB892),
                    topLeft = Offset(bx + 32f, groundY - 122f),
                    size = Size(44f, 52f),
                )
                for (v in 0..2) {
                    drawRect(
                        color = Color(0xFF6D4C41),
                        topLeft = Offset(bx + 38f + v * 12f, groundY - 114f),
                        size = Size(6f, 34f),
                    )
                }
                // Rain cistern dome (Berkeh)
                drawArc(
                    color = Color(0xFFFFF8E8),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(bx + 135f, groundY - 54f),
                    size = Size(72f, 56f),
                )
            }
        }
        BiomeType.MARTIAN_CLIFFS, BiomeType.ISLAND_GEOPARK -> {
            // Stratified eroded canyon pillars (Stars Valley / Chahkooh / Makran Martian cliffs)
            for (i in 0..8) {
                val rx = i * 290f - (parallaxX % 290f)
                val rh = 110f + (i % 3) * 28f
                drawRoundRect(
                    color = if (biome == BiomeType.MARTIAN_CLIFFS) Color(0xFFC85A32) else Color(0xFFD4A373),
                    topLeft = Offset(rx, groundY - rh),
                    size = Size(64f, rh),
                    cornerRadius = CornerRadius(14f, 14f),
                )
                // Horizontal erosion strata lines
                for (s in 1..4) {
                    drawLine(
                        color = Color(0xFF7C2D12).copy(alpha = 0.28f),
                        start = Offset(rx + 6f, groundY - rh + s * 20f),
                        end = Offset(rx + 58f, groundY - rh + s * 20f),
                        strokeWidth = 2.5f,
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawForegroundTerrain(
    biome: BiomeType,
    cameraX: Float,
    envTime: Float,
    w: Float,
    h: Float,
    groundY: Float,
) {
    val topGround = when (biome) {
        BiomeType.FOREST_GROVE, BiomeType.WATERFALL_VALLEY -> Color(0xFF52B788)
        BiomeType.COASTAL_MANGROVE, BiomeType.OCEAN_CORAL -> Color(0xFFF3D5A5)
        BiomeType.MARTIAN_CLIFFS -> Color(0xFFD98A5B)
        else -> Color(0xFFDAB77E)
    }
    val subGround = when (biome) {
        BiomeType.FOREST_GROVE, BiomeType.WATERFALL_VALLEY -> Color(0xFF7F5539)
        BiomeType.MARTIAN_CLIFFS -> Color(0xFF9E2A2B)
        else -> Color(0xFF9C6644)
    }

    drawRect(
        brush = Brush.verticalGradient(listOf(topGround, subGround), startY = groundY, endY = h),
        topLeft = Offset(0f, groundY),
        size = Size(w, h - groundY),
    )

    // Animated water / mountain stream strip at the edge of Bashagard / coastal stages
    val hasWaterForeground = biome in setOf(
        BiomeType.MOUNTAIN,
        BiomeType.COASTAL_MANGROVE,
        BiomeType.OCEAN_CORAL,
        BiomeType.WATERFALL_VALLEY,
    )
    if (hasWaterForeground) {
        val waterTop = groundY + (h - groundY) * 0.48f
        drawRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF22B8CF), Color(0xFF0E7490))),
            topLeft = Offset(0f, waterTop),
            size = Size(w, h - waterTop),
        )
        // Animated wave crests
        val waveShift = (envTime * 40f) % 80f
        var wx = -80f + waveShift
        while (wx < w + 80f) {
            drawArc(
                color = Color.White.copy(alpha = 0.45f),
                startAngle = 190f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(wx, waterTop + 6f),
                size = Size(58f, 12f),
                style = Stroke(width = 2.5f, cap = StrokeCap.Round),
            )
            wx += 76f
        }
    }

    // World boundary rocks at left/right edges so player cannot leave stage
    listOf(60f - cameraX, WORLD_WIDTH - 60f - cameraX).forEach { bx ->
        if (bx in -120f..(w + 120f)) {
            drawCircle(
                color = Color(0xFF5D4037),
                radius = 48f,
                center = Offset(bx, groundY - 12f),
            )
            drawCircle(
                color = Color(0xFF8D6E63),
                radius = 32f,
                center = Offset(bx + 18f, groundY - 24f),
            )
        }
    }
}

private fun DrawScope.drawPalmTree(base: Offset, sway: Float, height: Float) {
    // Curved trunk
    val top = Offset(base.x + sway * 0.8f, base.y - height)
    drawLine(
        color = Color(0xFF7F5539),
        start = base,
        end = top,
        strokeWidth = 9f,
        cap = StrokeCap.Round,
    )
    // Fronds
    for (a in -2..2) {
        rotate(degrees = a * 26f + sway * 0.6f, pivot = top) {
            val frond = Path().apply {
                moveTo(top.x, top.y)
                quadraticTo(top.x + 24f, top.y - 16f, top.x + 42f, top.y + 8f)
                quadraticTo(top.x + 20f, top.y + 2f, top.x, top.y)
                close()
            }
            drawPath(frond, color = Color(0xFF2D6A4F))
        }
    }
}

private fun DrawScope.drawSwayingTree(base: Offset, sway: Float, trunkH: Float, foliageColor: Color) {
    val top = Offset(base.x + sway * 0.4f, base.y - trunkH)
    drawLine(
        color = Color(0xFF6D4C41),
        start = base,
        end = top,
        strokeWidth = 7f,
        cap = StrokeCap.Round,
    )
    drawCircle(color = foliageColor, radius = 24f, center = top)
    drawCircle(color = foliageColor.copy(alpha = 0.88f), radius = 17f, center = Offset(top.x - 14f, top.y + 8f))
    drawCircle(color = foliageColor.copy(alpha = 0.88f), radius = 17f, center = Offset(top.x + 14f, top.y + 8f))
}

private fun DrawScope.drawMissionProp(
    propType: WorldPropType,
    screenX: Float,
    groundY: Float,
    isCompleted: Boolean,
    pulse: Float,
) {
    val glowRadius = 36f + sin(pulse * 3f) * 6f
    val beaconColor = if (isCompleted) Color(0xFF52B788) else Color(0xFFF6C453)

    // Magical clue aura behind prop
    drawCircle(
        color = beaconColor.copy(alpha = if (isCompleted) 0.22f else 0.38f),
        radius = glowRadius,
        center = Offset(screenX, groundY - 36f),
    )

    // Pedestal / Prop visual
    drawRoundRect(
        color = Color(0xFF6D4C41),
        topLeft = Offset(screenX - 24f, groundY - 22f),
        size = Size(48f, 22f),
        cornerRadius = CornerRadius(6f, 6f),
    )

    when (propType) {
        WorldPropType.WATER_CHANNEL_STONE, WorldPropType.WATERMILL_WHEEL -> {
            drawCircle(
                color = Color(0xFF0E7490),
                radius = 20f,
                center = Offset(screenX, groundY - 42f),
            )
            drawCircle(
                color = Color(0xFFE0F2FE),
                radius = 10f,
                center = Offset(screenX, groundY - 42f),
            )
        }
        WorldPropType.BOAT_DOCK, WorldPropType.CORAL_BUOY -> {
            drawRoundRect(
                color = Color(0xFFC85A32),
                topLeft = Offset(screenX - 18f, groundY - 58f),
                size = Size(36f, 36f),
                cornerRadius = CornerRadius(10f, 10f),
            )
        }
        else -> {
            // Ancient explorer stone/shrine with glowing turquoise gem
            val stone = Path().apply {
                moveTo(screenX - 18f, groundY - 22f)
                lineTo(screenX - 12f, groundY - 66f)
                lineTo(screenX + 12f, groundY - 66f)
                lineTo(screenX + 18f, groundY - 22f)
                close()
            }
            drawPath(stone, color = Color(0xFFE6CCB2))
            drawCircle(
                color = beaconColor,
                radius = 9f,
                center = Offset(screenX, groundY - 48f),
            )
        }
    }
}

private fun DrawScope.drawAmbientParticles(
    biome: BiomeType,
    envTime: Float,
    w: Float,
    h: Float,
) {
    val particleColor = when (biome) {
        BiomeType.COASTAL_MANGROVE, BiomeType.OCEAN_CORAL -> Color(0xFFE0F2FE)
        BiomeType.FOREST_GROVE, BiomeType.WATERFALL_VALLEY -> Color(0xFFBBF7D0)
        else -> Color(0xFFFDE68A)
    }
    for (i in 0..10) {
        val px = ((i * 173f + envTime * 45f) % (w + 40f)) - 20f
        val py = (h * (0.18f + (i % 5) * 0.13f)) + sin(envTime * 2f + i) * 14f
        drawCircle(
            color = particleColor.copy(alpha = 0.55f),
            radius = 3.2f + (i % 3),
            center = Offset(px, py),
        )
    }
}
