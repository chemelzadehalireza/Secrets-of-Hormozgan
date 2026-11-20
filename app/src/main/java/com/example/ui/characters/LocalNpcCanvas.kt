package com.example.ui.characters

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import com.example.model.HeadwearStyle
import com.example.model.NpcAppearance
import kotlin.math.PI
import kotlin.math.sin

/**
 * Distinct 2D cartoon renderer for all 13 fictional local characters of Hormozgan
 * with culturally respectful southern Iranian clothing, breathing idle, and lip-sync.
 */
@Composable
fun LocalNpcCanvas(
    appearance: NpcAppearance,
    isSpeaking: Boolean = false,
    mouthOpenLevel: Float = 0f,
    facingLeft: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val infinite = rememberInfiniteTransition(label = "npc_anim")
    val breath by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "npc_breath",
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val unit = minOf(w, h) / 140f
        val bobY = sin(breath) * 1.4f * unit

        drawOval(
            color = Color.Black.copy(alpha = 0.2f),
            topLeft = Offset(w * 0.5f - 28f * unit, h * 0.89f),
            size = Size(56f * unit, 10f * unit),
        )

        val flip = if (facingLeft) -1f else 1f
        scale(scaleX = flip, scaleY = 1f, pivot = Offset(w * 0.5f, h * 0.5f)) {
            val cx = w * 0.5f
            val baseY = h * 0.89f + bobY
            val primary = Color(appearance.primaryColorHex)
            val accent = Color(appearance.accentColorHex)
            val skin = Color(appearance.skinToneHex)

            // Traditional robe / tunic silhouette
            val robe = Path().apply {
                moveTo(cx - 17f * unit, baseY - 68f * unit)
                lineTo(cx + 17f * unit, baseY - 68f * unit)
                lineTo(cx + 21f * unit, baseY - 8f * unit)
                lineTo(cx - 21f * unit, baseY - 8f * unit)
                close()
            }
            drawPath(robe, color = primary)

            // Decorative southern embroidered vertical band
            drawRect(
                color = accent,
                topLeft = Offset(cx - 3f * unit, baseY - 68f * unit),
                size = Size(6f * unit, 56f * unit),
            )
            // Shoes
            drawRoundRect(
                color = Color(0xFF4E342E),
                topLeft = Offset(cx - 12f * unit, baseY - 9f * unit),
                size = Size(11f * unit, 8f * unit),
                cornerRadius = CornerRadius(3f * unit, 3f * unit),
            )
            drawRoundRect(
                color = Color(0xFF4E342E),
                topLeft = Offset(cx + 3f * unit, baseY - 9f * unit),
                size = Size(11f * unit, 8f * unit),
                cornerRadius = CornerRadius(3f * unit, 3f * unit),
            )

            // Friendly gesturing arm
            val armShift = if (isSpeaking) sin(breath * 3f) * 4f * unit else 0f
            drawRoundRect(
                color = primary.copy(alpha = 0.92f),
                topLeft = Offset(cx + 10f * unit, baseY - 62f * unit - armShift * 0.3f),
                size = Size(10f * unit, 22f * unit),
                cornerRadius = CornerRadius(5f * unit, 5f * unit),
            )
            drawCircle(
                color = skin,
                radius = 4.2f * unit,
                center = Offset(cx + 16f * unit, baseY - 38f * unit - armShift),
            )

            // Head & Hair / Shawl
            val headCenter = Offset(cx + 1f * unit, baseY - 85f * unit)
            if (appearance.headwear == HeadwearStyle.TRADITIONAL_SHAWL) {
                // Respectful colorful Bandari shawl framing face & shoulders
                val shawl = Path().apply {
                    moveTo(headCenter.x - 19f * unit, headCenter.y + 30f * unit)
                    cubicTo(
                        headCenter.x - 21f * unit, headCenter.y - 22f * unit,
                        headCenter.x + 17f * unit, headCenter.y - 24f * unit,
                        headCenter.x + 15f * unit, headCenter.y - 2f * unit,
                    )
                    lineTo(headCenter.x + 2f * unit, headCenter.y + 24f * unit)
                    close()
                }
                drawPath(shawl, color = accent)
            }

            // Face
            drawOval(
                color = skin,
                topLeft = Offset(headCenter.x - 14f * unit, headCenter.y - 15.5f * unit),
                size = Size(28f * unit, 31f * unit),
            )

            // Hair for non-covered styles
            if (appearance.headwear == HeadwearStyle.NONE || appearance.headwear == HeadwearStyle.SUN_HAT) {
                drawArc(
                    color = Color(0xFF2B211E),
                    startAngle = 175f,
                    sweepAngle = 185f,
                    useCenter = true,
                    topLeft = Offset(headCenter.x - 15f * unit, headCenter.y - 18f * unit),
                    size = Size(30f * unit, 18f * unit),
                )
            }

            // Headwear variants
            when (appearance.headwear) {
                HeadwearStyle.TURBAN_LIGHT -> {
                    drawOval(
                        color = Color(0xFFFDF8EE),
                        topLeft = Offset(headCenter.x - 17f * unit, headCenter.y - 23f * unit),
                        size = Size(34f * unit, 16f * unit),
                    )
                    drawArc(
                        color = accent,
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(headCenter.x - 15f * unit, headCenter.y - 20f * unit),
                        size = Size(30f * unit, 10f * unit),
                        style = Stroke(width = 2.2f * unit),
                    )
                }
                HeadwearStyle.SOUTHERN_CAP -> {
                    drawRoundRect(
                        color = Color(0xFFF5EFE0),
                        topLeft = Offset(headCenter.x - 13f * unit, headCenter.y - 21f * unit),
                        size = Size(26f * unit, 11f * unit),
                        cornerRadius = CornerRadius(5f * unit, 5f * unit),
                    )
                }
                HeadwearStyle.CAPTAIN_HAT -> {
                    drawRoundRect(
                        color = Color(0xFF0F4C5C),
                        topLeft = Offset(headCenter.x - 14f * unit, headCenter.y - 21f * unit),
                        size = Size(28f * unit, 10f * unit),
                        cornerRadius = CornerRadius(4f * unit, 4f * unit),
                    )
                    drawRoundRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(headCenter.x + 2f * unit, headCenter.y - 13f * unit),
                        size = Size(16f * unit, 3.5f * unit),
                        cornerRadius = CornerRadius(2f * unit, 2f * unit),
                    )
                }
                HeadwearStyle.SUN_HAT -> {
                    drawOval(
                        color = Color(0xFFD6B47B),
                        topLeft = Offset(headCenter.x - 21f * unit, headCenter.y - 16f * unit),
                        size = Size(42f * unit, 7f * unit),
                    )
                    drawRoundRect(
                        color = Color(0xFFC29B59),
                        topLeft = Offset(headCenter.x - 12f * unit, headCenter.y - 24f * unit),
                        size = Size(24f * unit, 10f * unit),
                        cornerRadius = CornerRadius(4f * unit, 4f * unit),
                    )
                }
                else -> Unit
            }

            // Friendly eyes
            val eyeY = headCenter.y - 2f * unit
            listOf(headCenter.x + 1f * unit, headCenter.x + 9f * unit).forEach { ex ->
                drawCircle(color = Color.White, radius = 3f * unit, center = Offset(ex, eyeY))
                drawCircle(color = Color(0xFF2B211E), radius = 1.9f * unit, center = Offset(ex + 0.5f * unit, eyeY))
            }

            // Beard if elder/adult male character has one
            if (appearance.hasBeard) {
                val beardColor = if (appearance.headwear == HeadwearStyle.TURBAN_LIGHT || appearance.headwear == HeadwearStyle.SOUTHERN_CAP) {
                    Color(0xFFD1D5DB)
                } else {
                    Color(0xFF3E2723)
                }
                val beard = Path().apply {
                    moveTo(headCenter.x - 8f * unit, headCenter.y + 6f * unit)
                    cubicTo(
                        headCenter.x - 6f * unit, headCenter.y + 20f * unit,
                        headCenter.x + 12f * unit, headCenter.y + 20f * unit,
                        headCenter.x + 13f * unit, headCenter.y + 6f * unit,
                    )
                    close()
                }
                drawPath(beard, color = beardColor)
            }

            // Lip-synced mouth
            val mouthCenter = Offset(headCenter.x + 5f * unit, headCenter.y + 8f * unit)
            if (isSpeaking && mouthOpenLevel > 0.1f) {
                val mh = (2.2f + mouthOpenLevel * 4.2f) * unit
                val mw = 6.2f * unit
                drawOval(
                    color = Color(0xFF7F1D1D),
                    topLeft = Offset(mouthCenter.x - mw * 0.5f, mouthCenter.y - mh * 0.5f),
                    size = Size(mw, mh),
                )
            } else {
                drawArc(
                    color = Color(0xFF5D4037),
                    startAngle = 15f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(mouthCenter.x - 4.5f * unit, mouthCenter.y - 2.5f * unit),
                    size = Size(9f * unit, 4.5f * unit),
                    style = Stroke(width = 1.8f * unit, cap = StrokeCap.Round),
                )
            }
        }
    }
}
