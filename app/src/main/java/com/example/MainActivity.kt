package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.SoundEffect
import com.example.data.catalog.HormozganCatalog
import com.example.firebase.configureAppCheck
import com.example.ui.screens.AboutUsScreen
import com.example.ui.screens.EncyclopediaScreen
import com.example.ui.screens.HormozganMapScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.SecretJournalScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashIntroScreen
import com.example.ui.stage.AdventureStageScreen
import com.example.ui.theme.HormozganTheme
import com.example.viewmodel.GameRoute
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check before Compose UI or ViewModel creation
        configureAppCheck(this, intent)

        // STEP 2: Full edge-to-edge display
        enableEdgeToEdge()

        setContent {
            HormozganTheme {
                HormozganGameApp()
            }
        }
    }
}

@Composable
fun HormozganGameApp(
    gameViewModel: GameViewModel = viewModel(),
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> gameViewModel.onAppResume()
                Lifecycle.Event.ON_PAUSE -> gameViewModel.onAppPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val route by gameViewModel.currentRoute.collectAsStateWithLifecycle()
    val progressMap by gameViewModel.countyProgress.collectAsStateWithLifecycle()
    val playerSave by gameViewModel.playerSave.collectAsStateWithLifecycle()
    val audioSettings by gameViewModel.audioSettings.collectAsStateWithLifecycle()
    val collectedPieces by gameViewModel.collectedPiecesCount.collectAsStateWithLifecycle()
    val activeDialogue by gameViewModel.activeDialogue.collectAsStateWithLifecycle()
    val activeMissionPuzzle by gameViewModel.activeMissionPuzzle.collectAsStateWithLifecycle()
    val rewardCelebrationCounty by gameViewModel.rewardCelebrationCounty.collectAsStateWithLifecycle()
    val isSpeaking by gameViewModel.voiceManager.isSpeaking.collectAsStateWithLifecycle()
    val isPreparingVoice by gameViewModel.voiceManager.isPreparingVoice.collectAsStateWithLifecycle()
    val mouthOpenLevel by gameViewModel.voiceManager.mouthOpenLevel.collectAsStateWithLifecycle()
    val voiceSourceLabel by gameViewModel.voiceManager.voiceSourceLabel.collectAsStateWithLifecycle()
    val voicePrecacheState by gameViewModel.voicePrecacheState.collectAsStateWithLifecycle()
    val stageVoiceStatus by gameViewModel.stageVoiceStatus.collectAsStateWithLifecycle()

    // Handle system Back button consistently on all secondary screens and overlays
    BackHandler(
        enabled = route !is GameRoute.SplashIntro && route !is GameRoute.MainMenu ||
            activeDialogue != null || activeMissionPuzzle != null || rewardCelebrationCounty != null,
    ) {
        gameViewModel.navigateBack()
    }

    when (val current = route) {
        is GameRoute.SplashIntro -> {
            SplashIntroScreen(
                hasSeenIntro = playerSave.hasSeenIntro,
                viewModel = gameViewModel,
                onContinueToMenu = { gameViewModel.finishIntroAndGoMenu() },
                onStartBashagardDirect = {
                    gameViewModel.finishIntroAndGoMenu()
                    gameViewModel.startNewAdventure()
                },
            )
        }

        is GameRoute.MainMenu -> {
            val activeCounty = HormozganCatalog.getById(playerSave.activeCountyId)
            MainMenuScreen(
                collectedPieces = collectedPieces,
                activeCountyName = activeCounty.nameFa,
                onStartNewGame = { gameViewModel.startNewAdventure() },
                onContinueGame = { gameViewModel.continueAdventure() },
                onOpenMap = { gameViewModel.navigateTo(GameRoute.HormozganMap) },
                onOpenJournal = {
                    gameViewModel.soundEngine.playSfx(SoundEffect.OPEN_JOURNAL)
                    gameViewModel.navigateTo(GameRoute.SecretJournal(0))
                },
                onOpenSettings = { gameViewModel.navigateTo(GameRoute.Settings) },
                onOpenAboutUs = { gameViewModel.navigateTo(GameRoute.AboutUs) },
            )
        }

        is GameRoute.HormozganMap -> {
            HormozganMapScreen(
                progressMap = progressMap,
                activeCountyId = playerSave.activeCountyId,
                encyclopediaUnlocked = playerSave.encyclopediaUnlocked,
                onSelectCountyToPlay = { county ->
                    gameViewModel.navigateTo(GameRoute.AdventureStage(county.id))
                },
                onOpenJournal = {
                    gameViewModel.soundEngine.playSfx(SoundEffect.OPEN_JOURNAL)
                    gameViewModel.navigateTo(GameRoute.SecretJournal(0))
                },
                onOpenEncyclopedia = { gameViewModel.navigateTo(GameRoute.Encyclopedia) },
                onBack = { gameViewModel.navigateBack() },
            )
        }

        is GameRoute.AdventureStage -> {
            val county = HormozganCatalog.getById(current.countyId)
            val countyState = progressMap[county.id] ?: com.example.model.CountyProgressState(
                countyId = county.id,
                isUnlocked = true,
                isCompleted = false,
                completedMissionIds = emptySet(),
                treasureCollected = false,
                npcMet = false,
            )
            val startX = if (playerSave.activeCountyId == county.id) playerSave.lastPlayerX else 320f
            AdventureStageScreen(
                county = county,
                progress = countyState,
                initialPlayerX = startX,
                activeDialogue = activeDialogue,
                isSpeaking = isSpeaking,
                isPreparingVoice = isPreparingVoice,
                mouthOpenLevel = mouthOpenLevel,
                voiceSourceLabel = voiceSourceLabel,
                stageVoiceStatus = stageVoiceStatus,
                viewModel = gameViewModel,
                activeMissionPuzzle = activeMissionPuzzle,
                rewardCelebrationCounty = rewardCelebrationCounty,
                onOpenJournal = {
                    gameViewModel.soundEngine.playSfx(SoundEffect.OPEN_JOURNAL)
                    gameViewModel.navigateTo(GameRoute.SecretJournal(2))
                },
                onOpenMap = { gameViewModel.navigateTo(GameRoute.HormozganMap) },
                onOpenSettings = { gameViewModel.navigateTo(GameRoute.Settings) },
            )
        }

        is GameRoute.SecretJournal -> {
            SecretJournalScreen(
                initialTab = current.initialTab,
                progressMap = progressMap,
                encyclopediaUnlocked = playerSave.encyclopediaUnlocked,
                onOpenEncyclopedia = { gameViewModel.navigateTo(GameRoute.Encyclopedia) },
                onPlayCounty = { countyId ->
                    gameViewModel.navigateTo(GameRoute.AdventureStage(countyId))
                },
                onBack = { gameViewModel.navigateBack() },
            )
        }

        is GameRoute.Encyclopedia -> {
            EncyclopediaScreen(
                onSpeakFinale = {
                    gameViewModel.voiceManager.speakDialogue(HormozganCatalog.encyclopediaFinaleLine)
                },
                onStopSpeech = { gameViewModel.voiceManager.stop() },
                onBack = { gameViewModel.navigateBack() },
            )
        }

        is GameRoute.Settings -> {
            SettingsScreen(
                settings = audioSettings,
                voicePrecacheState = voicePrecacheState,
                onUpdateSettings = { gameViewModel.updateAudioSettings(it) },
                onPrecacheAllVoices = { gameViewModel.precacheAllVoices() },
                onCancelPrecache = { gameViewModel.cancelVoicePrecaching() },
                onResetProgress = { gameViewModel.resetProgress() },
                onBack = { gameViewModel.navigateBack() },
            )
        }

        is GameRoute.AboutUs -> {
            AboutUsScreen(
                onBack = { gameViewModel.navigateBack() },
            )
        }
    }
}
