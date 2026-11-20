package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AriaAnimationState
import com.example.ui.characters.AriaCharacterCanvas
import com.example.ui.theme.DeepGulfTeal
import com.example.ui.theme.GulfTurquoise
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentCream
import com.example.ui.theme.PersianNastaliqFamily
import com.example.ui.theme.SunlitGold
import com.example.ui.theme.TerracottaOchre
import com.example.ui.theme.WarmAmberGold
import com.example.ui.theme.WarmSepiaInk
import com.example.util.toPersianNumber
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun MainMenuScreen(
    collectedPieces: Int,
    activeCountyName: String,
    onStartNewGame: () -> Unit,
    onContinueGame: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenJournal: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAboutUs: () -> Unit,
) {
    val infinite = rememberInfiniteTransition(label = "menu_bg")
    val shimmerPhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "menu_shimmer",
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 680.dp

        // Illustrated Hormozgan landscape background
        Image(
            painter = painterResource(id = R.drawable.img_menu_hormozgan_bg),
            contentDescription = "پس‌زمینه کارتونی هرمزگان",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Subtle atmospheric gradient + animated sunlit motes
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        DeepGulfTeal.copy(alpha = 0.25f),
                        Color.Transparent,
                        DeepGulfTeal.copy(alpha = 0.62f),
                    ),
                ),
            )
            for (i in 0..14) {
                val fx = ((i * 197f + shimmerPhase * 28f) % size.width)
                val fy = size.height * (0.18f + (i % 6) * 0.11f) + sin(shimmerPhase + i) * 14f
                drawCircle(
                    color = SunlitGold.copy(alpha = 0.45f),
                    radius = 3.5f + (i % 3),
                    center = Offset(fx, fy),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Centered Game Title Banner (Section 12)
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = DeepGulfTeal.copy(alpha = 0.88f),
                shadowElevation = 10.dp,
                border = androidx.compose.foundation.BorderStroke(2.dp, SunlitGold),
                modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "هرمزگان؛ سرزمین رازهای پنهان",
                        style = MaterialTheme.typography.displayLarge,
                        color = SunlitGold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "سفر دانایی و کشف سیزده گنج ماندگار",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = PersianNastaliqFamily,
                            fontSize = 18.sp,
                            lineHeight = 34.sp,
                        ),
                        color = ParchmentCream.copy(alpha = 0.94f),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "قطعات کشف‌شده گنج: ${collectedPieces.toPersianNumber()} از ۱۳  |  مقصد فعلی: $activeCountyName",
                        style = MaterialTheme.typography.labelMedium,
                        color = ParchmentCream,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (isWide) {
                Row(
                    modifier = Modifier.widthIn(max = 860.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MenuAriaShowcase(modifier = Modifier.weight(0.95f))
                    MenuButtonGrid(
                        onStartNewGame = onStartNewGame,
                        onContinueGame = onContinueGame,
                        onOpenMap = onOpenMap,
                        onOpenJournal = onOpenJournal,
                        onOpenSettings = onOpenSettings,
                        onOpenAboutUs = onOpenAboutUs,
                        modifier = Modifier.weight(1.25f),
                    )
                }
            } else {
                MenuAriaShowcase(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(14.dp))
                MenuButtonGrid(
                    onStartNewGame = onStartNewGame,
                    onContinueGame = onContinueGame,
                    onOpenMap = onOpenMap,
                    onOpenJournal = onOpenJournal,
                    onOpenSettings = onOpenSettings,
                    onOpenAboutUs = onOpenAboutUs,
                    modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun MenuAriaShowcase(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = ParchmentCream.copy(alpha = 0.20f),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, SunlitGold.copy(alpha = 0.6f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AriaCharacterCanvas(
                animationState = AriaAnimationState.OPEN_NOTEBOOK,
                facingRight = true,
                modifier = Modifier.size(185.dp),
            )
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ParchmentCream.copy(alpha = 0.92f),
            ) {
                Text(
                    text = "آریا با دفترچه و نقشه آماده سفر است!",
                    style = MaterialTheme.typography.labelMedium,
                    color = WarmSepiaInk,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun MenuButtonGrid(
    onStartNewGame: () -> Unit,
    onContinueGame: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenJournal: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAboutUs: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        MenuAnimatedButton(
            label = "شروع بازی",
            icon = Icons.Rounded.PlayArrow,
            containerColor = SunlitGold,
            contentColor = WarmSepiaInk,
            testTag = "menu_start_game_button",
            onClick = onStartNewGame,
        )
        MenuAnimatedButton(
            label = "ادامه بازی",
            icon = Icons.Rounded.Replay,
            containerColor = GulfTurquoise,
            contentColor = Color.White,
            testTag = "menu_continue_game_button",
            onClick = onContinueGame,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MenuAnimatedButton(
                label = "نقشه هرمزگان",
                icon = Icons.Rounded.Map,
                containerColor = ParchmentCream.copy(alpha = 0.95f),
                contentColor = DeepGulfTeal,
                testTag = "menu_map_button",
                onClick = onOpenMap,
                modifier = Modifier.weight(1f),
            )
            MenuAnimatedButton(
                label = "دفترچه اسرار",
                icon = Icons.Rounded.MenuBook,
                containerColor = ParchmentCream.copy(alpha = 0.95f),
                contentColor = DeepGulfTeal,
                testTag = "menu_journal_button",
                onClick = onOpenJournal,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MenuAnimatedButton(
                label = "تنظیمات",
                icon = Icons.Rounded.Settings,
                containerColor = DeepGulfTeal.copy(alpha = 0.92f),
                contentColor = ParchmentCream,
                testTag = "menu_settings_button",
                onClick = onOpenSettings,
                modifier = Modifier.weight(1f),
            )
            MenuAnimatedButton(
                label = "درباره ما",
                icon = Icons.Rounded.Info,
                containerColor = TerracottaOchre.copy(alpha = 0.92f),
                contentColor = Color.White,
                testTag = "menu_about_button",
                onClick = onOpenAboutUs,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MenuAnimatedButton(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "btn_scale",
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        shadowElevation = if (pressed) 2.dp else 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, WarmAmberGold.copy(alpha = 0.7f)),
        modifier = modifier
            .scale(scale)
            .height(58.dp)
            .testTag(testTag),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                maxLines = 1,
            )
        }
    }
}
