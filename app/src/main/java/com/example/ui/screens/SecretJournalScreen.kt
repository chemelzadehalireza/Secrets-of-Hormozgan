package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.catalog.HormozganCatalog
import com.example.model.BiomeType
import com.example.model.CountyProgressState
import com.example.ui.characters.LocalNpcCanvas
import com.example.ui.theme.DeepGulfTeal
import com.example.ui.theme.GulfTurquoise
import com.example.ui.theme.MarkaziNarrativeFamily
import com.example.ui.theme.OasisGreen
import com.example.ui.theme.ParchmentAged
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentCream
import com.example.ui.theme.PersianNastaliqFamily
import com.example.ui.theme.SunlitGold
import com.example.ui.theme.TerracottaOchre
import com.example.ui.theme.WarmAmberGold
import com.example.ui.theme.WarmSepiaInk
import com.example.util.formatPersianPercent
import com.example.util.toPersianNumber

/**
 * Grandpa's Secret Journal (Section 13):
 * Animated page-turning notebook containing all 7 required sections.
 */
@Composable
fun SecretJournalScreen(
    initialTab: Int = 0,
    progressMap: Map<String, CountyProgressState>,
    encyclopediaUnlocked: Boolean,
    onOpenEncyclopedia: () -> Unit,
    onPlayCounty: (String) -> Unit,
    onBack: () -> Unit,
) {
    val tabs = listOf(
        "نقشه ۱۳ شهرستان",
        "قطعات گنج",
        "اطلاعات علمی",
        "شخصیت‌های محلی",
        "مأموریت‌ها",
        "تصاویر مکان‌ها",
        "پیشرفت کلی",
    )
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, tabs.lastIndex)) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Generated antique leather notebook artwork
        Image(
            painter = painterResource(id = R.drawable.img_journal_parchment_bg),
            contentDescription = "دفترچه اسرار پدربزرگ",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmSepiaInk.copy(alpha = 0.25f)),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(14.dp),
        ) {
            // Header
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ParchmentCream.copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(2.dp, WarmAmberGold),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("journal_back_button"),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "بازگشت",
                                tint = DeepGulfTeal,
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "دفترچه اسرار پدربزرگ",
                                style = MaterialTheme.typography.headlineMedium,
                                color = DeepGulfTeal,
                            )
                            Text(
                                text = "ثبت مشاهدات، سرنخ‌ها و دانش ۱۳ شهرستان هرمزگان",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarmSepiaInk.copy(alpha = 0.78f),
                            )
                        }
                    }

                    val pieces = progressMap.values.count { it.treasureCollected }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SunlitGold,
                    ) {
                        Text(
                            text = "${pieces.toPersianNumber()} / ۱۳ قطعه",
                            style = MaterialTheme.typography.labelLarge,
                            color = WarmSepiaInk,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable 7-Section Journal Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                tabs.forEachIndexed { idx, title ->
                    val active = selectedTab == idx
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedTab = idx }
                            .testTag("journal_tab_$idx"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (active) DeepGulfTeal else ParchmentCream.copy(alpha = 0.90f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (active) SunlitGold else ParchmentBorder,
                        ),
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (active) SunlitGold else WarmSepiaInk,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated Page-Turn Content Area
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(2.dp, ParchmentBorder, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = ParchmentCream.copy(alpha = 0.94f),
                shadowElevation = 10.dp,
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        (fadeIn(tween(240)) + slideInHorizontally(tween(280)) { -it / 6 }) togetherWith
                            (fadeOut(tween(180)) + slideOutHorizontally(tween(220)) { it / 6 })
                    },
                    label = "journal_page_flip",
                ) { tabIdx ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        when (tabIdx) {
                            0 -> JournalCountiesTab(progressMap, onPlayCounty)
                            1 -> JournalTreasureTab(progressMap, encyclopediaUnlocked, onOpenEncyclopedia)
                            2 -> JournalScientificTab(progressMap)
                            3 -> JournalCharactersTab(progressMap)
                            4 -> JournalMissionsTab(progressMap)
                            5 -> JournalLandmarksTab(progressMap)
                            6 -> JournalOverallProgressTab(progressMap)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JournalCountiesTab(
    progressMap: Map<String, CountyProgressState>,
    onPlayCounty: (String) -> Unit,
) {
    Text(
        text = "فهرست ۱۳ شهرستان روی نقشه پدربزرگ",
        style = MaterialTheme.typography.titleLarge,
        color = DeepGulfTeal,
    )
    HormozganCatalog.allCounties.forEach { county ->
        val st = progressMap[county.id]
        val unlocked = st?.isUnlocked ?: (county.id == "bashagard")
        val done = st?.isCompleted == true
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(enabled = unlocked) { onPlayCounty(county.id) },
            shape = RoundedCornerShape(16.dp),
            color = if (done) OasisGreen.copy(alpha = 0.12f) else ParchmentAged,
            border = androidx.compose.foundation.BorderStroke(1.dp, ParchmentBorder),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${county.order.toPersianNumber()}. ${county.nameFa} — ${county.stageTitle}",
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepGulfTeal,
                    )
                    Text(
                        text = county.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmSepiaInk.copy(alpha = 0.8f),
                    )
                }
                Icon(
                    imageVector = when {
                        done -> Icons.Rounded.CheckCircle
                        unlocked -> Icons.Rounded.AutoAwesome
                        else -> Icons.Rounded.Lock
                    },
                    contentDescription = null,
                    tint = if (done) OasisGreen else if (unlocked) WarmAmberGold else Color.Gray,
                )
            }
        }
    }
}

@Composable
private fun JournalTreasureTab(
    progressMap: Map<String, CountyProgressState>,
    encyclopediaUnlocked: Boolean,
    onOpenEncyclopedia: () -> Unit,
) {
    val collected = progressMap.values.count { it.treasureCollected }
    Text(
        text = "قطعات گنج سیزده‌تکه (${collected.toPersianNumber()} از ۱۳)",
        style = MaterialTheme.typography.titleLarge,
        color = DeepGulfTeal,
    )
    if (encyclopediaUnlocked || collected >= 13) {
        Button(
            onClick = onOpenEncyclopedia,
            colors = ButtonDefaults.buttonColors(containerColor = SunlitGold),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = WarmSepiaInk)
            Spacer(modifier = Modifier.width(8.dp))
            Text("گشودن «دانشنامه آینده هرمزگان»", style = MaterialTheme.typography.labelLarge, color = WarmSepiaInk)
        }
    }

    HormozganCatalog.allCounties.forEach { county ->
        val found = progressMap[county.id]?.treasureCollected == true
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (found) SunlitGold.copy(alpha = 0.22f) else ParchmentAged.copy(alpha = 0.65f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, if (found) WarmAmberGold else ParchmentBorder),
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Text(
                    text = if (found) {
                        "قطعه ${county.treasurePiece.index.toPersianNumber()}: ${county.treasurePiece.title} (${county.nameFa})"
                    } else {
                        "قطعه ${county.treasurePiece.index.toPersianNumber()} (${county.nameFa}): هنوز کشف نشده"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (found) DeepGulfTeal else WarmSepiaInk.copy(alpha = 0.6f),
                )
                if (found) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${county.treasurePiece.pillarTheme} — ${county.treasurePiece.encyclopediaDetail}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmSepiaInk,
                    )
                }
            }
        }
    }
}

@Composable
private fun JournalScientificTab(progressMap: Map<String, CountyProgressState>) {
    Text(
        text = "اطلاعات علمی، جغرافیایی و فرهنگی مستند",
        style = MaterialTheme.typography.titleLarge,
        color = DeepGulfTeal,
    )
    Text(
        text = "پس از تکمیل مأموریت‌های هر شهرستان، پرونده علمی آن در این بخش فعال می‌شود. تمام داده‌های تاریخی و طبیعی بر اساس منابع معتبر استان هرمزگان تدوین شده‌اند.",
        style = MaterialTheme.typography.bodySmall,
        color = WarmSepiaInk.copy(alpha = 0.8f),
    )

    HormozganCatalog.allCounties.forEach { county ->
        val unlockedSci = progressMap[county.id]?.isCompleted == true
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (unlockedSci) ParchmentCream else ParchmentAged.copy(alpha = 0.6f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, if (unlockedSci) GulfTurquoise else ParchmentBorder),
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Text(
                    text = "${county.nameFa} — ${county.scientificBrief.landmarkTitle}",
                    style = MaterialTheme.typography.titleMedium,
                    color = DeepGulfTeal,
                )
                if (unlockedSci) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("جغرافیا: ${county.scientificBrief.geographySummary}", style = MaterialTheme.typography.bodyMedium, color = WarmSepiaInk)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("علم و محیط‌زیست: ${county.scientificBrief.ecologyAndScience}", style = MaterialTheme.typography.bodyMedium, color = WarmSepiaInk)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("میراث فرهنگی: ${county.scientificBrief.culturalHeritage}", style = MaterialTheme.typography.bodyMedium, color = WarmSepiaInk)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(county.scientificBrief.sourceNote, style = MaterialTheme.typography.labelSmall, color = GulfTurquoise)
                } else {
                    Text(
                        text = "برای گشودن پرونده علمی ${county.nameFa}، ابتدا مأموریت‌های این مرحله را کامل کنید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmSepiaInk.copy(alpha = 0.65f),
                    )
                }
            }
        }
    }
}

@Composable
private fun JournalCharactersTab(progressMap: Map<String, CountyProgressState>) {
    Text(
        text = "شخصیت‌های محلی ملاقات‌شده (شخصیت‌های داستانی)",
        style = MaterialTheme.typography.titleLarge,
        color = DeepGulfTeal,
    )
    HormozganCatalog.allCounties.forEach { county ->
        val met = progressMap[county.id]?.npcMet == true
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = ParchmentAged,
            border = androidx.compose.foundation.BorderStroke(1.dp, ParchmentBorder),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DeepGulfTeal.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (met) {
                        LocalNpcCanvas(
                            appearance = county.npcAppearance,
                            facingLeft = true,
                            modifier = Modifier.size(60.dp),
                        )
                    } else {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (met) "${county.npcName} (${county.nameFa})" else "شخصیت ناشناس در ${county.nameFa}",
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepGulfTeal,
                    )
                    Text(
                        text = if (met) county.npcRole else "هنوز با این شخصیت ملاقات نکرده‌اید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmSepiaInk,
                    )
                }
            }
        }
    }
}

@Composable
private fun JournalMissionsTab(progressMap: Map<String, CountyProgressState>) {
    Text(
        text = "کارنامه مأموریت‌های انجام‌شده",
        style = MaterialTheme.typography.titleLarge,
        color = DeepGulfTeal,
    )
    HormozganCatalog.allCounties.forEach { county ->
        val doneSet = progressMap[county.id]?.completedMissionIds ?: emptySet()
        Text(
            text = "${county.nameFa} (${doneSet.size.toPersianNumber()} از ${county.missions.size.toPersianNumber()})",
            style = MaterialTheme.typography.titleMedium,
            color = GulfTurquoise,
        )
        county.missions.forEach { m ->
            val ok = m.id in doneSet
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                Icon(
                    imageVector = if (ok) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = if (ok) OasisGreen else Color.Gray,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (ok) "${m.title} — سرنخ: ${m.clueRewardTitle}" else "${m.title} (در انتظار کشف)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (ok) WarmSepiaInk else WarmSepiaInk.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
private fun JournalLandmarksTab(progressMap: Map<String, CountyProgressState>) {
    Text(
        text = "نگارخانه مکان‌های کشف‌شده",
        style = MaterialTheme.typography.titleLarge,
        color = DeepGulfTeal,
    )
    HormozganCatalog.allCounties.forEach { county ->
        val discovered = progressMap[county.id]?.npcMet == true
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = ParchmentAged,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ParchmentBorder),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Stylized 2D Canvas scenic postcard per biome
                Canvas(
                    modifier = Modifier
                        .size(width = 110.dp, height = 74.dp)
                        .clip(RoundedCornerShape(12.dp)),
                ) {
                    val top = when (county.biome) {
                        BiomeType.MOUNTAIN, BiomeType.MARTIAN_CLIFFS -> Color(0xFFFB923C)
                        BiomeType.COASTAL_MANGROVE, BiomeType.OCEAN_CORAL -> Color(0xFF0EA5E9)
                        else -> Color(0xFF38BDF8)
                    }
                    drawRect(brush = Brush.verticalGradient(listOf(top, Color(0xFFFEF3C7))))
                    val hill = Path().apply {
                        moveTo(0f, size.height)
                        lineTo(size.width * 0.35f, size.height * 0.35f)
                        lineTo(size.width * 0.72f, size.height * 0.55f)
                        lineTo(size.width, size.height * 0.38f)
                        lineTo(size.width, size.height)
                        close()
                    }
                    drawPath(
                        hill,
                        color = if (discovered) TerracottaOchre.copy(alpha = 0.85f) else Color.Gray.copy(alpha = 0.45f),
                    )
                    drawCircle(color = SunlitGold, radius = 12f, center = Offset(size.width * 0.78f, size.height * 0.25f))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = county.scientificBrief.landmarkTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepGulfTeal,
                    )
                    Text(
                        text = if (discovered) "${county.nameFa} — ثبت‌شده در نگارخانه سفر" else "${county.nameFa} — هنوز بازدید نشده",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmSepiaInk.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

@Composable
private fun JournalOverallProgressTab(progressMap: Map<String, CountyProgressState>) {
    val totalCounties = HormozganCatalog.allCounties.size
    val completedCounties = progressMap.values.count { it.isCompleted }
    val totalMissions = HormozganCatalog.allCounties.sumOf { it.missions.size }
    val completedMissions = progressMap.values.sumOf { it.completedMissionIds.size }
    val pct = ((completedMissions * 100f) / totalMissions.coerceAtLeast(1)).toInt()

    Text(
        text = "پیشرفت کلی ماجراجویی آریا",
        style = MaterialTheme.typography.titleLarge,
        color = DeepGulfTeal,
    )
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = DeepGulfTeal.copy(alpha = 0.09f),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "درصد کل پیشرفت: ${formatPersianPercent(pct)}",
                style = MaterialTheme.typography.headlineMedium,
                color = DeepGulfTeal,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (completedMissions.toFloat() / totalMissions.coerceAtLeast(1)).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = GulfTurquoise,
                trackColor = ParchmentBorder.copy(alpha = 0.4f),
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "• شهرستان‌های تکمیل‌شده: ${completedCounties.toPersianNumber()} از ${totalCounties.toPersianNumber()}",
                style = MaterialTheme.typography.bodyLarge,
                color = WarmSepiaInk,
            )
            Text(
                text = "• مأموریت‌ها و معماهای حل‌شده: ${completedMissions.toPersianNumber()} از ${totalMissions.toPersianNumber()}",
                style = MaterialTheme.typography.bodyLarge,
                color = WarmSepiaInk,
            )
            Text(
                text = "• قطعات گنج ثبت‌شده: ${completedCounties.toPersianNumber()} از ${totalCounties.toPersianNumber()}",
                style = MaterialTheme.typography.bodyLarge,
                color = WarmSepiaInk,
            )
        }
    }
    Text(
        text = "یادداشت پدربزرگ: «گنج واقعی زمانی کامل می‌شود که هر ۱۳ شهرستان را بشناسی و دانشنامه آینده هرمزگان را بگشایی.»",
        style = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = PersianNastaliqFamily,
            fontSize = 19.sp,
            lineHeight = 36.sp,
        ),
        color = TerracottaOchre,
    )
}
