package com.example.ui.stage

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundEffect
import com.example.audio.StageVoiceStatus
import com.example.data.catalog.HormozganCatalog
import com.example.model.AriaAnimationState
import com.example.model.CountyDefinition
import com.example.model.CountyProgressState
import com.example.ui.characters.AriaCharacterCanvas
import com.example.ui.characters.LocalNpcCanvas
import com.example.ui.components.AdventureActionButtons
import com.example.ui.components.CartoonDialogueOverlay
import com.example.ui.components.CharacterNavigationController
import com.example.ui.components.PuzzleModalOverlay
import com.example.ui.components.StageNavigatorCompassBar
import com.example.ui.theme.DeepGulfTeal
import com.example.ui.theme.GulfTurquoise
import com.example.ui.theme.OasisGreen
import com.example.ui.theme.ParchmentCream
import com.example.ui.theme.PersianNastaliqFamily
import com.example.ui.theme.SunlitGold
import com.example.ui.theme.WarmAmberGold
import com.example.ui.theme.WarmSepiaInk
import com.example.util.toPersianNumber
import com.example.viewmodel.ActiveDialogueState
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

@Composable
fun AdventureStageScreen(
    county: CountyDefinition,
    progress: CountyProgressState,
    initialPlayerX: Float,
    activeDialogue: ActiveDialogueState?,
    isSpeaking: Boolean,
    isPreparingVoice: Boolean = false,
    mouthOpenLevel: Float,
    voiceSourceLabel: String,
    stageVoiceStatus: StageVoiceStatus? = null,
    viewModel: GameViewModel,
    activeMissionPuzzle: com.example.model.CountyMission?,
    rewardCelebrationCounty: CountyDefinition?,
    onOpenJournal: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    // Section 15: Adventure gameplay stages prefer landscape orientation
    val context = LocalContext.current
    DisposableEffect(county.id) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    var playerX by remember(county.id) {
        mutableFloatStateOf(initialPlayerX.coerceIn(WORLD_MIN_X, WORLD_MAX_X))
    }
    var cameraX by remember(county.id) { mutableFloatStateOf((playerX - 320f).coerceIn(0f, WORLD_WIDTH - 700f)) }
    var viewportWidthState by remember(county.id) { mutableFloatStateOf(960f) }
    var facingRight by remember(county.id) { mutableStateOf(true) }
    var joyAxisX by remember(county.id) { mutableFloatStateOf(0f) }
    var joyFast by remember(county.id) { mutableStateOf(false) }
    var manualRunMode by remember(county.id) { mutableStateOf(false) }
    var velocityX by remember(county.id) { mutableFloatStateOf(0f) }
    var autoTargetX by remember(county.id) { mutableStateOf<Float?>(null) }
    var isJumping by remember(county.id) { mutableStateOf(false) }
    var jumpYOffset by remember(county.id) { mutableFloatStateOf(0f) }
    var temporaryEmotion by remember(county.id) { mutableStateOf<AriaAnimationState?>(null) }

    // Trigger opening dialogue the very first time player meets the local character
    // and pre-cache this county's character voices offline in the background
    LaunchedEffect(county.id) {
        viewModel.precacheCountyVoices(county)
        if (!progress.npcMet) {
            viewModel.markNpcMet(county.id)
            viewModel.startDialogue(county, county.openingDialogues)
        }
    }

    // Smooth 60 FPS character movement, velocity easing, auto-guide walk, jump arc & camera tracking
    LaunchedEffect(county.id) {
        var jumpVel = 0f
        var saveCounter = 0
        while (true) {
            val modalBlocking = activeDialogue != null || activeMissionPuzzle != null || rewardCelebrationCounty != null
            if (!modalBlocking) {
                val desiredAxis = when {
                    abs(joyAxisX) > 0.08f -> {
                        autoTargetX = null
                        joyAxisX
                    }
                    autoTargetX != null -> {
                        val diff = autoTargetX!! - playerX
                        if (abs(diff) <= 55f) {
                            autoTargetX = null
                            0f
                        } else {
                            sign(diff) * 0.85f
                        }
                    }
                    else -> 0f
                }

                val running = manualRunMode || joyFast || (autoTargetX != null)
                val maxSpeed = if (running) 7.8f else 4.5f
                val targetVel = desiredAxis * maxSpeed
                velocityX += (targetVel - velocityX) * 0.28f
                if (abs(velocityX) < 0.08f && desiredAxis == 0f) {
                    velocityX = 0f
                }

                if (abs(velocityX) > 0.15f) {
                    val nextX = (playerX + velocityX).coerceIn(WORLD_MIN_X, WORLD_MAX_X)
                    if (nextX != playerX) {
                        playerX = nextX
                        facingRight = velocityX >= 0f
                        viewModel.soundEngine.playFootstep()
                    }
                }
            } else {
                velocityX = 0f
                autoTargetX = null
            }

            if (isJumping) {
                if (jumpYOffset == 0f && jumpVel == 0f) {
                    jumpVel = -11.8f
                }
                jumpYOffset += jumpVel
                jumpVel += 0.84f
                if (jumpYOffset >= 0f) {
                    jumpYOffset = 0f
                    jumpVel = 0f
                    isJumping = false
                }
            }

            // Smooth camera interpolation at 60fps inside loop
            val targetCam = (playerX - viewportWidthState * 0.42f)
                .coerceIn(0f, (WORLD_WIDTH - viewportWidthState).coerceAtLeast(0f))
            cameraX += (targetCam - cameraX) * 0.14f

            saveCounter++
            if (saveCounter >= 45) {
                saveCounter = 0
                viewModel.savePlayerX(county.id, playerX)
            }
            delay(16L)
        }
    }

    // Clear temporary reaction after brief beat
    LaunchedEffect(temporaryEmotion) {
        if (temporaryEmotion != null) {
            delay(1500L)
            temporaryEmotion = null
        }
    }

    val isMoving = activeDialogue == null && activeMissionPuzzle == null && abs(velocityX) > 0.45f
    val ariaAnimState = when {
        rewardCelebrationCounty != null -> AriaAnimationState.RECEIVE_REWARD
        activeDialogue?.currentLine?.isAria == true && isSpeaking -> AriaAnimationState.TALK
        activeDialogue?.currentLine != null -> activeDialogue.currentLine!!.ariaEmotion
        temporaryEmotion != null -> temporaryEmotion!!
        isJumping -> AriaAnimationState.JUMP
        isMoving && (manualRunMode || joyFast || autoTargetX != null) -> AriaAnimationState.RUN
        isMoving -> AriaAnimationState.WALK
        else -> AriaAnimationState.IDLE
    }

    // Nearest interactable NPC or Mission Shrine
    val nearNpc = abs(playerX - county.npcWorldX) < 125f
    val nearbyMission = county.missions.minByOrNull { abs(playerX - it.worldX) }
        ?.takeIf { abs(playerX - it.worldX) < 125f }

    val interactLabel = when {
        nearbyMission != null && nearbyMission.id !in progress.completedMissionIds -> "بررسی مأموریت"
        nearbyMission != null -> "بازبینی سرنخ"
        nearNpc -> "گفتگو با ${county.npcName}"
        else -> null
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepGulfTeal),
    ) {
        val density = LocalDensity.current
        val viewportWidthPx = constraints.maxWidth.toFloat()
        val viewportHeightPx = constraints.maxHeight.toFloat()
        viewportWidthState = viewportWidthPx

        val groundPx = viewportHeightPx * 0.77f
        val spriteSizeDp = 128.dp
        val spriteSizePx = with(density) { spriteSizeDp.toPx() }

        // World stage MUST use physical Left-to-Right coordinates so canvas scenery,
        // playerX movement direction, facingRight orientation, and touch controller match 1:1.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(modifier = Modifier.fillMaxSize()) {
                // 1. Multi-layer 2D Cartoon World Scenery
                BiomeSceneryCanvas(
                    county = county,
                    cameraX = cameraX,
                    completedMissionIds = progress.completedMissionIds,
                    modifier = Modifier.fillMaxSize(),
                )

                // 2. Local Character (NPC) in World
                val npcScreenX = county.npcWorldX - cameraX - spriteSizePx * 0.5f
                if (npcScreenX in -spriteSizePx..(viewportWidthPx + spriteSizePx)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .absoluteOffset {
                                IntOffset(
                                    npcScreenX.roundToInt(),
                                    (groundPx - spriteSizePx * 0.90f - 28f).roundToInt(),
                                )
                            }
                            .clickable {
                                viewModel.markNpcMet(county.id)
                                val lines = if (progress.isCompleted) county.farewellDialogues else county.openingDialogues
                                viewModel.startDialogue(county, lines)
                            }
                            .testTag("world_npc_${county.id}"),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (nearNpc) SunlitGold else ParchmentCream.copy(alpha = 0.92f),
                            shadowElevation = 4.dp,
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Text(
                                    text = if (nearNpc) "${county.npcName} (ضربه برای گفتگو)" else county.npcName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (nearNpc) WarmSepiaInk else DeepGulfTeal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                )
                            }
                        }
                        LocalNpcCanvas(
                            appearance = county.npcAppearance,
                            isSpeaking = isSpeaking && activeDialogue?.currentLine?.isAria == false,
                            mouthOpenLevel = mouthOpenLevel,
                            facingLeft = playerX < county.npcWorldX,
                            modifier = Modifier.size(spriteSizeDp),
                        )
                    }
                }

                // 3. Mission Floating Labels in World (tappable directly or via Interact button)
                county.missions.forEach { mission ->
                    val mx = mission.worldX - cameraX
                    if (mx in -140f..(viewportWidthPx + 140f)) {
                        val done = mission.id in progress.completedMissionIds
                        val isHighlighted = nearbyMission?.id == mission.id
                        Surface(
                            modifier = Modifier
                                .absoluteOffset {
                                    IntOffset(
                                        (mx - 68f).roundToInt(),
                                        (groundPx - 124f).roundToInt(),
                                    )
                                }
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = if (isHighlighted) 2.dp else 1.dp,
                                    color = if (isHighlighted) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp),
                                )
                                .clickable {
                                    if (done) {
                                        viewModel.openMissionPuzzle(mission)
                                    } else {
                                        viewModel.startDialogue(county, mission.introDialogues) {
                                            viewModel.openMissionPuzzle(mission)
                                        }
                                    }
                                }
                                .testTag("world_mission_${mission.id}"),
                            shape = RoundedCornerShape(14.dp),
                            color = if (done) OasisGreen else SunlitGold,
                            shadowElevation = if (isHighlighted) 10.dp else 5.dp,
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    if (done) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = "انجام شده",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = if (isHighlighted && !done) "بررسی مأموریت ${mission.orderIndex.toPersianNumber()}" else "مأموریت ${mission.orderIndex.toPersianNumber()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (done) Color.White else WarmSepiaInk,
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Aria (Main Character) in World
                val ariaScreenX = playerX - cameraX - spriteSizePx * 0.5f
                AriaCharacterCanvas(
                    animationState = ariaAnimState,
                    facingRight = facingRight,
                    mouthOpenLevel = if (activeDialogue?.currentLine?.isAria == true && isSpeaking) mouthOpenLevel else 0f,
                    modifier = Modifier
                        .size(spriteSizeDp)
                        .absoluteOffset {
                            IntOffset(
                                ariaScreenX.roundToInt(),
                                (groundPx - spriteSizePx * 0.90f + jumpYOffset).roundToInt(),
                            )
                        },
                )
            }
        }

        // 5. Top Stage HUD Bar + World Radar Compass
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            // Stage Title + County Badge
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = ParchmentCream.copy(alpha = 0.92f),
                shadowElevation = 6.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onOpenMap,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("stage_back_map_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "بازگشت به نقشه",
                            tint = DeepGulfTeal,
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "${county.nameFa}؛ ${county.stageTitle}",
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepGulfTeal,
                        )
                        Text(
                            text = "مأموریت‌ها: ${progress.completedMissionIds.size.toPersianNumber()} از ${county.missions.size.toPersianNumber()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = WarmSepiaInk.copy(alpha = 0.8f),
                        )
                    }
                }
            }

            // Center: Live Stage Navigator Radar & Objective Guide
            StageNavigatorCompassBar(
                county = county,
                progress = progress,
                playerX = playerX,
                onGuideToWorldX = { targetX ->
                    autoTargetX = targetX
                    viewModel.soundEngine.playSfx(SoundEffect.OBJECT_INTERACT)
                },
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            // Right: Scientific Info + Map + Settings
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (progress.isCompleted) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onOpenJournal() }
                            .testTag("stage_science_button"),
                        shape = RoundedCornerShape(14.dp),
                        color = GulfTurquoise,
                        shadowElevation = 5.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Science, contentDescription = "اطلاعات علمی", tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("اطلاعات علمی", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenMap)
                        .testTag("stage_map_button"),
                    shape = CircleShape,
                    color = ParchmentCream.copy(alpha = 0.92f),
                    shadowElevation = 4.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Map, contentDescription = "نقشه هرمزگان", tint = DeepGulfTeal)
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenSettings)
                        .testTag("stage_settings_button"),
                    shape = CircleShape,
                    color = ParchmentCream.copy(alpha = 0.92f),
                    shadowElevation = 4.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Settings, contentDescription = "تنظیمات صدا", tint = DeepGulfTeal)
                    }
                }
            }
        }

        // 6. Ergonomic Two-Thumb On-Screen Controls (Physical Left = Actions, Physical Right = Navigation Controller)
        if (activeDialogue == null && activeMissionPuzzle == null && rewardCelebrationCounty == null) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    AdventureActionButtons(
                        interactLabel = interactLabel,
                        isRunMode = manualRunMode,
                        onToggleRun = {
                            manualRunMode = !manualRunMode
                            viewModel.soundEngine.playSfx(SoundEffect.MENU_BUTTON)
                        },
                        onJump = {
                            if (!isJumping) {
                                isJumping = true
                                viewModel.soundEngine.playSfx(SoundEffect.OBJECT_INTERACT)
                            }
                        },
                        onInteract = {
                            when {
                                nearbyMission != null -> {
                                    temporaryEmotion = AriaAnimationState.OPEN_NOTEBOOK
                                    if (nearbyMission.id in progress.completedMissionIds) {
                                        viewModel.openMissionPuzzle(nearbyMission)
                                    } else {
                                        viewModel.startDialogue(county, nearbyMission.introDialogues) {
                                            viewModel.openMissionPuzzle(nearbyMission)
                                        }
                                    }
                                }
                                nearNpc -> {
                                    viewModel.markNpcMet(county.id)
                                    val lines = if (progress.isCompleted) county.farewellDialogues else county.openingDialogues
                                    viewModel.startDialogue(county, lines)
                                }
                            }
                        },
                        onOpenJournal = {
                            temporaryEmotion = AriaAnimationState.OPEN_NOTEBOOK
                            viewModel.soundEngine.playSfx(SoundEffect.OPEN_JOURNAL)
                            onOpenJournal()
                        },
                        modifier = Modifier.align(Alignment.BottomStart),
                    )

                    CharacterNavigationController(
                        onMoveChanged = { axis, fast ->
                            joyAxisX = axis
                            joyFast = fast
                        },
                        modifier = Modifier.align(Alignment.BottomEnd),
                    )
                }
            }
        }

        // 7. Custom Cartoon Dialogue Box
        CartoonDialogueOverlay(
            county = county,
            line = activeDialogue?.currentLine,
            isSpeaking = isSpeaking,
            isPreparingVoice = isPreparingVoice,
            mouthOpenLevel = mouthOpenLevel,
            voiceSourceLabel = voiceSourceLabel,
            onAdvance = { viewModel.advanceDialogue() },
            onSkip = { viewModel.skipDialogue() },
            onToggleVoicePlayback = {
                if (isSpeaking) viewModel.stopDialogueVoiceOnly() else viewModel.replayDialogueVoice()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing),
        )

        // 8. Interactive Mission Puzzle Overlay
        if (activeMissionPuzzle != null) {
            PuzzleModalOverlay(
                mission = activeMissionPuzzle,
                onPlaySfx = { viewModel.soundEngine.playSfx(it) },
                onSpeakCharacterLine = { lineId, text ->
                    viewModel.voiceManager.speakDialogue(
                        com.example.model.DialogueLine(
                            id = lineId,
                            speakerName = county.npcName,
                            isAria = false,
                            voiceProfile = county.npcVoice,
                            ariaEmotion = AriaAnimationState.DISCOVER_CLUE,
                            text = text,
                        ),
                    )
                },
                onStopSpeech = { viewModel.voiceManager.stop() },
                onSolved = {
                    temporaryEmotion = AriaAnimationState.HAPPY
                    viewModel.completeMission(county, activeMissionPuzzle)
                },
                onDismiss = { viewModel.dismissMissionPuzzle() },
            )
        }

        // 9. Treasure Piece Discovery Ceremony
        AnimatedVisibility(
            visible = rewardCelebrationCounty != null,
            enter = fadeIn() + scaleIn(initialScale = 0.88f),
        ) {
            if (rewardCelebrationCounty != null) {
                TreasureRewardOverlay(
                    county = rewardCelebrationCounty,
                    onSpeakDiscovery = {
                        viewModel.voiceManager.speakDialogue(
                            HormozganCatalog.treasureRewardLine(rewardCelebrationCounty),
                        )
                    },
                    onStopSpeech = { viewModel.voiceManager.stop() },
                    onContinue = {
                        viewModel.voiceManager.stop()
                        viewModel.dismissRewardCelebrationAndFarewell(rewardCelebrationCounty)
                    },
                    onViewScientificBrief = {
                        viewModel.voiceManager.stop()
                        viewModel.dismissRewardCelebrationAndFarewell(rewardCelebrationCounty)
                        onOpenJournal()
                    },
                )
            }
        }
    }
}

@Composable
private fun TreasureRewardOverlay(
    county: CountyDefinition,
    onSpeakDiscovery: () -> Unit = {},
    onStopSpeech: () -> Unit = {},
    onContinue: () -> Unit,
    onViewScientificBrief: () -> Unit,
) {
    LaunchedEffect(county.id) {
        onSpeakDiscovery()
    }
    DisposableEffect(county.id) {
        onDispose { onStopSpeech() }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .border(3.dp, SunlitGold, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = ParchmentCream,
            shadowElevation = 18.dp,
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AriaCharacterCanvas(
                    animationState = AriaAnimationState.RECEIVE_REWARD,
                    facingRight = true,
                    modifier = Modifier.size(116.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = WarmAmberGold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "قطعه ${county.treasurePiece.index.toPersianNumber()} از ۱۳ گنج کشف شد!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = DeepGulfTeal,
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = county.treasurePiece.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PersianNastaliqFamily,
                        fontSize = 22.sp,
                        lineHeight = 40.sp,
                    ),
                    color = GulfTurquoise,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = WarmAmberGold.copy(alpha = 0.16f),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "محور دانشی: ${county.treasurePiece.pillarTheme}",
                            style = MaterialTheme.typography.labelLarge,
                            color = DeepGulfTeal,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = county.treasurePiece.encyclopediaDetail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmSepiaInk,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onViewScientificBrief,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text("مطالعه اطلاعات علمی", style = MaterialTheme.typography.labelMedium)
                    }
                    Button(
                        onClick = onContinue,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("reward_continue_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GulfTurquoise),
                    ) {
                        Text("ثبت در دفترچه", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
