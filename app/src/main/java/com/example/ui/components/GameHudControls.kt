package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.model.CountyDefinition
import com.example.model.CountyProgressState
import com.example.ui.stage.WORLD_MAX_X
import com.example.ui.stage.WORLD_MIN_X
import com.example.ui.theme.DeepGulfTeal
import com.example.ui.theme.GulfTurquoise
import com.example.ui.theme.OasisGreen
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentCream
import com.example.ui.theme.SunlitGold
import com.example.ui.theme.WarmAmberGold
import com.example.ui.theme.WarmSepiaInk
import com.example.util.toPersianNumber
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

enum class NavControllerMode {
    ANALOG,
    DPAD,
}

/**
 * Upgraded dual-mode left-thumb character navigation controller:
 * - Instant touch-anywhere analog compass pad (immediate press & drag response, sprint outer ring)
 * - Switchable tactile Left/Right D-Pad buttons for precision walking
 */
@Composable
fun CharacterNavigationController(
    onMoveChanged: (horizontalAxis: Float, fastRun: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var mode by remember { mutableStateOf(NavControllerMode.ANALOG) }
    var activeAxis by remember { mutableFloatStateOf(0f) }
    var activeFast by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Mode switch + live motion telemetry badge
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = DeepGulfTeal.copy(alpha = 0.78f),
            border = androidx.compose.foundation.BorderStroke(1.dp, ParchmentCream.copy(alpha = 0.45f)),
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .clickable {
                    activeAxis = 0f
                    activeFast = false
                    onMoveChanged(0f, false)
                    mode = if (mode == NavControllerMode.ANALOG) NavControllerMode.DPAD else NavControllerMode.ANALOG
                }
                .testTag("controller_mode_toggle"),
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Gamepad,
                        contentDescription = "تغییر نوع کنترلگر",
                        tint = SunlitGold,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = when {
                            abs(activeAxis) > 0.1f && activeFast -> "دویدن"
                            abs(activeAxis) > 0.1f -> "حرکت"
                            mode == NavControllerMode.ANALOG -> "آنالوگ ⇄ دکمه‌ای"
                            else -> "دکمه‌ای ⇄ آنالوگ"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = ParchmentCream,
                    )
                }
            }
        }

        if (mode == NavControllerMode.ANALOG) {
            TouchResponsiveAnalogPad(
                onMoveChanged = { axis, fast ->
                    activeAxis = axis
                    activeFast = fast
                    onMoveChanged(axis, fast)
                },
            )
        } else {
            DirectionalButtonPad(
                onMoveChanged = { axis, fast ->
                    activeAxis = axis
                    activeFast = fast
                    onMoveChanged(axis, fast)
                },
            )
        }
    }
}

@Composable
private fun TouchResponsiveAnalogPad(
    onMoveChanged: (horizontalAxis: Float, fastRun: Boolean) -> Unit,
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    var isPressed by remember { mutableStateOf(false) }

    val animatedX by animateFloatAsState(
        targetValue = knobOffset.x,
        animationSpec = if (isPressed) tween(0) else spring(dampingRatio = 0.65f, stiffness = 450f),
        label = "joy_x",
    )
    val animatedY by animateFloatAsState(
        targetValue = knobOffset.y,
        animationSpec = if (isPressed) tween(0) else spring(dampingRatio = 0.65f, stiffness = 450f),
        label = "joy_y",
    )

    Box(
        modifier = Modifier
            .size(144.dp)
            .pointerInput(Unit) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadiusPx = size.width * 0.34f
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    isPressed = true

                    fun updateFromPosition(pos: Offset) {
                        val delta = pos - center
                        val dist = hypot(delta.x, delta.y)
                        val clamped = if (dist > maxRadiusPx && dist > 0f) {
                            Offset(delta.x * maxRadiusPx / dist, delta.y * maxRadiusPx / dist)
                        } else {
                            delta
                        }
                        knobOffset = clamped
                        val rawAxis = (clamped.x / maxRadiusPx).coerceIn(-1f, 1f)
                        val axisX = if (abs(rawAxis) < 0.12f) 0f else rawAxis
                        val isFast = abs(rawAxis) > 0.72f
                        onMoveChanged(axisX, isFast)
                    }

                    updateFromPosition(down.position)

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                        if (change == null || !change.pressed) {
                            break
                        }
                        change.consume()
                        updateFromPosition(change.position)
                    }

                    isPressed = false
                    knobOffset = Offset.Zero
                    onMoveChanged(0f, false)
                }
            }
            .testTag("virtual_joystick"),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerR = size.minDimension * 0.45f
            val axisNorm = (animatedX / (size.width * 0.34f)).coerceIn(-1f, 1f)

            // Outer translucent compass dial
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(DeepGulfTeal.copy(alpha = 0.56f), DeepGulfTeal.copy(alpha = 0.34f)),
                    center = center,
                    radius = outerR,
                ),
                radius = outerR,
                center = center,
            )

            // Sprint threshold dashed/glowing ring
            drawCircle(
                color = if (abs(axisNorm) > 0.72f) SunlitGold else ParchmentCream.copy(alpha = 0.55f),
                radius = outerR,
                center = center,
                style = Stroke(width = if (abs(axisNorm) > 0.72f) 3.5.dp.toPx() else 2.2.dp.toPx()),
            )
            drawCircle(
                color = ParchmentCream.copy(alpha = 0.22f),
                radius = outerR * 0.68f,
                center = center,
                style = Stroke(width = 1.2.dp.toPx()),
            )

            // Left & Right directional chevron cues inside pad
            val leftActive = axisNorm < -0.15f
            val rightActive = axisNorm > 0.15f
            val chevronY = center.y
            val chevronR = outerR * 0.66f

            // Left chevron
            val leftPath = Path().apply {
                moveTo(center.x - chevronR + 9f, chevronY - 9f)
                lineTo(center.x - chevronR - 3f, chevronY)
                lineTo(center.x - chevronR + 9f, chevronY + 9f)
            }
            drawPath(
                leftPath,
                color = if (leftActive) SunlitGold else ParchmentCream.copy(alpha = 0.65f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )

            // Right chevron
            val rightPath = Path().apply {
                moveTo(center.x + chevronR - 9f, chevronY - 9f)
                lineTo(center.x + chevronR + 3f, chevronY)
                lineTo(center.x + chevronR - 9f, chevronY + 9f)
            }
            drawPath(
                rightPath,
                color = if (rightActive) SunlitGold else ParchmentCream.copy(alpha = 0.65f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )

            // Thumb knob with highlight
            val knobPos = center + Offset(animatedX, animatedY)
            drawCircle(
                color = Color.Black.copy(alpha = 0.22f),
                radius = outerR * 0.41f,
                center = knobPos + Offset(0f, 3f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFFFF3B0), SunlitGold, WarmAmberGold),
                    center = knobPos - Offset(6f, 6f),
                    radius = outerR * 0.45f,
                ),
                radius = outerR * 0.40f,
                center = knobPos,
            )
            drawCircle(
                color = WarmSepiaInk.copy(alpha = 0.30f),
                radius = outerR * 0.31f,
                center = knobPos,
                style = Stroke(width = 1.8.dp.toPx()),
            )
        }
    }
}

@Composable
private fun DirectionalButtonPad(
    onMoveChanged: (horizontalAxis: Float, fastRun: Boolean) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.testTag("dpad_controller"),
    ) {
        HoldDirectionButton(
            isLeft = true,
            onHoldChanged = { held -> onMoveChanged(if (held) -1f else 0f, false) },
            testTag = "dpad_left_button",
        )
        HoldDirectionButton(
            isLeft = false,
            onHoldChanged = { held -> onMoveChanged(if (held) 1f else 0f, false) },
            testTag = "dpad_right_button",
        )
    }
}

@Composable
private fun HoldDirectionButton(
    isLeft: Boolean,
    onHoldChanged: (Boolean) -> Unit,
    testTag: String,
) {
    var held by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (held) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "dpad_btn_scale",
    )
    val bgColor by animateColorAsState(
        targetValue = if (held) SunlitGold else DeepGulfTeal.copy(alpha = 0.72f),
        label = "dpad_btn_bg",
    )

    Surface(
        shape = CircleShape,
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            if (held) WarmSepiaInk else ParchmentCream.copy(alpha = 0.75f),
        ),
        shadowElevation = if (held) 2.dp else 8.dp,
        modifier = Modifier
            .size(64.dp)
            .scale(scale)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    held = true
                    onHoldChanged(true)
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                        if (change == null || !change.pressed) break
                    }
                    held = false
                    onHoldChanged(false)
                }
            }
            .testTag(testTag),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (isLeft) Icons.Rounded.ArrowBack else Icons.Rounded.ArrowForward,
                contentDescription = if (isLeft) "حرکت به چپ" else "حرکت به راست",
                tint = if (held) WarmSepiaInk else ParchmentCream,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

/**
 * Top-center compact world radar & directional waypoint guide so players always see
 * where Aria is relative to the local character and the 3 missions.
 */
@Composable
fun StageNavigatorCompassBar(
    county: CountyDefinition,
    progress: CountyProgressState,
    playerX: Float,
    onGuideToWorldX: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val nextPendingMission = county.missions.firstOrNull { it.id !in progress.completedMissionIds }
    val targetWorldX = when {
        !progress.npcMet -> county.npcWorldX
        nextPendingMission != null -> nextPendingMission.worldX
        else -> county.npcWorldX
    }
    val deltaX = targetWorldX - playerX
    val directionHint = when {
        abs(deltaX) < 110f && nextPendingMission != null -> "نزدیک «${nextPendingMission.title}» هستید؛ دکمه تعامل را بزنید"
        abs(deltaX) < 110f && !progress.npcMet -> "نزدیک ${county.npcName} هستید"
        deltaX > 110f && nextPendingMission != null -> "مسیر بعدی (${nextPendingMission.title}): حرکت به راست ▶"
        deltaX < -110f && nextPendingMission != null -> "◀ مسیر بعدی (${nextPendingMission.title}): حرکت به چپ"
        progress.isCompleted -> "تمام مأموریت‌های ${county.nameFa} کامل شد!"
        else -> "برای گفتگو یا مأموریت در مسیر حرکت کنید"
    }

    Column(
        modifier = modifier.widthIn(max = 360.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // World trail mini-radar (physical Left->Right world coordinates)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = DeepGulfTeal.copy(alpha = 0.72f),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, ParchmentCream.copy(alpha = 0.55f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    // Track line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(ParchmentCream.copy(alpha = 0.30f)),
                    )

                    val span = (WORLD_MAX_X - WORLD_MIN_X).coerceAtLeast(1f)
                    val ariaFrac = ((playerX - WORLD_MIN_X) / span).coerceIn(0f, 1f)
                    val npcFrac = ((county.npcWorldX - WORLD_MIN_X) / span).coerceIn(0f, 1f)

                    // NPC marker
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(npcFrac.coerceAtLeast(0.02f))
                            .height(22.dp),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(ParchmentCream)
                                .border(1.5.dp, WarmAmberGold, CircleShape)
                                .clickable { onGuideToWorldX(county.npcWorldX) },
                        )
                    }

                    // 3 Mission markers
                    county.missions.forEach { m ->
                        val frac = ((m.worldX - WORLD_MIN_X) / span).coerceIn(0.05f, 1f)
                        val done = m.id in progress.completedMissionIds
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(frac)
                                .height(24.dp),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (done) OasisGreen else SunlitGold,
                                border = androidx.compose.foundation.BorderStroke(1.dp, WarmSepiaInk),
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { onGuideToWorldX(m.worldX) },
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (done) "✓" else m.orderIndex.toPersianNumber(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (done) Color.White else WarmSepiaInk,
                                    )
                                }
                            }
                        }
                    }

                    // Aria's live dot
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ariaFrac.coerceAtLeast(0.02f))
                            .height(26.dp),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(GulfTurquoise)
                                .border(2.dp, Color.White, CircleShape),
                        )
                    }
                }
            }
        }

        // Compact Objective / Direction Hint Pill
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ParchmentCream.copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(1.dp, ParchmentBorder),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Explore,
                        contentDescription = null,
                        tint = DeepGulfTeal,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = directionHint,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = DeepGulfTeal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/**
 * Left-side adventure action controls: Interact (pulsing when active), Jump, Run toggle, and Journal shortcut.
 */
@Composable
fun AdventureActionButtons(
    interactLabel: String?,
    isRunMode: Boolean,
    onToggleRun: () -> Unit,
    onJump: () -> Unit,
    onInteract: () -> Unit,
    onOpenJournal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canInteract = interactLabel != null
    val pulse = rememberInfiniteTransition(label = "interact_pulse")
    val interactScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = if (canInteract) 1.05f else 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "interact_scale",
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Jump Button
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onJump)
                        .testTag("hud_jump_button"),
                    shape = CircleShape,
                    color = GulfTurquoise.copy(alpha = 0.94f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.7f)),
                    shadowElevation = 6.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = "پرش",
                            tint = Color.White,
                        )
                    }
                }

                // Run Mode Toggle
                Surface(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onToggleRun)
                        .testTag("hud_run_toggle_button"),
                    shape = CircleShape,
                    color = if (isRunMode) SunlitGold else ParchmentCream.copy(alpha = 0.92f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isRunMode) WarmSepiaInk else ParchmentBorder,
                    ),
                    shadowElevation = 6.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.DirectionsRun,
                            contentDescription = "حالت دویدن",
                            tint = WarmSepiaInk,
                        )
                    }
                }

                // Journal Quick Button
                Surface(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenJournal)
                        .testTag("hud_journal_button"),
                    shape = CircleShape,
                    color = ParchmentCream.copy(alpha = 0.94f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, ParchmentBorder),
                    shadowElevation = 6.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.MenuBook,
                            contentDescription = "دفترچه اسرار",
                            tint = DeepGulfTeal,
                        )
                    }
                }
            }

            // Primary Context-Aware Interact Button
            Surface(
                modifier = Modifier
                    .scale(interactScale)
                    .clip(RoundedCornerShape(28.dp))
                    .border(
                        width = if (canInteract) 2.5.dp else 1.5.dp,
                        color = if (canInteract) Color.White else Color.White.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(28.dp),
                    )
                    .clickable(enabled = canInteract, onClick = onInteract)
                    .testTag("hud_interact_button"),
                shape = RoundedCornerShape(28.dp),
                color = if (canInteract) SunlitGold else DeepGulfTeal.copy(alpha = 0.48f),
                shadowElevation = if (canInteract) 12.dp else 2.dp,
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TouchApp,
                            contentDescription = "تعامل",
                            tint = if (canInteract) WarmSepiaInk else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(26.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = interactLabel ?: "تعامل",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (canInteract) WarmSepiaInk else Color.White.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
    }
}
