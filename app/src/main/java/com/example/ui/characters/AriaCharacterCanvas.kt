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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import com.example.model.AriaAnimationState
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 2D Cartoon skeletal-vector renderer for Aria (15-year-old Iranian explorer)
 * supporting all 14 animation states and real-time speech lip-sync.
 */
@Composable
fun AriaCharacterCanvas(
    animationState: AriaAnimationState,
    facingRight: Boolean = true,
    mouthOpenLevel: Float = 0f,
    modifier: Modifier = Modifier,
) {
    val infinite = rememberInfiniteTransition(label = "aria_anim")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "aria_phase",
    )
    val fastPhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 620, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "aria_fast_phase",
    )
    val blinkCycle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "aria_blink",
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val unit = minOf(w, h) / 140f

        val isBlinking = blinkCycle > 0.94f

        val jumpLift = when (animationState) {
            AriaAnimationState.JUMP -> abs(sin(fastPhase)) * 18f * unit
            AriaAnimationState.HAPPY, AriaAnimationState.RECEIVE_REWARD -> abs(sin(fastPhase * 0.8f)) * 7f * unit
            AriaAnimationState.SURPRISED -> abs(sin(fastPhase * 1.1f)) * 5f * unit
            else -> 0f
        }
        val bobY = when (animationState) {
            AriaAnimationState.IDLE -> sin(phase) * 1.4f * unit
            AriaAnimationState.WALK -> abs(sin(fastPhase)) * -3.5f * unit
            AriaAnimationState.RUN -> abs(sin(fastPhase * 1.35f)) * -5.5f * unit
            AriaAnimationState.SAD -> 3f * unit + sin(phase * 0.5f) * 1.2f * unit
            else -> sin(phase) * 1.0f * unit
        }

        // Ground shadow
        val shadowScale = (1f - jumpLift / (40f * unit)).coerceIn(0.45f, 1f)
        drawOval(
            color = Color.Black.copy(alpha = 0.20f * shadowScale),
            topLeft = Offset(w * 0.5f - 28f * unit * shadowScale, h * 0.89f),
            size = Size(56f * unit * shadowScale, 11f * unit * shadowScale),
        )

        val flipX = if (facingRight) 1f else -1f
        scale(scaleX = flipX, scaleY = 1f, pivot = Offset(w * 0.5f, h * 0.5f)) {
            val cx = w * 0.5f
            val baseY = h * 0.89f - jumpLift + bobY

            val bodyLeanDeg = when (animationState) {
                AriaAnimationState.RUN -> 11f
                AriaAnimationState.WALK -> 4f
                AriaAnimationState.SAD -> -5f
                AriaAnimationState.PICK_UP -> 18f
                AriaAnimationState.THINK -> -3f
                else -> 0f
            }

            // Running dust puffs
            if (animationState == AriaAnimationState.RUN || animationState == AriaAnimationState.WALK) {
                val dustAlpha = if (animationState == AriaAnimationState.RUN) 0.45f else 0.22f
                for (i in 0..2) {
                    val dx = -18f * unit - i * 9f * unit - (abs(sin(fastPhase + i)) * 6f * unit)
                    val dy = baseY - (2f + i * 2.5f) * unit
                    drawCircle(
                        color = Color(0xFFEBD9B4).copy(alpha = dustAlpha * (1f - i * 0.25f)),
                        radius = (4.5f - i) * unit,
                        center = Offset(cx + dx, dy),
                    )
                }
            }

            rotate(degrees = bodyLeanDeg, pivot = Offset(cx, baseY - 35f * unit)) {
                drawAriaLegs(cx, baseY, unit, animationState, fastPhase)
                drawAriaBackpack(cx, baseY, unit, animationState)
                drawAriaTorso(cx, baseY, unit, animationState)
                drawAriaHead(
                    cx = cx,
                    baseY = baseY,
                    unit = unit,
                    state = animationState,
                    phase = phase,
                    isBlinking = isBlinking,
                    mouthOpen = mouthOpenLevel,
                )
                drawAriaArmsAndProps(cx, baseY, unit, animationState, phase, fastPhase)
            }

            drawAriaStateEffects(cx, baseY, unit, animationState, phase, fastPhase)
        }
    }
}

private fun DrawScope.drawAriaLegs(
    cx: Float,
    baseY: Float,
    unit: Float,
    state: AriaAnimationState,
    fastPhase: Float,
) {
    val legSwing = when (state) {
        AriaAnimationState.WALK -> sin(fastPhase) * 22f
        AriaAnimationState.RUN -> sin(fastPhase * 1.35f) * 38f
        AriaAnimationState.JUMP -> 18f
        else -> 0f
    }
    val pantsColor = Color(0xFF2B3A4A)
    val bootColor = Color(0xFF6D4C41)
    val bootSole = Color(0xFF3E2723)

    // Back leg
    rotate(degrees = -legSwing, pivot = Offset(cx - 4f * unit, baseY - 36f * unit)) {
        drawRoundRect(
            color = pantsColor.copy(alpha = 0.88f),
            topLeft = Offset(cx - 12f * unit, baseY - 36f * unit),
            size = Size(11f * unit, 28f * unit),
            cornerRadius = CornerRadius(5f * unit, 5f * unit),
        )
        drawRoundRect(
            color = bootColor,
            topLeft = Offset(cx - 13f * unit, baseY - 11f * unit),
            size = Size(16f * unit, 9f * unit),
            cornerRadius = CornerRadius(4f * unit, 4f * unit),
        )
        drawRect(
            color = bootSole,
            topLeft = Offset(cx - 13f * unit, baseY - 4f * unit),
            size = Size(16f * unit, 3f * unit),
        )
    }

    // Front leg
    rotate(degrees = legSwing, pivot = Offset(cx + 5f * unit, baseY - 36f * unit)) {
        drawRoundRect(
            color = pantsColor,
            topLeft = Offset(cx + 1f * unit, baseY - 36f * unit),
            size = Size(11f * unit, 28f * unit),
            cornerRadius = CornerRadius(5f * unit, 5f * unit),
        )
        drawRoundRect(
            color = bootColor,
            topLeft = Offset(cx + 0f * unit, baseY - 11f * unit),
            size = Size(16f * unit, 9f * unit),
            cornerRadius = CornerRadius(4f * unit, 4f * unit),
        )
        drawRect(
            color = bootSole,
            topLeft = Offset(cx + 0f * unit, baseY - 4f * unit),
            size = Size(16f * unit, 3f * unit),
        )
    }
}

private fun DrawScope.drawAriaBackpack(
    cx: Float,
    baseY: Float,
    unit: Float,
    state: AriaAnimationState,
) {
    // Explorer backpack with rolled map scroll on top
    val packColor = Color(0xFF8D5B4C)
    val packDark = Color(0xFF5D3A2E)
    val mapScroll = Color(0xFFF6E3BA)

    // Rolled small map peeking out of backpack
    rotate(degrees = -15f, pivot = Offset(cx - 18f * unit, baseY - 68f * unit)) {
        drawRoundRect(
            color = mapScroll,
            topLeft = Offset(cx - 26f * unit, baseY - 76f * unit),
            size = Size(15f * unit, 7f * unit),
            cornerRadius = CornerRadius(3f * unit, 3f * unit),
        )
        drawCircle(
            color = Color(0xFFC8A96E),
            radius = 3.2f * unit,
            center = Offset(cx - 11f * unit, baseY - 72.5f * unit),
        )
    }

    drawRoundRect(
        color = packColor,
        topLeft = Offset(cx - 25f * unit, baseY - 68f * unit),
        size = Size(17f * unit, 30f * unit),
        cornerRadius = CornerRadius(7f * unit, 7f * unit),
    )
    drawRoundRect(
        color = packDark,
        topLeft = Offset(cx - 23f * unit, baseY - 52f * unit),
        size = Size(12f * unit, 11f * unit),
        cornerRadius = CornerRadius(4f * unit, 4f * unit),
    )
    if (state != AriaAnimationState.OPEN_NOTEBOOK) {
        // Grandpa's small notebook tucked on backpack side
        drawRoundRect(
            color = Color(0xFF9E2A2B),
            topLeft = Offset(cx - 22f * unit, baseY - 65f * unit),
            size = Size(9f * unit, 11f * unit),
            cornerRadius = CornerRadius(2f * unit, 2f * unit),
        )
    }
}

private fun DrawScope.drawAriaTorso(
    cx: Float,
    baseY: Float,
    unit: Float,
    state: AriaAnimationState,
) {
    // Cream shirt + Turquoise expedition vest + Golden compass pin
    val shirtColor = Color(0xFFFFF8E8)
    val vestColor = Color(0xFF0E7490)
    val vestHighlight = Color(0xFF22B8CF)
    val beltColor = Color(0xFF5D4037)

    // Inner cream shirt
    drawRoundRect(
        color = shirtColor,
        topLeft = Offset(cx - 13f * unit, baseY - 70f * unit),
        size = Size(26f * unit, 35f * unit),
        cornerRadius = CornerRadius(9f * unit, 9f * unit),
    )
    // Turquoise adventurer vest
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(vestHighlight, vestColor)),
        topLeft = Offset(cx - 14f * unit, baseY - 70f * unit),
        size = Size(28f * unit, 33f * unit),
        cornerRadius = CornerRadius(8f * unit, 8f * unit),
    )
    // Shirt collar V-neck
    val collar = Path().apply {
        moveTo(cx - 4f * unit, baseY - 70f * unit)
        lineTo(cx + 6f * unit, baseY - 70f * unit)
        lineTo(cx + 2f * unit, baseY - 57f * unit)
        close()
    }
    drawPath(collar, color = shirtColor)

    // Belt + brass buckle
    drawRect(
        color = beltColor,
        topLeft = Offset(cx - 13.5f * unit, baseY - 39f * unit),
        size = Size(27f * unit, 4.5f * unit),
    )
    drawRoundRect(
        color = Color(0xFFF6C453),
        topLeft = Offset(cx - 1f * unit, baseY - 39.5f * unit),
        size = Size(6.5f * unit, 5.5f * unit),
        cornerRadius = CornerRadius(1.5f * unit, 1.5f * unit),
    )
    // Golden explorer compass emblem on vest
    drawCircle(
        color = Color(0xFFF6C453),
        radius = 3.4f * unit,
        center = Offset(cx + 8f * unit, baseY - 56f * unit),
    )
    if (state == AriaAnimationState.IDLE) {
        drawCircle(
            color = Color(0xFF0F4C5C),
            radius = 1.6f * unit,
            center = Offset(cx + 8f * unit, baseY - 56f * unit),
        )
    }
}

private fun DrawScope.drawAriaHead(
    cx: Float,
    baseY: Float,
    unit: Float,
    state: AriaAnimationState,
    phase: Float,
    isBlinking: Boolean,
    mouthOpen: Float,
) {
    val headShiftX = when (state) {
        AriaAnimationState.LOOK_AROUND -> sin(phase * 1.4f) * 4f * unit
        else -> 0f
    }
    val headTilt = when (state) {
        AriaAnimationState.THINK -> -8f
        AriaAnimationState.SAD -> 10f
        AriaAnimationState.SURPRISED -> -5f
        else -> 0f
    }
    val headCenter = Offset(cx + 1.5f * unit + headShiftX, baseY - 87f * unit)
    val skin = Color(0xFFE5B583)
    val skinShadow = Color(0xFFD29B65)
    val hairDark = Color(0xFF231B18)
    val hairHighlight = Color(0xFF3E302A)

    rotate(degrees = headTilt, pivot = Offset(cx, baseY - 70f * unit)) {
        // Neck
        drawRoundRect(
            color = skinShadow,
            topLeft = Offset(cx - 4f * unit, baseY - 76f * unit),
            size = Size(9f * unit, 10f * unit),
            cornerRadius = CornerRadius(4f * unit, 4f * unit),
        )

        // Ear
        drawCircle(
            color = skinShadow,
            radius = 4.2f * unit,
            center = Offset(headCenter.x - 14f * unit, headCenter.y + 1f * unit),
        )

        // Face oval
        drawOval(
            color = skin,
            topLeft = Offset(headCenter.x - 14.5f * unit, headCenter.y - 16f * unit),
            size = Size(29f * unit, 32f * unit),
        )

        // Soft cheek blush
        drawOval(
            color = Color(0xFFE57373).copy(alpha = 0.25f),
            topLeft = Offset(headCenter.x + 4f * unit, headCenter.y + 2f * unit),
            size = Size(6.5f * unit, 3.8f * unit),
        )

        // Stylish Iranian teenager wavy hair
        val hairPath = Path().apply {
            moveTo(headCenter.x - 15.5f * unit, headCenter.y - 2f * unit)
            cubicTo(
                headCenter.x - 18f * unit, headCenter.y - 20f * unit,
                headCenter.x - 4f * unit, headCenter.y - 24f * unit,
                headCenter.x + 12f * unit, headCenter.y - 17f * unit,
            )
            cubicTo(
                headCenter.x + 18f * unit, headCenter.y - 13f * unit,
                headCenter.x + 15f * unit, headCenter.y - 6f * unit,
                headCenter.x + 8f * unit, headCenter.y - 7f * unit,
            )
            cubicTo(
                headCenter.x + 1f * unit, headCenter.y - 10f * unit,
                headCenter.x - 8f * unit, headCenter.y - 7f * unit,
                headCenter.x - 11f * unit, headCenter.y + 1f * unit,
            )
            close()
        }
        drawPath(hairPath, color = hairDark)
        // Hair highlight swoop
        drawArc(
            color = hairHighlight,
            startAngle = 200f,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = Offset(headCenter.x - 10f * unit, headCenter.y - 19f * unit),
            size = Size(19f * unit, 10f * unit),
            style = Stroke(width = 2.4f * unit, cap = StrokeCap.Round),
        )

        // Eyes
        val lookOffset = if (state == AriaAnimationState.LOOK_AROUND) cos(phase * 1.4f) * 2.0f * unit else 0f
        val leftEyeCenter = Offset(headCenter.x + 0.5f * unit + lookOffset, headCenter.y - 2f * unit)
        val rightEyeCenter = Offset(headCenter.x + 9.5f * unit + lookOffset, headCenter.y - 2f * unit)

        if (isBlinking) {
            drawLine(
                color = hairDark,
                start = Offset(leftEyeCenter.x - 2.5f * unit, leftEyeCenter.y),
                end = Offset(leftEyeCenter.x + 2.5f * unit, leftEyeCenter.y),
                strokeWidth = 1.8f * unit,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = hairDark,
                start = Offset(rightEyeCenter.x - 2.5f * unit, rightEyeCenter.y),
                end = Offset(rightEyeCenter.x + 2.5f * unit, rightEyeCenter.y),
                strokeWidth = 1.8f * unit,
                cap = StrokeCap.Round,
            )
        } else {
            val eyeScale = if (state == AriaAnimationState.SURPRISED) 1.32f else 1f
            listOf(leftEyeCenter, rightEyeCenter).forEach { eyePos ->
                drawCircle(color = Color.White, radius = 3.2f * unit * eyeScale, center = eyePos)
                drawCircle(color = Color(0xFF3E2723), radius = 2.1f * unit * eyeScale, center = Offset(eyePos.x + 0.5f * unit, eyePos.y))
                drawCircle(color = Color.White, radius = 0.8f * unit, center = Offset(eyePos.x + 1.1f * unit, eyePos.y - 0.8f * unit))
            }
        }

        // Expressive eyebrows
        val browOffsetY = when (state) {
            AriaAnimationState.SURPRISED -> -2.2f * unit
            AriaAnimationState.SAD -> 1.2f * unit
            else -> 0f
        }
        val browAngle = when (state) {
            AriaAnimationState.SAD -> -14f
            AriaAnimationState.THINK -> 8f
            else -> 0f
        }
        rotate(degrees = browAngle, pivot = leftEyeCenter) {
            drawLine(
                color = hairDark,
                start = Offset(leftEyeCenter.x - 2.6f * unit, leftEyeCenter.y - 5.2f * unit + browOffsetY),
                end = Offset(leftEyeCenter.x + 2.8f * unit, leftEyeCenter.y - 5.6f * unit + browOffsetY),
                strokeWidth = 1.7f * unit,
                cap = StrokeCap.Round,
            )
        }
        rotate(degrees = -browAngle, pivot = rightEyeCenter) {
            drawLine(
                color = hairDark,
                start = Offset(rightEyeCenter.x - 2.4f * unit, rightEyeCenter.y - 5.6f * unit + browOffsetY),
                end = Offset(rightEyeCenter.x + 2.6f * unit, rightEyeCenter.y - 5.0f * unit + browOffsetY),
                strokeWidth = 1.7f * unit,
                cap = StrokeCap.Round,
            )
        }

        // Mouth with real-time speech lip-sync and emotional shapes
        val mouthCenter = Offset(headCenter.x + 5.2f * unit, headCenter.y + 7.5f * unit)
        val activeOpen = if (state == AriaAnimationState.TALK) {
            mouthOpen.coerceIn(0.15f, 1f)
        } else {
            mouthOpen.coerceIn(0f, 1f)
        }

        when {
            activeOpen > 0.12f -> {
                val mh = (2.5f + activeOpen * 4.5f) * unit
                val mw = (5.5f + (1f - activeOpen * 0.25f) * 2.5f) * unit
                drawOval(
                    color = Color(0xFF7F1D1D),
                    topLeft = Offset(mouthCenter.x - mw * 0.5f, mouthCenter.y - mh * 0.5f),
                    size = Size(mw, mh),
                )
                drawOval(
                    color = Color(0xFFFB7185),
                    topLeft = Offset(mouthCenter.x - mw * 0.25f, mouthCenter.y + mh * 0.05f),
                    size = Size(mw * 0.5f, mh * 0.36f),
                )
            }
            state == AriaAnimationState.SURPRISED -> {
                drawCircle(
                    color = Color(0xFF7F1D1D),
                    radius = 3.2f * unit,
                    center = mouthCenter,
                )
            }
            state == AriaAnimationState.SAD -> {
                drawArc(
                    color = Color(0xFF5D4037),
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(mouthCenter.x - 4.5f * unit, mouthCenter.y - 0.5f * unit),
                    size = Size(9f * unit, 5f * unit),
                    style = Stroke(width = 1.8f * unit, cap = StrokeCap.Round),
                )
            }
            state == AriaAnimationState.HAPPY || state == AriaAnimationState.RECEIVE_REWARD || state == AriaAnimationState.DISCOVER_CLUE -> {
                drawArc(
                    color = Color(0xFF7F1D1D),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(mouthCenter.x - 5.5f * unit, mouthCenter.y - 2.5f * unit),
                    size = Size(11f * unit, 7f * unit),
                )
            }
            else -> {
                drawArc(
                    color = Color(0xFF5D4037),
                    startAngle = 15f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(mouthCenter.x - 4.5f * unit, mouthCenter.y - 2.5f * unit),
                    size = Size(9f * unit, 5f * unit),
                    style = Stroke(width = 1.8f * unit, cap = StrokeCap.Round),
                )
            }
        }
    }
}

private fun DrawScope.drawAriaArmsAndProps(
    cx: Float,
    baseY: Float,
    unit: Float,
    state: AriaAnimationState,
    phase: Float,
    fastPhase: Float,
) {
    val sleeveColor = Color(0xFFFDF3DC)
    val skin = Color(0xFFE5B583)

    val frontArmAngle = when (state) {
        AriaAnimationState.WALK -> -sin(fastPhase) * 26f
        AriaAnimationState.RUN -> -sin(fastPhase * 1.35f) * 42f
        AriaAnimationState.JUMP -> -125f
        AriaAnimationState.HAPPY, AriaAnimationState.RECEIVE_REWARD -> -135f + sin(fastPhase) * 12f
        AriaAnimationState.DISCOVER_CLUE -> -110f
        AriaAnimationState.THINK -> -105f
        AriaAnimationState.OPEN_NOTEBOOK -> -58f
        AriaAnimationState.PICK_UP -> -35f
        AriaAnimationState.TALK -> -25f + sin(fastPhase) * 15f
        AriaAnimationState.SAD -> 10f
        else -> sin(phase) * 4f
    }

    val shoulder = Offset(cx + 4f * unit, baseY - 64f * unit)
    rotate(degrees = frontArmAngle, pivot = shoulder) {
        drawRoundRect(
            color = sleeveColor,
            topLeft = Offset(shoulder.x - 4f * unit, shoulder.y),
            size = Size(8.5f * unit, 19f * unit),
            cornerRadius = CornerRadius(4f * unit, 4f * unit),
        )
        drawCircle(
            color = skin,
            radius = 4.2f * unit,
            center = Offset(shoulder.x + 0.2f * unit, shoulder.y + 21f * unit),
        )
    }

    // State-specific props held by Aria
    when (state) {
        AriaAnimationState.OPEN_NOTEBOOK -> {
            // Grandpa's open glowing adventure notebook in Aria's hands
            drawRoundRect(
                color = Color(0xFF795548),
                topLeft = Offset(cx + 12f * unit, baseY - 64f * unit),
                size = Size(20f * unit, 15f * unit),
                cornerRadius = CornerRadius(2.5f * unit, 2.5f * unit),
            )
            drawRoundRect(
                color = Color(0xFFFFF8E8),
                topLeft = Offset(cx + 13.5f * unit, baseY - 62.5f * unit),
                size = Size(17f * unit, 12f * unit),
                cornerRadius = CornerRadius(1.5f * unit, 1.5f * unit),
            )
            drawLine(
                color = Color(0xFFC8A96E),
                start = Offset(cx + 22f * unit, baseY - 62f * unit),
                end = Offset(cx + 22f * unit, baseY - 51f * unit),
                strokeWidth = 1.4f * unit,
            )
        }
        AriaAnimationState.DISCOVER_CLUE -> {
            // Glowing golden clue crystal held up
            val glowCenter = Offset(cx + 21f * unit, baseY - 82f * unit)
            drawCircle(
                color = Color(0xFFF6C453).copy(alpha = 0.35f),
                radius = 12f * unit,
                center = glowCenter,
            )
            val gem = Path().apply {
                moveTo(glowCenter.x, glowCenter.y - 7f * unit)
                lineTo(glowCenter.x + 6f * unit, glowCenter.y)
                lineTo(glowCenter.x, glowCenter.y + 7f * unit)
                lineTo(glowCenter.x - 6f * unit, glowCenter.y)
                close()
            }
            drawPath(gem, color = Color(0xFFF6C453))
        }
        AriaAnimationState.RECEIVE_REWARD -> {
            // Golden treasure shard with sunbeam rays
            val rewardCenter = Offset(cx + 14f * unit, baseY - 102f * unit)
            drawCircle(
                color = Color(0xFFF6C453).copy(alpha = 0.42f),
                radius = 16f * unit,
                center = rewardCenter,
            )
            drawCircle(
                color = Color(0xFF22B8CF),
                radius = 7.5f * unit,
                center = rewardCenter,
            )
            drawCircle(
                color = Color(0xFFF6C453),
                radius = 7.5f * unit,
                center = rewardCenter,
                style = Stroke(width = 2.2f * unit),
            )
        }
        else -> Unit
    }
}

private fun DrawScope.drawAriaStateEffects(
    cx: Float,
    baseY: Float,
    unit: Float,
    state: AriaAnimationState,
    phase: Float,
    fastPhase: Float,
) {
    when (state) {
        AriaAnimationState.THINK -> {
            // Floating thought bubbles
            val drift = sin(phase) * 2f * unit
            drawCircle(color = Color.White.copy(alpha = 0.88f), radius = 2.5f * unit, center = Offset(cx + 22f * unit, baseY - 100f * unit + drift))
            drawCircle(color = Color.White.copy(alpha = 0.92f), radius = 4.2f * unit, center = Offset(cx + 28f * unit, baseY - 108f * unit + drift))
        }
        AriaAnimationState.SURPRISED -> {
            // Geometric exclamation mark bar
            drawRoundRect(
                color = Color(0xFFF6C453),
                topLeft = Offset(cx + 16f * unit, baseY - 118f * unit),
                size = Size(4f * unit, 12f * unit),
                cornerRadius = CornerRadius(2f * unit, 2f * unit),
            )
            drawCircle(
                color = Color(0xFFF6C453),
                radius = 2.3f * unit,
                center = Offset(cx + 18f * unit, baseY - 102f * unit),
            )
        }
        AriaAnimationState.HAPPY, AriaAnimationState.DISCOVER_CLUE, AriaAnimationState.RECEIVE_REWARD -> {
            // Sparkle stars around Aria
            for (i in 0..3) {
                val angle = fastPhase * 0.6f + i * (PI.toFloat() / 2f)
                val sx = cx + cos(angle) * 28f * unit
                val sy = baseY - 82f * unit + sin(angle) * 20f * unit
                drawCircle(
                    color = Color(0xFFF6C453).copy(alpha = 0.85f),
                    radius = 2.6f * unit,
                    center = Offset(sx, sy),
                )
            }
        }
        else -> Unit
    }
}
