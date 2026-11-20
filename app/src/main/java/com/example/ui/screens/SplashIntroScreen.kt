package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.catalog.HormozganCatalog
import com.example.model.AriaAnimationState
import com.example.model.DialogueLine
import com.example.model.VoiceProfile
import com.example.ui.characters.AriaCharacterCanvas
import com.example.ui.theme.DeepGulfTeal
import com.example.ui.theme.GulfTurquoise
import com.example.ui.theme.MarkaziNarrativeFamily
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentCream
import com.example.ui.theme.PersianNastaliqFamily
import com.example.ui.theme.SunlitGold
import com.example.ui.theme.WarmAmberGold
import com.example.ui.theme.WarmSepiaInk
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashIntroScreen(
    hasSeenIntro: Boolean,
    viewModel: GameViewModel,
    onContinueToMenu: () -> Unit,
    onStartBashagardDirect: () -> Unit,
) {
    var showStoryCard by remember { mutableStateOf(false) }
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        delay(250L)
        showStoryCard = true
        viewModel.voiceManager.speakDialogue(HormozganCatalog.introDialogueLine)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.voiceManager.stop()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_menu_hormozgan_bg),
            contentDescription = "منظره کارتونی استان هرمزگان",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            DeepGulfTeal.copy(alpha = 0.45f),
                            DeepGulfTeal.copy(alpha = 0.72f),
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "هرمزگان؛ سرزمین رازهای پنهان",
                style = MaterialTheme.typography.displayLarge,
                color = SunlitGold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "سفر ماجراجویانه آریا در ۱۳ شهرستان استان هرمزگان",
                style = MaterialTheme.typography.titleMedium,
                color = ParchmentCream,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = showStoryCard,
                enter = fadeIn(tween(550)) + slideInVertically(tween(550)) { it / 4 },
            ) {
                Surface(
                    modifier = Modifier
                        .widthIn(max = 620.dp)
                        .fillMaxWidth()
                        .border(2.5.dp, WarmAmberGold, RoundedCornerShape(26.dp)),
                    shape = RoundedCornerShape(26.dp),
                    color = ParchmentCream.copy(alpha = 0.96f),
                    shadowElevation = 14.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            AriaCharacterCanvas(
                                animationState = AriaAnimationState.OPEN_NOTEBOOK,
                                facingRight = true,
                                modifier = Modifier.size(106.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = Icons.Rounded.AutoAwesome,
                                            contentDescription = null,
                                            tint = WarmAmberGold,
                                            modifier = Modifier.size(20.dp),
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "صفحه نخست دفترچه پدربزرگ",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = PersianNastaliqFamily,
                                                fontSize = 20.sp,
                                                lineHeight = 36.sp,
                                            ),
                                            color = DeepGulfTeal,
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            if (isSpeaking) {
                                                viewModel.voiceManager.stop()
                                            } else {
                                                viewModel.voiceManager.speakDialogue(HormozganCatalog.introDialogueLine)
                                            }
                                        },
                                        modifier = Modifier.size(48.dp).testTag("intro_voice_toggle_button"),
                                    ) {
                                        Icon(
                                            imageVector = if (isSpeaking) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                                            contentDescription = if (isSpeaking) "توقف صدای راوی" else "پخش صدای راوی",
                                            tint = DeepGulfTeal,
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "«آریای عزیزم؛\nدر این سرزمین گنجی پنهان شده است.\nاما برای پیدا کردن آن باید سیزده راز را کشف کنی.\nهر شهرستان یک نشانه دارد و هر نشانه بخشی از یک حقیقت بزرگ‌تر را آشکار می‌کند.\nبرای پیدا کردن گنج، باید سفر کنی، بشناسی، یاد بگیری و کشف کنی.»",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = MarkaziNarrativeFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        lineHeight = 30.sp,
                                    ),
                                    color = WarmSepiaInk,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.voiceManager.stop()
                                    onContinueToMenu()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("intro_menu_button"),
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                Text("منوی اصلی", style = MaterialTheme.typography.labelLarge)
                            }

                            Button(
                                onClick = {
                                    viewModel.voiceManager.stop()
                                    onStartBashagardDirect()
                                },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(52.dp)
                                    .testTag("intro_start_journey_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GulfTurquoise),
                            ) {
                                Icon(Icons.Rounded.Explore, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("آغاز سفر از بشاگرد", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}
