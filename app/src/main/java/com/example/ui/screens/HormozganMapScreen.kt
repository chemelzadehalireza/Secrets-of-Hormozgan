package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.catalog.HormozganCatalog
import com.example.model.CountyDefinition
import com.example.model.CountyProgressState
import com.example.ui.characters.LocalNpcCanvas
import com.example.ui.theme.DeepGulfTeal
import com.example.ui.theme.GulfTurquoise
import com.example.ui.theme.OasisGreen
import com.example.ui.theme.ParchmentAged
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentCream
import com.example.ui.theme.SunlitGold
import com.example.ui.theme.TerracottaOchre
import com.example.ui.theme.WarmAmberGold
import com.example.ui.theme.WarmSepiaInk
import com.example.util.toPersianNumber
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun HormozganMapScreen(
    progressMap: Map<String, CountyProgressState>,
    activeCountyId: String,
    encyclopediaUnlocked: Boolean,
    onSelectCountyToPlay: (CountyDefinition) -> Unit,
    onOpenJournal: () -> Unit,
    onOpenEncyclopedia: () -> Unit,
    onBack: () -> Unit,
) {
    var selectedCounty by remember(activeCountyId) {
        mutableStateOf(HormozganCatalog.getById(activeCountyId))
    }

    val infinite = rememberInfiniteTransition(label = "map_anim")
    val wavePhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "map_waves",
    )

    val collectedCount = progressMap.values.count { it.treasureCollected }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0E7490), Color(0xFF0F4C5C))),
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(12.dp),
    ) {
        // Top Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = ParchmentCream,
                    shadowElevation = 4.dp,
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(46.dp)
                            .testTag("map_back_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "بازگشت",
                            tint = DeepGulfTeal,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "نقشه ۱۳ شهرستان استان هرمزگان",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SunlitGold,
                    )
                    Text(
                        text = "نقشه تصویری و نمادین ماجراجویی (گنج‌های کشف‌شده: ${collectedCount.toPersianNumber()} از ۱۳)",
                        style = MaterialTheme.typography.labelSmall,
                        color = ParchmentCream.copy(alpha = 0.90f),
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (encyclopediaUnlocked || collectedCount >= 13) {
                    Button(
                        onClick = onOpenEncyclopedia,
                        colors = ButtonDefaults.buttonColors(containerColor = SunlitGold),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("map_encyclopedia_button"),
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = WarmSepiaInk, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("دانشنامه آینده هرمزگان", style = MaterialTheme.typography.labelMedium, color = WarmSepiaInk)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ParchmentCream,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onOpenJournal)
                        .testTag("map_journal_button"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.MenuBook, contentDescription = "دفترچه اسرار", tint = DeepGulfTeal, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("دفترچه", style = MaterialTheme.typography.labelMedium, color = DeepGulfTeal)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Stylized Hormozgan Geographic Cartoon Map Canvas
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .border(2.5.dp, WarmAmberGold, RoundedCornerShape(26.dp)),
        ) {
            val density = LocalDensity.current
            val mapWidthPx = constraints.maxWidth.toFloat()
            val mapHeightPx = constraints.maxHeight.toFloat()

            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Sea of Persian Gulf & Sea of Oman
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF38BDF8), Color(0xFF0E7490), Color(0xFF0C4A6E)),
                    ),
                )

                // Animated sea wave lines
                for (row in 0..7) {
                    val wy = h * (0.48f + row * 0.068f)
                    val shift = sin(wavePhase + row) * 14f
                    for (col in 0..6) {
                        val wx = w * (0.06f + col * 0.15f) + shift
                        drawArc(
                            color = Color.White.copy(alpha = 0.22f),
                            startAngle = 195f,
                            sweepAngle = 150f,
                            useCenter = false,
                            topLeft = Offset(wx, wy),
                            size = androidx.compose.ui.geometry.Size(34f, 10f),
                            style = Stroke(width = 2.2f, cap = StrokeCap.Round),
                        )
                    }
                }

                // Stylized Hormozgan Province Mainland Coastline (West Parsian -> Center Bandar Abbas -> East Jask/Bashagard)
                val mainland = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w, 0f)
                    lineTo(w, h * 0.88f)
                    // Jask & Sirik coast curving along Sea of Oman up into Strait of Hormuz
                    cubicTo(w * 0.84f, h * 0.84f, w * 0.72f, h * 0.76f, w * 0.67f, h * 0.56f)
                    // Minab & Bandar Abbas bay
                    cubicTo(w * 0.63f, h * 0.48f, w * 0.53f, h * 0.45f, w * 0.44f, h * 0.47f)
                    // Khamir -> Bandar Lengeh -> Parsian western Gulf coast
                    cubicTo(w * 0.34f, h * 0.52f, w * 0.25f, h * 0.62f, w * 0.14f, h * 0.54f)
                    cubicTo(w * 0.06f, h * 0.48f, 0f, h * 0.42f, 0f, h * 0.38f)
                    close()
                }
                drawPath(
                    path = mainland,
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFF3E0B5), Color(0xFFE7C68E), Color(0xFFD6A86E)),
                    ),
                )
                drawPath(
                    path = mainland,
                    color = Color(0xFF9C6644),
                    style = Stroke(width = 3.5f),
                )

                // Stylized Mountain Peaks in Hajiabad & Bashagard interior
                listOf(
                    Offset(w * 0.52f, h * 0.14f),
                    Offset(w * 0.78f, h * 0.52f),
                    Offset(w * 0.24f, h * 0.28f),
                ).forEach { peak ->
                    val tri = Path().apply {
                        moveTo(peak.x - 24f, peak.y + 18f)
                        lineTo(peak.x, peak.y - 16f)
                        lineTo(peak.x + 24f, peak.y + 18f)
                        close()
                    }
                    drawPath(tri, color = Color(0xFFB07D56).copy(alpha = 0.42f))
                }

                // Stylized Qeshm Island (elongated southwest of Bandar Abbas)
                val qeshmPath = Path().apply {
                    moveTo(w * 0.41f, h * 0.65f)
                    cubicTo(w * 0.47f, h * 0.58f, w * 0.56f, h * 0.56f, w * 0.59f, h * 0.60f)
                    cubicTo(w * 0.56f, h * 0.66f, w * 0.46f, h * 0.70f, w * 0.40f, h * 0.68f)
                    close()
                }
                drawPath(qeshmPath, color = Color(0xFFECD3A0))
                drawPath(qeshmPath, color = Color(0xFF9C6644), style = Stroke(width = 2.5f))

                // Stylized Abu Musa Island (southern Persian Gulf)
                drawCircle(color = Color(0xFFECD3A0), radius = 20f, center = Offset(w * 0.38f, h * 0.84f))
                drawCircle(color = Color(0xFF9C6644), radius = 20f, center = Offset(w * 0.38f, h * 0.84f), style = Stroke(width = 2.5f))

                // Dotted Explorer Routes linking Counties
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                HormozganCatalog.allCounties.forEach { fromCounty ->
                    val fromUnlocked = progressMap[fromCounty.id]?.isUnlocked == true
                    fromCounty.unlocksNextIds.forEach { targetId ->
                        val toCounty = HormozganCatalog.getById(targetId)
                        drawLine(
                            color = if (fromUnlocked) WarmSepiaInk.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.25f),
                            start = Offset(fromCounty.mapX * w, fromCounty.mapY * h),
                            end = Offset(toCounty.mapX * w, toCounty.mapY * h),
                            strokeWidth = 3f,
                            pathEffect = dashEffect,
                        )
                    }
                }
            }

            // Interactive Pins for all 13 Counties
            val pinHalfW = with(density) { 42.dp.toPx() }
            val pinHalfH = with(density) { 24.dp.toPx() }

            HormozganCatalog.allCounties.forEach { county ->
                val state = progressMap[county.id]
                val isUnlocked = state?.isUnlocked ?: (county.id == "bashagard")
                val isCompleted = state?.isCompleted == true
                val isSelected = selectedCounty.id == county.id

                val px = (county.mapX * mapWidthPx - pinHalfW).coerceIn(4f, mapWidthPx - pinHalfW * 2.1f)
                val py = (county.mapY * mapHeightPx - pinHalfH).coerceIn(4f, mapHeightPx - pinHalfH * 2.1f)

                val chipBg = when {
                    isCompleted -> OasisGreen
                    isUnlocked -> SunlitGold
                    else -> Color(0xFF475569).copy(alpha = 0.88f)
                }
                val chipText = when {
                    isCompleted -> Color.White
                    isUnlocked -> WarmSepiaInk
                    else -> Color.White.copy(alpha = 0.85f)
                }

                Surface(
                    modifier = Modifier
                        .offset { IntOffset(px.roundToInt(), py.roundToInt()) }
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = if (isSelected) 2.5.dp else 1.2.dp,
                            color = if (isSelected) Color.White else ParchmentBorder,
                            shape = RoundedCornerShape(16.dp),
                        )
                        .clickable { selectedCounty = county }
                        .testTag("map_county_${county.id}"),
                    shape = RoundedCornerShape(16.dp),
                    color = chipBg,
                    shadowElevation = if (isSelected) 10.dp else 4.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = when {
                                isCompleted -> Icons.Rounded.CheckCircle
                                isUnlocked -> Icons.Rounded.Explore
                                else -> Icons.Rounded.Lock
                            },
                            contentDescription = null,
                            tint = chipText,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = county.nameFa,
                            style = MaterialTheme.typography.labelSmall,
                            color = chipText,
                        )
                    }
                }
            }

            // Sea Labels for Persian Gulf & Sea of Oman
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(10.dp),
                color = DeepGulfTeal.copy(alpha = 0.72f),
            ) {
                Text(
                    text = "خلیج فارس  •  تنگه هرمز  •  دریای عمان",
                    style = MaterialTheme.typography.labelSmall,
                    color = ParchmentCream,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selected County Detail Card
        val selState = progressMap[selectedCounty.id]
        val selUnlocked = selState?.isUnlocked ?: (selectedCounty.id == "bashagard")
        val selCompleted = selState?.isCompleted == true
        val doneMissions = selState?.completedMissionIds?.size ?: 0

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, WarmAmberGold, RoundedCornerShape(22.dp)),
            shape = RoundedCornerShape(22.dp),
            color = ParchmentCream,
            shadowElevation = 8.dp,
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(DeepGulfTeal.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    LocalNpcCanvas(
                        appearance = selectedCounty.npcAppearance,
                        facingLeft = true,
                        modifier = Modifier.size(66.dp),
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${selectedCounty.order.toPersianNumber()}. ${selectedCounty.nameFa}؛ ${selectedCounty.stageTitle}",
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepGulfTeal,
                        )
                    }
                    Text(
                        text = "${selectedCounty.subtitle}  |  شخصیت محلی: ${selectedCounty.npcName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmSepiaInk.copy(alpha = 0.85f),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (selUnlocked) {
                            "مأموریت‌ها: ${doneMissions.toPersianNumber()} از ${selectedCounty.missions.size.toPersianNumber()}  •  قطعه گنج: ${selectedCounty.treasurePiece.title}"
                        } else {
                            "برای باز شدن این شهرستان، مراحل قبلی را تکمیل کنید."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selCompleted) OasisGreen else TerracottaOchre,
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = { onSelectCountyToPlay(selectedCounty) },
                    enabled = selUnlocked,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selCompleted) OasisGreen else GulfTurquoise,
                    ),
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("map_enter_county_button"),
                ) {
                    Text(
                        text = when {
                            !selUnlocked -> "قفل است"
                            selCompleted -> "ورود مجدد"
                            else -> "شروع مرحله"
                        },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
