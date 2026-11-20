package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.SurroundSound
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VoicePrecacheState
import com.example.data.catalog.HormozganCatalog
import com.example.model.AriaAnimationState
import com.example.model.AudioSettings
import com.example.ui.characters.AriaCharacterCanvas
import com.example.ui.theme.CrimsonCliff
import com.example.ui.theme.DeepGulfTeal
import com.example.ui.theme.GulfTurquoise
import com.example.ui.theme.MarkaziNarrativeFamily
import com.example.ui.theme.OasisGreen
import com.example.ui.theme.ParchmentAged
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentCream
import com.example.ui.theme.PersianNastaliqFamily
import com.example.ui.theme.SunlitGold
import com.example.ui.theme.WarmAmberGold
import com.example.ui.theme.WarmSepiaInk
import com.example.util.formatPersianPercent
import com.example.util.toPersianNumber
import kotlin.math.roundToInt

/**
 * «دانشنامه آینده هرمزگان» — Emotional 10-pillar finale unlocked after collecting all 13 treasure pieces.
 */
@Composable
fun EncyclopediaScreen(
    onSpeakFinale: () -> Unit = {},
    onStopSpeech: () -> Unit = {},
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) {
        onSpeakFinale()
    }
    DisposableEffect(Unit) {
        onDispose { onStopSpeech() }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(DeepGulfTeal, Color(0xFF0A3642))),
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.widthIn(max = 820.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = ParchmentCream) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(46.dp).testTag("encyclopedia_back_button"),
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "بازگشت", tint = DeepGulfTeal)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "دانشنامه آینده هرمزگان",
                style = MaterialTheme.typography.headlineLarge,
                color = SunlitGold,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .widthIn(max = 820.dp)
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = ParchmentCream,
                border = androidx.compose.foundation.BorderStroke(2.5.dp, SunlitGold),
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AriaCharacterCanvas(
                        animationState = AriaAnimationState.RECEIVE_REWARD,
                        facingRight = true,
                        modifier = Modifier.size(110.dp),
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = WarmAmberGold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "گنج واقعی هرمزگان آشکار شد!",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = PersianNastaliqFamily,
                                    fontSize = 23.sp,
                                    lineHeight = 40.sp,
                                ),
                                color = DeepGulfTeal,
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "«آفرین آریا! این گنج از طلا و جواهر نیست؛ دانش ارزشمندی است که با سفر در هر ۱۳ شهرستان آموختی تا آب، دریا، زمین، فرهنگ و آینده هرمزگان را با عشق و آگاهی بسازی.»",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = MarkaziNarrativeFamily,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = WarmSepiaInk,
                        )
                    }
                }
            }

            HormozganCatalog.encyclopediaPillars.forEach { pillar ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = ParchmentCream.copy(alpha = 0.96f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, ParchmentBorder),
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        Text(
                            text = "${pillar.number.toPersianNumber()}. ${pillar.title}",
                            style = MaterialTheme.typography.titleMedium,
                            color = GulfTurquoise,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = pillar.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmSepiaInk,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "شهرستان‌های مرتبط: ${pillar.relatedCounties}",
                            style = MaterialTheme.typography.labelSmall,
                            color = OasisGreen,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Audio & Game Settings screen with independent sliders for Music, Voice, and SFX (Section 11).
 */
@Composable
fun SettingsScreen(
    settings: AudioSettings,
    voicePrecacheState: VoicePrecacheState = VoicePrecacheState(),
    onUpdateSettings: ((AudioSettings) -> AudioSettings) -> Unit,
    onPrecacheAllVoices: () -> Unit = {},
    onCancelPrecache: () -> Unit = {},
    onResetProgress: () -> Unit,
    onBack: () -> Unit,
) {
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(ParchmentCream, ParchmentAged)),
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = DeepGulfTeal,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(46.dp).testTag("settings_back_button"),
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "بازگشت", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "تنظیمات صدا و بازی",
                style = MaterialTheme.typography.headlineMedium,
                color = DeepGulfTeal,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AudioSliderCard(
                title = "موسیقی پس‌زمینه محیط‌ها",
                icon = Icons.Rounded.MusicNote,
                enabled = settings.musicEnabled,
                volume = settings.musicVolume,
                onToggle = { enabled -> onUpdateSettings { it.copy(musicEnabled = enabled) } },
                onVolumeChange = { v -> onUpdateSettings { it.copy(musicVolume = v) } },
                testTagPrefix = "music",
            )

            AudioSliderCard(
                title = "صدای گوینده و شخصیت‌ها",
                icon = Icons.Rounded.RecordVoiceOver,
                enabled = settings.voiceEnabled,
                volume = settings.voiceVolume,
                onToggle = { enabled -> onUpdateSettings { it.copy(voiceEnabled = enabled) } },
                onVolumeChange = { v -> onUpdateSettings { it.copy(voiceVolume = v) } },
                testTagPrefix = "voice",
            )

            AudioSliderCard(
                title = "افکت‌های صوتی بازی",
                icon = Icons.Rounded.SurroundSound,
                enabled = settings.sfxEnabled,
                volume = settings.sfxVolume,
                onToggle = { enabled -> onUpdateSettings { it.copy(sfxEnabled = enabled) } },
                onVolumeChange = { v -> onUpdateSettings { it.copy(sfxVolume = v) } },
                testTagPrefix = "sfx",
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ParchmentCream,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, ParchmentBorder),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RecordVoiceOver,
                        contentDescription = null,
                        tint = OasisGreen,
                        modifier = Modifier.size(28.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "گویندگی اختصاصی ۱۵ شخصیت",
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepGulfTeal,
                        )
                        Text(
                            text = "صدای اختصاصی راوی، آریا و تمام شخصیت‌های ۱۳ شهرستان به‌صورت کامل داخل بازی قرار دارد و بدون نیاز به دانلود پخش می‌شود.",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmSepiaInk.copy(alpha = 0.84f),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("settings_reset_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonCliff),
            ) {
                Icon(Icons.Rounded.DeleteForever, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("پاک کردن پیشرفت و شروع دوباره", style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("شروع دوباره ماجراجویی؟", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "با تایید این گزینه تمام مأموریت‌ها و قطعات گنج ثبت‌شده پاک می‌شوند و بازی دوباره از بشاگرد آغاز می‌شود.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        onResetProgress()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonCliff),
                ) {
                    Text("بله، پاک شود", style = MaterialTheme.typography.labelMedium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("انصراف", style = MaterialTheme.typography.labelMedium)
                }
            },
        )
    }
}

@Composable
private fun AudioSliderCard(
    title: String,
    icon: ImageVector,
    enabled: Boolean,
    volume: Float,
    onToggle: (Boolean) -> Unit,
    onVolumeChange: (Float) -> Unit,
    testTagPrefix: String,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ParchmentCream,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, ParchmentBorder),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = GulfTurquoise)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = title, style = MaterialTheme.typography.titleMedium, color = DeepGulfTeal)
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(checkedTrackColor = GulfTurquoise),
                    modifier = Modifier.testTag("settings_${testTagPrefix}_switch"),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null, tint = WarmSepiaInk.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = WarmAmberGold, activeTrackColor = GulfTurquoise),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatPersianPercent((volume * 100).roundToInt()),
                    style = MaterialTheme.typography.labelMedium,
                    color = DeepGulfTeal,
                )
            }
        }
    }
}

/**
 * Section 12: «درباره ما» screen exists and works properly, while its body content remains empty
 * for the team to populate later.
 */
@Composable
fun AboutUsScreen(
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(ParchmentCream, ParchmentAged)),
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = DeepGulfTeal) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(46.dp).testTag("about_back_button"),
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "بازگشت", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "درباره ما",
                style = MaterialTheme.typography.headlineMedium,
                color = DeepGulfTeal,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Intentionally empty content container per Section 12 instructions
        Box(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .weight(1f)
                .fillMaxWidth()
                .border(1.5.dp, ParchmentBorder, RoundedCornerShape(24.dp))
                .background(ParchmentCream.copy(alpha = 0.85f), RoundedCornerShape(24.dp))
                .testTag("about_empty_content_container"),
        )
    }
}
