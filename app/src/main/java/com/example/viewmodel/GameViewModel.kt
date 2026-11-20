package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEffect
import com.example.audio.SoundEngine
import com.example.audio.StageVoiceStatus
import com.example.audio.VoiceManager
import com.example.audio.VoicePrecacheState
import com.example.data.catalog.HormozganCatalog
import com.example.data.local.AudioSettingsStore
import com.example.data.local.HormozganDatabase
import com.example.data.local.PlayerSaveEntity
import com.example.data.repository.GameRepository
import com.example.model.AudioSettings
import com.example.model.BiomeType
import com.example.model.CountyDefinition
import com.example.model.CountyMission
import com.example.model.CountyProgressState
import com.example.model.DialogueLine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface GameRoute {
    data object SplashIntro : GameRoute
    data object MainMenu : GameRoute
    data object HormozganMap : GameRoute
    data class AdventureStage(val countyId: String) : GameRoute
    data class SecretJournal(val initialTab: Int = 0) : GameRoute
    data object Encyclopedia : GameRoute
    data object Settings : GameRoute
    data object AboutUs : GameRoute
}

data class ActiveDialogueState(
    val county: CountyDefinition,
    val lines: List<DialogueLine>,
    val currentIndex: Int = 0,
    val onFinishedAction: (() -> Unit)? = null,
) {
    val currentLine: DialogueLine? get() = lines.getOrNull(currentIndex)
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val database = HormozganDatabase.getInstance(application)
    private val audioStore = AudioSettingsStore(application)
    private val repository = GameRepository(database.gameDao(), audioStore)

    val soundEngine = SoundEngine()
    val voiceManager = VoiceManager(application, soundEngine)
    val voicePrecacheState: StateFlow<VoicePrecacheState> = voiceManager.precacheState
    val stageVoiceStatus: StateFlow<StageVoiceStatus?> = voiceManager.stageVoiceStatus

    val audioSettings: StateFlow<AudioSettings> = repository.audioSettings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AudioSettings())

    val countyProgress: StateFlow<Map<String, CountyProgressState>> = repository.countyProgressMap
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val playerSave: StateFlow<PlayerSaveEntity> = repository.playerSave
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            PlayerSaveEntity(1, "bashagard", 320f, hasSeenIntro = false, encyclopediaUnlocked = false),
        )

    private val _routeStack = MutableStateFlow<List<GameRoute>>(listOf(GameRoute.SplashIntro))
    val currentRoute: StateFlow<GameRoute> = MutableStateFlow<GameRoute>(GameRoute.SplashIntro).also { state ->
        viewModelScope.launch {
            _routeStack.collect { stack ->
                state.value = stack.lastOrNull() ?: GameRoute.MainMenu
            }
        }
    }

    private val _activeDialogue = MutableStateFlow<ActiveDialogueState?>(null)
    val activeDialogue: StateFlow<ActiveDialogueState?> = _activeDialogue.asStateFlow()

    private val _activeMissionPuzzle = MutableStateFlow<CountyMission?>(null)
    val activeMissionPuzzle: StateFlow<CountyMission?> = _activeMissionPuzzle.asStateFlow()

    private val _rewardCelebrationCounty = MutableStateFlow<CountyDefinition?>(null)
    val rewardCelebrationCounty: StateFlow<CountyDefinition?> = _rewardCelebrationCounty.asStateFlow()

    val collectedPiecesCount: StateFlow<Int> = countyProgress
        .combine(playerSave) { map, _ -> map.values.count { it.treasureCollected } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        val allLines = HormozganCatalog.allDialogueLines()
        voiceManager.refreshCachedCount(allLines)
        viewModelScope.launch {
            repository.ensureSeeded()
        }
        var initialPrecacheStarted = false
        viewModelScope.launch {
            audioSettings.collect { settings ->
                soundEngine.updateSettings(settings)
                voiceManager.updateSettings(settings)
                if (settings.preferGeminiTts && settings.voiceEnabled && !initialPrecacheStarted) {
                    initialPrecacheStarted = true
                    voiceManager.precacheDialogueLines(
                        lines = allLines,
                        surfaceErrorsToUi = false,
                        overrideExistingJob = false,
                    )
                }
            }
        }
    }

    fun onAppResume() {
        soundEngine.onResume()
    }

    fun onAppPause() {
        soundEngine.onPause()
        voiceManager.stop()
    }

    fun navigateTo(route: GameRoute, replaceTop: Boolean = false) {
        soundEngine.playSfx(SoundEffect.MENU_BUTTON)
        voiceManager.stop()
        _activeDialogue.value = null
        _activeMissionPuzzle.value = null
        if (route is GameRoute.AdventureStage) {
            val county = HormozganCatalog.getById(route.countyId)
            soundEngine.setBiome(county.biome)
            viewModelScope.launch {
                repository.savePlayerLocation(county.id, 320f)
            }
        } else if (route is GameRoute.MainMenu || route is GameRoute.HormozganMap) {
            soundEngine.setBiome(BiomeType.OCEAN_CORAL)
        }
        val stack = _routeStack.value.toMutableList()
        if (replaceTop && stack.isNotEmpty()) {
            stack[stack.lastIndex] = route
        } else {
            stack.add(route)
        }
        _routeStack.value = stack
    }

    fun navigateBack(): Boolean {
        voiceManager.stop()
        if (_activeMissionPuzzle.value != null) {
            _activeMissionPuzzle.value = null
            return true
        }
        if (_activeDialogue.value != null) {
            _activeDialogue.value = null
            return true
        }
        if (_rewardCelebrationCounty.value != null) {
            _rewardCelebrationCounty.value = null
            return true
        }
        val stack = _routeStack.value
        if (stack.size > 1) {
            val nextStack = stack.dropLast(1)
            _routeStack.value = nextStack
            val top = nextStack.lastOrNull()
            if (top is GameRoute.MainMenu || top is GameRoute.HormozganMap) {
                soundEngine.setBiome(BiomeType.OCEAN_CORAL)
            }
            return true
        }
        return false
    }

    fun finishIntroAndGoMenu() {
        viewModelScope.launch {
            repository.markIntroSeen()
        }
        _routeStack.value = listOf(GameRoute.MainMenu)
        soundEngine.setBiome(BiomeType.OCEAN_CORAL)
    }

    fun startNewAdventure() {
        viewModelScope.launch {
            repository.markIntroSeen()
        }
        navigateTo(GameRoute.AdventureStage("bashagard"))
    }

    fun continueAdventure() {
        val save = playerSave.value
        val targetId = save.activeCountyId.ifBlank { "bashagard" }
        navigateTo(GameRoute.AdventureStage(targetId))
    }

    fun startDialogue(
        county: CountyDefinition,
        lines: List<DialogueLine>,
        onFinished: (() -> Unit)? = null,
    ) {
        if (lines.isEmpty()) {
            onFinished?.invoke()
            return
        }
        val state = ActiveDialogueState(
            county = county,
            lines = lines,
            currentIndex = 0,
            onFinishedAction = onFinished,
        )
        _activeDialogue.value = state
        lines.firstOrNull()?.let { voiceManager.speakDialogue(it) }
    }

    fun advanceDialogue() {
        val current = _activeDialogue.value ?: return
        soundEngine.playSfx(SoundEffect.MENU_BUTTON)
        val nextIndex = current.currentIndex + 1
        if (nextIndex < current.lines.size) {
            val nextState = current.copy(currentIndex = nextIndex)
            _activeDialogue.value = nextState
            nextState.currentLine?.let { voiceManager.speakDialogue(it) }
        } else {
            voiceManager.stop()
            _activeDialogue.value = null
            current.onFinishedAction?.invoke()
        }
    }

    fun skipDialogue() {
        val current = _activeDialogue.value ?: return
        soundEngine.playSfx(SoundEffect.MENU_BUTTON)
        voiceManager.stop()
        _activeDialogue.value = null
        current.onFinishedAction?.invoke()
    }

    fun stopDialogueVoiceOnly() {
        voiceManager.stop()
    }

    fun replayDialogueVoice() {
        _activeDialogue.value?.currentLine?.let { voiceManager.speakDialogue(it) }
    }

    fun precacheCountyVoices(
        county: CountyDefinition,
        surfaceErrorsToUi: Boolean = false,
    ) {
        voiceManager.precacheStageThenRemaining(
            stageCountyId = county.id,
            stageLines = HormozganCatalog.countyDialogueLines(county),
            allLines = HormozganCatalog.allDialogueLines(),
            surfaceErrorsToUi = surfaceErrorsToUi,
        )
    }

    fun precacheAllVoices() {
        voiceManager.precacheDialogueLines(
            lines = HormozganCatalog.allDialogueLines(),
            surfaceErrorsToUi = true,
        )
    }

    fun cancelVoicePrecaching() {
        voiceManager.cancelPrecaching()
    }

    fun markNpcMet(countyId: String) {
        viewModelScope.launch {
            repository.markNpcMet(countyId)
        }
    }

    fun savePlayerX(countyId: String, x: Float) {
        viewModelScope.launch {
            repository.savePlayerLocation(countyId, x)
        }
    }

    fun openMissionPuzzle(mission: CountyMission) {
        soundEngine.playSfx(SoundEffect.OBJECT_INTERACT)
        _activeMissionPuzzle.value = mission
    }

    fun dismissMissionPuzzle() {
        _activeMissionPuzzle.value = null
    }

    fun completeMission(county: CountyDefinition, mission: CountyMission) {
        _activeMissionPuzzle.value = null
        soundEngine.playSfx(SoundEffect.PUZZLE_SOLVED)
        viewModelScope.launch {
            val unlockedTreasureNow = repository.completeMission(county.id, mission.id)
            if (unlockedTreasureNow) {
                soundEngine.playSfx(SoundEffect.REWARD_FANFARE)
                soundEngine.playSfx(SoundEffect.STAGE_UNLOCK)
                _rewardCelebrationCounty.value = county
            }
        }
    }

    fun dismissRewardCelebrationAndFarewell(county: CountyDefinition) {
        _rewardCelebrationCounty.value = null
        startDialogue(county, county.farewellDialogues)
    }

    fun updateAudioSettings(transform: (AudioSettings) -> AudioSettings) {
        viewModelScope.launch {
            repository.updateAudioSettings(transform)
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
            soundEngine.playSfx(SoundEffect.OPEN_JOURNAL)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.release()
        soundEngine.release()
    }
}
