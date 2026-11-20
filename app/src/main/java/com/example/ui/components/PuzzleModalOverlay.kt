package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.audio.SoundEffect
import com.example.data.catalog.HormozganCatalog
import com.example.model.AriaAnimationState
import com.example.model.CountyMission
import com.example.model.PuzzlePayload
import com.example.model.SlopeSegment
import com.example.ui.characters.AriaCharacterCanvas
import com.example.ui.theme.CrimsonCliff
import com.example.ui.theme.DeepGulfTeal
import com.example.ui.theme.GulfTurquoise
import com.example.ui.theme.MangroveMint
import com.example.ui.theme.OasisGreen
import com.example.ui.theme.ParchmentAged
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentCream
import com.example.ui.theme.SunlitGold
import com.example.ui.theme.TerracottaOchre
import com.example.ui.theme.WarmAmberGold
import com.example.ui.theme.WarmSepiaInk
import com.example.util.toPersianDigits
import com.example.util.toPersianNumber
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun PuzzleModalOverlay(
    mission: CountyMission,
    onPlaySfx: (SoundEffect) -> Unit,
    onSpeakCharacterLine: (id: String, text: String) -> Unit = { _, _ -> },
    onStopSpeech: () -> Unit = {},
    onSolved: () -> Unit,
    onDismiss: () -> Unit,
) {
    var solved by remember(mission.id) { mutableStateOf(false) }
    var feedbackMessage by remember(mission.id) { mutableStateOf<String?>(null) }
    var ariaEmotion by remember(mission.id) { mutableStateOf(AriaAnimationState.THINK) }

    fun markSuccess(customNote: String? = null) {
        solved = true
        ariaEmotion = AriaAnimationState.DISCOVER_CLUE
        val resolvedText = customNote ?: mission.successMessage
        val resolvedId = if (customNote == null) {
            "${mission.id}_success"
        } else {
            HormozganCatalog.missionFeedbackLineId(mission.id, "solved", resolvedText)
        }
        feedbackMessage = resolvedText
        onPlaySfx(SoundEffect.CLUE_FOUND)
        onSpeakCharacterLine(resolvedId, resolvedText)
    }

    fun markTryAgain(note: String) {
        ariaEmotion = AriaAnimationState.SAD
        feedbackMessage = note
        onPlaySfx(SoundEffect.ERROR_GENTLE)
        onSpeakCharacterLine(
            HormozganCatalog.missionFeedbackLineId(mission.id, "hint", note),
            note,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.56f))
            .padding(14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 700.dp)
                .fillMaxWidth()
                .border(2.5.dp, WarmAmberGold, RoundedCornerShape(26.dp)),
            shape = RoundedCornerShape(26.dp),
            color = ParchmentCream,
            shadowElevation = 16.dp,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
            ) {
                // Header with Aria portrait & close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(DeepGulfTeal.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            AriaCharacterCanvas(
                                animationState = ariaEmotion,
                                facingRight = true,
                                modifier = Modifier.size(58.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "مأموریت ${mission.orderIndex.toPersianNumber()}: ${mission.title}",
                                style = MaterialTheme.typography.titleLarge,
                                color = DeepGulfTeal,
                            )
                            Text(
                                text = mission.shortGoal,
                                style = MaterialTheme.typography.bodySmall,
                                color = WarmSepiaInk.copy(alpha = 0.85f),
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            onSpeakCharacterLine("${mission.id}_goal", mission.shortGoal)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("puzzle_speak_goal_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "پخش صدای راهنما",
                            tint = DeepGulfTeal,
                        )
                    }

                    IconButton(
                        onClick = {
                            onStopSpeech()
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("puzzle_close_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "بستن معما",
                            tint = WarmSepiaInk,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Puzzle Body
                when (val payload = mission.puzzlePayload) {
                    is PuzzlePayload.PathGrid -> PathGridPuzzleBoard(
                        payload = payload,
                        solved = solved,
                        onStep = { onPlaySfx(SoundEffect.OBJECT_INTERACT) },
                        onBlocked = { markTryAgain(HormozganCatalog.PuzzleVoiceHints.PATH_GRID_BLOCKED) },
                        onSolved = { markSuccess() },
                    )
                    is PuzzlePayload.WaterSlope -> WaterSlopePuzzleBoard(
                        payload = payload,
                        solved = solved,
                        onTap = { onPlaySfx(SoundEffect.OBJECT_INTERACT) },
                        onWrong = { markTryAgain(HormozganCatalog.PuzzleVoiceHints.WATER_SLOPE_WRONG) },
                        onSolved = { markSuccess(payload.explanation) },
                    )
                    is PuzzlePayload.HiddenSpots -> HiddenObjectPuzzleBoard(
                        payload = payload,
                        solved = solved,
                        onInspect = { msg, found ->
                            if (found) {
                                markSuccess(msg)
                            } else {
                                ariaEmotion = AriaAnimationState.LOOK_AROUND
                                feedbackMessage = msg
                                onPlaySfx(SoundEffect.OBJECT_INTERACT)
                                onSpeakCharacterLine(
                                    HormozganCatalog.missionFeedbackLineId(mission.id, "spot", msg),
                                    msg,
                                )
                            }
                        },
                    )
                    is PuzzlePayload.PatternMatrix -> PatternMatrixPuzzleBoard(
                        payload = payload,
                        solved = solved,
                        onToggle = { onPlaySfx(SoundEffect.OBJECT_INTERACT) },
                        onSolved = { markSuccess() },
                    )
                    is PuzzlePayload.SequenceItems -> SequencePuzzleBoard(
                        payload = payload,
                        solved = solved,
                        onStep = { onPlaySfx(SoundEffect.OBJECT_INTERACT) },
                        onWrong = { markTryAgain(HormozganCatalog.PuzzleVoiceHints.SEQUENCE_WRONG) },
                        onSolved = { markSuccess() },
                    )
                    is PuzzlePayload.InteractiveChallenge -> ChallengeOptionsBoard(
                        payload = payload,
                        solved = solved,
                        onWrong = { msg -> markTryAgain(msg) },
                        onSolved = { msg -> markSuccess(msg) },
                    )
                    is PuzzlePayload.MechanismBalance -> MechanismBalanceBoard(
                        payload = payload,
                        solved = solved,
                        onAdjust = { onPlaySfx(SoundEffect.OBJECT_INTERACT) },
                        onSolved = { markSuccess() },
                    )
                }

                // Live Feedback & Encouragement Banner
                AnimatedVisibility(
                    visible = feedbackMessage != null,
                    enter = fadeIn() + scaleIn(initialScale = 0.94f),
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = if (solved) OasisGreen.copy(alpha = 0.14f) else TerracottaOchre.copy(alpha = 0.13f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (solved) OasisGreen else TerracottaOchre,
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = if (solved) Icons.Rounded.AutoAwesome else Icons.Rounded.Search,
                                contentDescription = null,
                                tint = if (solved) OasisGreen else TerracottaOchre,
                                modifier = Modifier.size(26.dp),
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                if (solved) {
                                    Text(
                                        text = "سرنخ جدید کشف شد: ${mission.clueRewardTitle}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = OasisGreen,
                                    )
                                }
                                Text(
                                    text = feedbackMessage.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = WarmSepiaInk,
                                )
                            }
                        }
                    }
                }

                if (solved) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            onStopSpeech()
                            onSolved()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("puzzle_collect_clue_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GulfTurquoise),
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ثبت سرنخ در دفترچه و ادامه",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PathGridPuzzleBoard(
    payload: PuzzlePayload.PathGrid,
    solved: Boolean,
    onStep: () -> Unit,
    onBlocked: () -> Unit,
    onSolved: () -> Unit,
) {
    val startIdx = payload.cells.indexOf(2).coerceAtLeast(0)
    val visited = remember(payload) { mutableStateListOf(startIdx) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = payload.hintText,
            style = MaterialTheme.typography.bodyMedium,
            color = WarmSepiaInk,
        )
        Spacer(modifier = Modifier.height(10.dp))

        for (r in 0 until payload.rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (c in 0 until payload.cols) {
                    val idx = r * payload.cols + c
                    val cellType = payload.cells[idx]
                    val isVisited = idx in visited
                    val last = visited.last()
                    val lastR = last / payload.cols
                    val lastC = last % payload.cols
                    val isAdjacent = abs(lastR - r) + abs(lastC - c) == 1

                    val bgColor = when {
                        cellType == 1 -> CrimsonCliff.copy(alpha = 0.78f)
                        cellType == 3 && isVisited -> OasisGreen
                        cellType == 3 -> GulfTurquoise
                        cellType == 2 -> OasisGreen
                        isVisited -> SunlitGold
                        else -> ParchmentAged
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(bgColor)
                            .border(
                                width = if (isAdjacent && cellType != 1 && !solved) 2.dp else 1.dp,
                                color = if (isAdjacent && cellType != 1) DeepGulfTeal else ParchmentBorder,
                                shape = RoundedCornerShape(14.dp),
                            )
                            .clickable(enabled = !solved) {
                                if (cellType == 1) {
                                    onBlocked()
                                } else if (isAdjacent && !isVisited) {
                                    visited.add(idx)
                                    onStep()
                                    if (cellType == 3) {
                                        onSolved()
                                    }
                                } else if (isVisited && idx != last) {
                                    val cut = visited.indexOf(idx)
                                    while (visited.size > cut + 1) visited.removeAt(visited.lastIndex)
                                    onStep()
                                } else if (!isAdjacent && !isVisited) {
                                    onBlocked()
                                }
                            }
                            .testTag("path_cell_$idx"),
                        contentAlignment = Alignment.Center,
                    ) {
                        val label = when {
                            cellType == 2 -> "شروع"
                            cellType == 3 -> "مقصد"
                            cellType == 1 -> "صخره"
                            isVisited -> "✓"
                            else -> "هموار"
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (cellType == 1 || cellType == 2 || cellType == 3) Color.White else WarmSepiaInk,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (!solved) {
            OutlinedButton(
                onClick = {
                    visited.clear()
                    visited.add(startIdx)
                },
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("شروع دوباره مسیر", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun WaterSlopePuzzleBoard(
    payload: PuzzlePayload.WaterSlope,
    solved: Boolean,
    onTap: () -> Unit,
    onWrong: () -> Unit,
    onSolved: () -> Unit,
) {
    val sortedDesc = remember(payload) {
        payload.channelSegments.sortedByDescending { it.elevationMeters }
    }
    val chosen = remember(payload) { mutableStateListOf<SlopeSegment>() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "قطعه‌های آبراهه را به ترتیب از بلندترین ارتفاع (بالای کوه) تا پایین‌ترین ارتفاع (باغ) انتخاب کن تا آب جاری شود:",
            style = MaterialTheme.typography.bodyMedium,
            color = WarmSepiaInk,
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Visual slope channel bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(DeepGulfTeal.copy(alpha = 0.08f))
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (slot in 0 until sortedDesc.size) {
                val seg = chosen.getOrNull(slot)
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = if (seg != null) GulfTurquoise else ParchmentAged,
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = if (seg != null) "${seg.elevationMeters.toPersianNumber()} متر" else "جایگاه ${(slot + 1).toPersianNumber()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (seg != null) Color.White else WarmSepiaInk.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        payload.channelSegments.forEach { seg ->
            val alreadyPicked = seg in chosen
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(enabled = !solved && !alreadyPicked) {
                        val expected = sortedDesc[chosen.size]
                        if (seg.id == expected.id) {
                            chosen.add(seg)
                            onTap()
                            if (chosen.size == sortedDesc.size) {
                                onSolved()
                            }
                        } else {
                            onWrong()
                        }
                    }
                    .testTag("slope_segment_${seg.id}"),
                shape = RoundedCornerShape(14.dp),
                color = if (alreadyPicked) OasisGreen.copy(alpha = 0.18f) else ParchmentAged,
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (alreadyPicked) OasisGreen else ParchmentBorder,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.WaterDrop,
                            contentDescription = null,
                            tint = GulfTurquoise,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = seg.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = WarmSepiaInk,
                        )
                    }
                    Text(
                        text = "ارتفاع: ${seg.elevationMeters.toPersianNumber()} متر",
                        style = MaterialTheme.typography.labelMedium,
                        color = DeepGulfTeal,
                    )
                }
            }
        }
    }
}

@Composable
private fun HiddenObjectPuzzleBoard(
    payload: PuzzlePayload.HiddenSpots,
    solved: Boolean,
    onInspect: (String, Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "بخش‌های مختلف محیط را لمس و بررسی کن تا «${payload.targetItemName}» را پیدا کنی:",
            style = MaterialTheme.typography.bodyMedium,
            color = WarmSepiaInk,
        )
        Spacer(modifier = Modifier.height(10.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, ParchmentBorder, RoundedCornerShape(20.dp)),
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()

            // Illustrated discovery scene backdrop
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFBAE6FD), Color(0xFFFEF3C7), Color(0xFFD6B47B)),
                    ),
                )
                // Distant stylized cliffs & foliage silhouettes
                drawCircle(
                    color = TerracottaOchre.copy(alpha = 0.55f),
                    radius = size.height * 0.45f,
                    center = Offset(size.width * 0.78f, size.height * 0.68f),
                )
                drawCircle(
                    color = OasisGreen.copy(alpha = 0.65f),
                    radius = size.height * 0.28f,
                    center = Offset(size.width * 0.22f, size.height * 0.75f),
                )
                drawRect(
                    color = Color(0xFF0E7490).copy(alpha = 0.35f),
                    topLeft = Offset(0f, size.height * 0.72f),
                    size = size.copy(height = size.height * 0.28f),
                )
            }

            payload.spots.forEach { spot ->
                val xOff = (spot.xFraction * widthPx - 64f).coerceIn(8f, widthPx - 140f)
                val yOff = (spot.yFraction * heightPx - 28f).coerceIn(8f, heightPx - 60f)

                Surface(
                    modifier = Modifier
                        .offset { IntOffset(xOff.roundToInt(), yOff.roundToInt()) }
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(enabled = !solved) {
                            onInspect(spot.inspectionText, spot.isTarget)
                        }
                        .testTag("hidden_spot_${spot.id}"),
                    shape = RoundedCornerShape(16.dp),
                    color = if (solved && spot.isTarget) SunlitGold else ParchmentCream.copy(alpha = 0.92f),
                    shadowElevation = 6.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (solved && spot.isTarget) OasisGreen else GulfTurquoise,
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = spot.label,
                            tint = DeepGulfTeal,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = spot.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = WarmSepiaInk,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PatternMatrixPuzzleBoard(
    payload: PuzzlePayload.PatternMatrix,
    solved: Boolean,
    onToggle: () -> Unit,
    onSolved: () -> Unit,
) {
    val cells = remember(payload) {
        mutableStateListOf<Boolean>().apply { addAll(payload.initialPattern) }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "الگوی راست را مثل نمونه «${payload.motifName}» در سمت چپ تکمیل کن:",
            style = MaterialTheme.typography.bodyMedium,
            color = WarmSepiaInk,
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Target motif preview
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("الگوی نمونه", style = MaterialTheme.typography.labelMedium, color = DeepGulfTeal)
                Spacer(modifier = Modifier.height(6.dp))
                for (r in 0 until payload.size) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (c in 0 until payload.size) {
                            val on = payload.targetPattern[r * payload.size + c]
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (on) WarmAmberGold else ParchmentAged)
                                    .border(1.dp, ParchmentBorder, RoundedCornerShape(6.dp)),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Interactive player grid
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("بافت تو", style = MaterialTheme.typography.labelMedium, color = GulfTurquoise)
                Spacer(modifier = Modifier.height(6.dp))
                for (r in 0 until payload.size) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (c in 0 until payload.size) {
                            val idx = r * payload.size + c
                            val on = cells[idx]
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (on) GulfTurquoise else ParchmentAged)
                                    .border(1.5.dp, DeepGulfTeal, RoundedCornerShape(10.dp))
                                    .clickable(enabled = !solved) {
                                        cells[idx] = !cells[idx]
                                        onToggle()
                                        if (cells.toList() == payload.targetPattern) {
                                            onSolved()
                                        }
                                    }
                                    .testTag("pattern_cell_$idx"),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun SequencePuzzleBoard(
    payload: PuzzlePayload.SequenceItems,
    solved: Boolean,
    onStep: () -> Unit,
    onWrong: () -> Unit,
    onSolved: () -> Unit,
) {
    val shuffled = remember(payload) {
        listOf(
            payload.correctOrder[2],
            payload.correctOrder[0],
            payload.correctOrder[3],
            payload.correctOrder[1],
        )
    }
    val selected = remember(payload) { mutableStateListOf<String>() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = payload.promptNote,
            style = MaterialTheme.typography.bodyMedium,
            color = WarmSepiaInk,
        )
        Spacer(modifier = Modifier.height(10.dp))

        shuffled.forEach { step ->
            val orderPos = selected.indexOf(step)
            val isPicked = orderPos >= 0
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(enabled = !solved && !isPicked) {
                        val expected = payload.correctOrder[selected.size]
                        if (step == expected) {
                            selected.add(step)
                            onStep()
                            if (selected.size == payload.correctOrder.size) {
                                onSolved()
                            }
                        } else {
                            onWrong()
                        }
                    },
                shape = RoundedCornerShape(14.dp),
                color = if (isPicked) OasisGreen.copy(alpha = 0.16f) else ParchmentAged,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isPicked) OasisGreen else ParchmentBorder),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (isPicked) OasisGreen else DeepGulfTeal.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (isPicked) (orderPos + 1).toPersianNumber() else "؟",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isPicked) Color.White else DeepGulfTeal,
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = step.toPersianDigits(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmSepiaInk,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChallengeOptionsBoard(
    payload: PuzzlePayload.InteractiveChallenge,
    solved: Boolean,
    onWrong: (String) -> Unit,
    onSolved: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = DeepGulfTeal.copy(alpha = 0.08f),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = payload.visualCueTitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = GulfTurquoise,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = payload.question,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = WarmSepiaInk,
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        payload.options.forEach { opt ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(enabled = !solved) {
                        if (opt.isCorrect) {
                            onSolved(opt.feedbackText)
                        } else {
                            onWrong(opt.feedbackText)
                        }
                    }
                    .testTag("challenge_option_${opt.id}"),
                shape = RoundedCornerShape(14.dp),
                color = if (solved && opt.isCorrect) OasisGreen.copy(alpha = 0.18f) else ParchmentAged,
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (solved && opt.isCorrect) OasisGreen else ParchmentBorder,
                ),
            ) {
                Text(
                    text = opt.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmSepiaInk,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun MechanismBalanceBoard(
    payload: PuzzlePayload.MechanismBalance,
    solved: Boolean,
    onAdjust: () -> Unit,
    onSolved: () -> Unit,
) {
    val levels = remember(payload) {
        mutableStateListOf<Int>().apply { repeat(payload.elements.size) { add(1) } }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = payload.targetHint,
            style = MaterialTheme.typography.bodyMedium,
            color = WarmSepiaInk,
        )
        Spacer(modifier = Modifier.height(10.dp))

        payload.elements.forEachIndexed { index, element ->
            val current = levels[index]
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                color = ParchmentAged,
                border = androidx.compose.foundation.BorderStroke(1.dp, ParchmentBorder),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = element.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmSepiaInk,
                        modifier = Modifier.weight(1f),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (lvl in 1..3) {
                            Surface(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(enabled = !solved) {
                                        levels[index] = lvl
                                        onAdjust()
                                        val allMatched = payload.elements.indices.all { i ->
                                            levels[i] == payload.elements[i].targetLevel
                                        }
                                        if (allMatched) onSolved()
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (current == lvl) GulfTurquoise else ParchmentCream,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, DeepGulfTeal),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = lvl.toPersianNumber(),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (current == lvl) Color.White else DeepGulfTeal,
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
