package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AriaAnimationState
import com.example.model.CountyDefinition
import com.example.model.DialogueLine
import com.example.ui.characters.AriaCharacterCanvas
import com.example.ui.characters.LocalNpcCanvas
import com.example.ui.theme.DeepGulfTeal
import com.example.ui.theme.GulfTurquoise
import com.example.ui.theme.ParchmentAged
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentCream
import com.example.ui.theme.WarmAmberGold
import com.example.ui.theme.WarmSepiaInk
import kotlinx.coroutines.delay

/**
 * Custom cartoon adventure dialogue box (Section 10):
 * Soft rounded translucent parchment card, animated character portrait with live lip-sync,
 * speaker badge, progressive typewriter text reveal, continue indicator, skip & voice controls.
 */
@Composable
fun CartoonDialogueOverlay(
    county: CountyDefinition,
    line: DialogueLine?,
    isSpeaking: Boolean,
    isPreparingVoice: Boolean = false,
    mouthOpenLevel: Float,
    voiceSourceLabel: String,
    onAdvance: () -> Unit,
    onSkip: () -> Unit,
    onToggleVoicePlayback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = line != null,
        enter = fadeIn(tween(220)) + slideInVertically(tween(260)) { it / 2 },
        exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { it / 2 },
        modifier = modifier,
    ) {
        if (line != null) {
            var visibleChars by remember(line.id) { mutableIntStateOf(0) }
            val fullText = line.text

            LaunchedEffect(line.id) {
                visibleChars = 0
                while (visibleChars < fullText.length) {
                    visibleChars += 2
                    delay(26L)
                }
                visibleChars = fullText.length
            }

            val isTypewriterDone = visibleChars >= fullText.length
            val displayedText = fullText.take(visibleChars.coerceAtMost(fullText.length))

            val pulse = rememberInfiniteTransition(label = "continue_arrow")
            val bobOffset by pulse.animateFloat(
                initialValue = 0f,
                targetValue = 5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "arrow_bob",
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Surface(
                    modifier = Modifier
                        .widthIn(max = 760.dp)
                        .fillMaxWidth()
                        .border(
                            width = 2.5.dp,
                            brush = Brush.linearGradient(listOf(WarmAmberGold, ParchmentBorder)),
                            shape = RoundedCornerShape(24.dp),
                        )
                        .clickable {
                            if (!isTypewriterDone) {
                                visibleChars = fullText.length
                            } else {
                                onAdvance()
                            }
                        }
                        .testTag("dialogue_box_card"),
                    shape = RoundedCornerShape(24.dp),
                    color = ParchmentCream.copy(alpha = 0.94f),
                    shadowElevation = 12.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        ParchmentCream.copy(alpha = 0.96f),
                                        ParchmentAged.copy(alpha = 0.92f),
                                    ),
                                ),
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Animated Character Portrait Frame
                        Box(
                            modifier = Modifier
                                .size(86.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFFE0F7FA), DeepGulfTeal.copy(alpha = 0.22f)),
                                    ),
                                )
                                .border(2.dp, GulfTurquoise.copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            when {
                                line.isAria -> {
                                    AriaCharacterCanvas(
                                        animationState = if (isSpeaking) AriaAnimationState.TALK else line.ariaEmotion,
                                        facingRight = true,
                                        mouthOpenLevel = if (isSpeaking) mouthOpenLevel else 0f,
                                        modifier = Modifier.size(80.dp),
                                    )
                                }
                                line.speakerName == "راوی" -> {
                                    Icon(
                                        imageVector = Icons.Rounded.MenuBook,
                                        contentDescription = "راوی داستان",
                                        tint = DeepGulfTeal,
                                        modifier = Modifier.size(44.dp),
                                    )
                                }
                                else -> {
                                    LocalNpcCanvas(
                                        appearance = county.npcAppearance,
                                        isSpeaking = isSpeaking,
                                        mouthOpenLevel = mouthOpenLevel,
                                        facingLeft = true,
                                        modifier = Modifier.size(80.dp),
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Speaker Badge, Dialogue Text & Controls
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (line.isAria) GulfTurquoise else DeepGulfTeal,
                                    ) {
                                        Text(
                                            text = line.speakerName,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        )
                                    }
                                    if (isSpeaking || isPreparingVoice) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        ) {
                                            if (isPreparingVoice && !isSpeaking) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(12.dp),
                                                    strokeWidth = 2.dp,
                                                    color = GulfTurquoise,
                                                )
                                            }
                                            Text(
                                                text = voiceSourceLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = GulfTurquoise,
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    IconButton(
                                        onClick = onToggleVoicePlayback,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("dialogue_voice_toggle_button"),
                                    ) {
                                        Icon(
                                            imageVector = if (isSpeaking) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                                            contentDescription = if (isSpeaking) "توقف صدا" else "پخش دوباره صدا",
                                            tint = DeepGulfTeal,
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = WarmAmberGold.copy(alpha = 0.25f),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable(onClick = onSkip)
                                            .testTag("dialogue_skip_button"),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.FastForward,
                                                contentDescription = "رد کردن گفتگو",
                                                tint = WarmSepiaInk,
                                                modifier = Modifier.size(18.dp),
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "رد کردن",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = WarmSepiaInk,
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = displayedText,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = WarmSepiaInk,
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = if (isTypewriterDone) "ادامه" else "تکمیل متن",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = DeepGulfTeal,
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .offset(x = (-bobOffset).dp)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(WarmAmberGold),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = "ادامه گفتگو",
                                        tint = WarmSepiaInk,
                                        modifier = Modifier.size(15.dp),
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
