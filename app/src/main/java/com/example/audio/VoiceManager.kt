package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.model.AudioSettings
import com.example.model.DialogueLine
import com.example.model.VoiceProfile
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.InlineDataPart
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.SpeechConfig
import com.google.firebase.ai.type.Voice
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.sin

private const val TAG = "VoiceManager"
private const val GEMINI_TTS_MODEL = "gemini-3.1-flash-tts-preview"
private const val GEMINI_TTS_TIMEOUT_MS = 35_000L

data class CharacterVoiceSpec(
    val speakerKey: String,
    val geminiVoiceName: String,
    val playbackPitch: Float,
    val playbackSpeed: Float,
    val ttsPitch: Float,
    val ttsRate: Float,
    val audioProfileEn: String,
    val pacingNoteEn: String,
    val personaDescription: String,
) {
    val signatureAssetPath: String get() = "voices/characters/$speakerKey.ogg"
}

data class VoicePrecacheState(
    val cachedCount: Int = 0,
    val totalCount: Int = 0,
    val isGenerating: Boolean = false,
    val currentSpeakerName: String = "",
    val lastErrorMessage: String? = null,
)

data class StageVoiceStatus(
    val countyId: String = "",
    val cachedCount: Int = 0,
    val totalCount: Int = 0,
    val isGenerating: Boolean = false,
    val currentSpeakerName: String = "",
    val errorMessage: String? = null,
)

class VoiceManager(
    private val context: Context,
    private val soundEngine: SoundEngine,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // Lazily instantiated per-character Google AI Studio TTS models
    private val geminiTtsModels = ConcurrentHashMap<String, GenerativeModel>()

    // Single-flight deduplication so live speech and background pre-caching never race on the same line
    private val inFlightGenerations = ConcurrentHashMap<String, Deferred<Result<File>>>()

    // Versioned permanent offline storage directory inside filesDir
    private val offlineVoicesDir: File by lazy {
        File(context.filesDir, "offline_voices_v3").apply { mkdirs() }
    }

    private val bundledVoiceFiles: Set<String> by lazy {
        runCatching {
            context.assets.list("voices")?.toSet() ?: emptySet()
        }.getOrDefault(emptySet())
    }

    private val bundledCharacterSignatureFiles: Set<String> by lazy {
        runCatching {
            context.assets.list("voices/characters")?.toSet() ?: emptySet()
        }.getOrDefault(emptySet())
    }

    private var tts: TextToSpeech? = null
    private var ttsReady: Boolean = false
    private var persianTtsAvailable: Boolean = false
    private var mediaPlayer: MediaPlayer? = null
    private var activeSpeakJob: Job? = null
    private var mouthJob: Job? = null
    private var precacheJob: Job? = null
    private var activeDialogueId: String? = null
    private var trackedDialogueIds: List<String> = emptyList()

    @Volatile
    private var currentSettings: AudioSettings = AudioSettings()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isPreparingVoice = MutableStateFlow(false)
    val isPreparingVoice: StateFlow<Boolean> = _isPreparingVoice.asStateFlow()

    private val _mouthOpenLevel = MutableStateFlow(0f)
    val mouthOpenLevel: StateFlow<Float> = _mouthOpenLevel.asStateFlow()

    private val _voiceSourceLabel = MutableStateFlow("صدای راوی")
    val voiceSourceLabel: StateFlow<String> = _voiceSourceLabel.asStateFlow()

    private val _precacheState = MutableStateFlow(VoicePrecacheState())
    val precacheState: StateFlow<VoicePrecacheState> = _precacheState.asStateFlow()

    private val _stageVoiceStatus = MutableStateFlow<StageVoiceStatus?>(null)
    val stageVoiceStatus: StateFlow<StageVoiceStatus?> = _stageVoiceStatus.asStateFlow()

    init {
        initAndroidTts()
    }

    @OptIn(PublicPreviewAPI::class)
    private fun getOrCreateTtsModel(
        voiceName: String,
        includeLanguageCode: Boolean = true,
    ): GenerativeModel {
        val cacheKey = if (includeLanguageCode) "$voiceName|fa-IR" else "$voiceName|auto"
        return geminiTtsModels.getOrPut(cacheKey) {
            Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                modelName = GEMINI_TTS_MODEL,
                generationConfig = generationConfig {
                    responseModalities = listOf(ResponseModality.AUDIO)
                    speechConfig = if (includeLanguageCode) {
                        SpeechConfig(
                            voice = Voice(voiceName),
                            languageCode = "fa-IR",
                        )
                    } else {
                        SpeechConfig(
                            voice = Voice(voiceName),
                        )
                    }
                },
            )
        }
    }

    private fun initAndroidTts() {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val locales = listOf(Locale("fa", "IR"), Locale("fa"))
                    var available = false
                    for (loc in locales) {
                        val res = tts?.setLanguage(loc)
                        if (res != null &&
                            res != TextToSpeech.LANG_MISSING_DATA &&
                            res != TextToSpeech.LANG_NOT_SUPPORTED
                        ) {
                            available = true
                            break
                        }
                    }
                    persianTtsAvailable = available
                    ttsReady = true
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                            startMouthSyncLoop(null)
                        }

                        override fun onDone(utteranceId: String?) {
                            stopMouthSync()
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            stopMouthSync()
                        }
                    })
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Android TextToSpeech initialization failed; offline character signature fallback active.", e)
        }
    }

    fun updateSettings(settings: AudioSettings) {
        currentSettings = settings
        if (!settings.voiceEnabled || settings.voiceVolume <= 0.01f) {
            stop()
        }
    }

    fun refreshCachedCount(allLines: List<DialogueLine>) {
        trackedDialogueIds = allLines.map { it.id }
        scope.launch(Dispatchers.IO) {
            val count = countAvailableOfflineLines(trackedDialogueIds)
            _precacheState.value = _precacheState.value.copy(
                cachedCount = count,
                totalCount = trackedDialogueIds.size.coerceAtLeast(1),
            )
        }
    }

    private fun countAvailableOfflineLines(ids: List<String>): Int {
        return ids.count { id ->
            findBundledVoiceAsset(id) != null || findSavedOfflineFile(id) != null
        }
    }

    /**
     * Plays spoken Persian voice for any dialogue/story line:
     * 1) Pre-bundled studio recording in assets/voices/<id>.(ogg|mp3|wav)
     * 2) Permanently saved voice file in filesDir/offline_voices_v3/
     * 3) Google AI Studio (`gemini-3.1-flash-tts-preview`) with unique character Audio Profile,
     *    Persian Director's Notes (`بَشاگَرد` -> Ba-shā-gard), `fa-IR` SpeechConfig & permanent caching
     * 4) On-device Persian TextToSpeech with character pitch/rate tuning
     * 5) Character acoustic signature + real-time lip-sync
     */
    fun speakDialogue(line: DialogueLine) {
        if (activeDialogueId == line.id && _isSpeaking.value) {
            return
        }
        stop()
        activeDialogueId = line.id

        val cfg = currentSettings
        if (!cfg.voiceEnabled || cfg.voiceVolume <= 0.01f) {
            return
        }

        val charSpec = resolveCharacterVoice(line)
        val cleanSpeakerBadge = "صدای ${line.speakerName}"
        _voiceSourceLabel.value = cleanSpeakerBadge

        activeSpeakJob = scope.launch {
            // 1. Pre-bundled full-line voice recording from assets/voices/
            val bundledAsset = findBundledVoiceAsset(line.id)
            if (bundledAsset != null) {
                _voiceSourceLabel.value = cleanSpeakerBadge
                val played = playAssetAudio(
                    assetPath = bundledAsset,
                    volume = cfg.voiceVolume,
                    charSpec = charSpec,
                    keepMouthActiveUntilComplete = true,
                )
                if (played) return@launch
            }

            // 2. Permanently saved voice file in filesDir/offline_voices_v3/
            val savedFile = withContext(Dispatchers.IO) { findSavedOfflineFile(line.id) }
            if (savedFile != null && savedFile.exists() && savedFile.length() > 128L) {
                _voiceSourceLabel.value = cleanSpeakerBadge
                val played = playAudioFile(savedFile, cfg.voiceVolume, charSpec)
                if (played) return@launch
            }

            // 3. Google AI Studio unique character voice synthesis (saved permanently on device)
            if (cfg.preferGeminiTts) {
                _isPreparingVoice.value = true
                val result = getOrGenerateGeminiVoice(line, charSpec)
                _isPreparingVoice.value = false
                val generatedFile = result.getOrNull()
                if (generatedFile != null && isActive) {
                    _voiceSourceLabel.value = cleanSpeakerBadge
                    val played = playAudioFile(generatedFile, cfg.voiceVolume, charSpec)
                    if (played) return@launch
                }
            }

            if (!isActive) return@launch
            _isPreparingVoice.value = false

            // 4. Device Persian TextToSpeech with per-character unique pitch & cadence
            if (ttsReady && persianTtsAvailable) {
                _voiceSourceLabel.value = cleanSpeakerBadge
                speakWithAndroidTts(line, charSpec, cfg.voiceVolume)
            } else {
                // 5. Unique character acoustic signature + live lip-sync
                _voiceSourceLabel.value = cleanSpeakerBadge
                runCharacterSignatureFallback(line, charSpec, cfg.voiceVolume)
            }
        }
    }

    /**
     * Generates and loads all voices for the currently entered stage with priority concurrency,
     * updating both stage-level progress and global cache state, then continues loading the rest.
     */
    fun precacheStageThenRemaining(
        stageCountyId: String,
        stageLines: List<DialogueLine>,
        allLines: List<DialogueLine>,
        surfaceErrorsToUi: Boolean,
    ) {
        if (!currentSettings.preferGeminiTts || stageLines.isEmpty()) return

        precacheJob?.cancel()
        val stageIds = stageLines.map { it.id }
        val stageIdSet = stageIds.toHashSet()
        val remainingLines = allLines.filter { it.id !in stageIdSet }

        precacheJob = scope.launch {
            val initialStageCached = withContext(Dispatchers.IO) { countAvailableOfflineLines(stageIds) }
            val stageNeedsWork = initialStageCached < stageLines.size
            _stageVoiceStatus.value = StageVoiceStatus(
                countyId = stageCountyId,
                cachedCount = initialStageCached,
                totalCount = stageLines.size,
                isGenerating = stageNeedsWork,
                currentSpeakerName = if (stageNeedsWork) stageLines.first().speakerName else "",
                errorMessage = null,
            )

            _precacheState.value = _precacheState.value.copy(
                isGenerating = true,
                lastErrorMessage = null,
            )

            if (stageNeedsWork) {
                val semaphore = Semaphore(3)
                var firstError: String? = null
                coroutineScope {
                    stageLines.map { line ->
                        async {
                            semaphore.withPermit {
                                if (!isActive) return@withPermit
                                val exists = withContext(Dispatchers.IO) {
                                    findBundledVoiceAsset(line.id) != null || findSavedOfflineFile(line.id) != null
                                }
                                if (exists) return@withPermit

                                val charSpec = resolveCharacterVoice(line)
                                _stageVoiceStatus.value = _stageVoiceStatus.value?.copy(
                                    currentSpeakerName = line.speakerName,
                                )
                                _precacheState.value = _precacheState.value.copy(
                                    currentSpeakerName = line.speakerName,
                                )

                                val result = getOrGenerateGeminiVoice(line, charSpec)
                                val stageCachedNow = withContext(Dispatchers.IO) { countAvailableOfflineLines(stageIds) }
                                if (result.isFailure && firstError == null) {
                                    firstError = result.exceptionOrNull()?.localizedMessage
                                        ?: "خطا در بارگذاری صدای ${line.speakerName}"
                                }
                                _stageVoiceStatus.value = _stageVoiceStatus.value?.copy(
                                    cachedCount = stageCachedNow,
                                    errorMessage = if (surfaceErrorsToUi) firstError else _stageVoiceStatus.value?.errorMessage,
                                )
                            }
                        }
                    }.awaitAll()
                }

                val finalStageCached = withContext(Dispatchers.IO) { countAvailableOfflineLines(stageIds) }
                _stageVoiceStatus.value = StageVoiceStatus(
                    countyId = stageCountyId,
                    cachedCount = finalStageCached,
                    totalCount = stageLines.size,
                    isGenerating = false,
                    currentSpeakerName = "",
                    errorMessage = if (finalStageCached < stageLines.size) firstError else null,
                )
            }

            // Seamlessly continue loading remaining counties in the background
            runConcurrentPrecacheBatch(
                lines = remainingLines,
                concurrency = 2,
                surfaceErrorsToUi = false,
            )

            val globalFinal = withContext(Dispatchers.IO) {
                val ids = if (trackedDialogueIds.isNotEmpty()) trackedDialogueIds else allLines.map { it.id }
                countAvailableOfflineLines(ids)
            }
            _precacheState.value = _precacheState.value.copy(
                cachedCount = globalFinal,
                isGenerating = false,
                currentSpeakerName = "",
            )
        }
    }

    /**
     * Generates and permanently stores dialogue audio via Google AI Studio in the background.
     * Existing cached files are skipped automatically.
     */
    fun precacheDialogueLines(
        lines: List<DialogueLine>,
        surfaceErrorsToUi: Boolean,
        overrideExistingJob: Boolean = true,
    ) {
        if (!currentSettings.preferGeminiTts) return
        if (lines.isEmpty()) return

        if (precacheJob?.isActive == true && !overrideExistingJob) {
            return
        }
        precacheJob?.cancel()

        precacheJob = scope.launch {
            _precacheState.value = _precacheState.value.copy(
                isGenerating = true,
                lastErrorMessage = null,
            )

            runConcurrentPrecacheBatch(
                lines = lines,
                concurrency = 3,
                surfaceErrorsToUi = surfaceErrorsToUi,
            )

            val finalCount = withContext(Dispatchers.IO) {
                val ids = if (trackedDialogueIds.isNotEmpty()) trackedDialogueIds else lines.map { it.id }
                countAvailableOfflineLines(ids)
            }
            _precacheState.value = _precacheState.value.copy(
                cachedCount = finalCount,
                isGenerating = false,
                currentSpeakerName = "",
            )
        }
    }

    private suspend fun runConcurrentPrecacheBatch(
        lines: List<DialogueLine>,
        concurrency: Int,
        surfaceErrorsToUi: Boolean,
    ) {
        if (lines.isEmpty()) return
        val semaphore = Semaphore(concurrency)
        var consecutiveFailures = 0
        var abortBatch = false

        coroutineScope {
            lines.map { line ->
                async {
                    semaphore.withPermit {
                        if (!isActive || abortBatch) return@withPermit
                        val alreadyExists = withContext(Dispatchers.IO) {
                            findBundledVoiceAsset(line.id) != null || findSavedOfflineFile(line.id) != null
                        }
                        if (alreadyExists) return@withPermit

                        val charSpec = resolveCharacterVoice(line)
                        _precacheState.value = _precacheState.value.copy(
                            currentSpeakerName = line.speakerName,
                        )

                        val result = getOrGenerateGeminiVoice(line, charSpec)
                        if (result.isSuccess) {
                            consecutiveFailures = 0
                        } else {
                            consecutiveFailures++
                            val errMsg = result.exceptionOrNull()?.localizedMessage
                                ?: "خطا در دریافت صدای ${line.speakerName}"
                            if (surfaceErrorsToUi) {
                                _precacheState.value = _precacheState.value.copy(
                                    lastErrorMessage = errMsg,
                                )
                            }
                            val maxFailures = if (surfaceErrorsToUi) 6 else 4
                            if (consecutiveFailures >= maxFailures) {
                                abortBatch = true
                                if (surfaceErrorsToUi) {
                                    _precacheState.value = _precacheState.value.copy(
                                        lastErrorMessage = "اتصال اینترنت یا دسترسی سرویس صدا موقتاً برقرار نیست: $errMsg",
                                    )
                                }
                            }
                        }
                    }
                }
            }.awaitAll()
        }
    }

    fun cancelPrecaching() {
        precacheJob?.cancel()
        precacheJob = null
        _precacheState.value = _precacheState.value.copy(
            isGenerating = false,
            currentSpeakerName = "",
        )
        _stageVoiceStatus.value = _stageVoiceStatus.value?.copy(
            isGenerating = false,
            currentSpeakerName = "",
        )
    }

    private suspend fun getOrGenerateGeminiVoice(
        line: DialogueLine,
        charSpec: CharacterVoiceSpec,
    ): Result<File> {
        val existingDeferred = inFlightGenerations[line.id]
        if (existingDeferred != null && existingDeferred.isActive) {
            return existingDeferred.await()
        }

        val deferred = CompletableDeferred<Result<File>>()
        val previous = inFlightGenerations.putIfAbsent(line.id, deferred)
        if (previous != null && previous.isActive) {
            return previous.await()
        }

        return try {
            val result = generateAndPersistGeminiVoice(line, charSpec)
            deferred.complete(result)
            result
        } catch (e: CancellationException) {
            deferred.cancel(e)
            throw e
        } catch (e: Exception) {
            val failure = Result.failure<File>(e)
            deferred.complete(failure)
            failure
        } finally {
            inFlightGenerations.remove(line.id, deferred)
        }
    }

    private fun findBundledVoiceAsset(dialogueId: String): String? {
        val candidates = listOf("$dialogueId.ogg", "$dialogueId.mp3", "$dialogueId.wav")
        val match = candidates.firstOrNull { it in bundledVoiceFiles } ?: return null
        return "voices/$match"
    }

    private fun findSavedOfflineFile(dialogueId: String): File? {
        val wav = File(offlineVoicesDir, "$dialogueId.wav")
        if (wav.exists() && wav.length() > 128L) return wav
        val mp3 = File(offlineVoicesDir, "$dialogueId.mp3")
        if (mp3.exists() && mp3.length() > 128L) return mp3
        val ogg = File(offlineVoicesDir, "$dialogueId.ogg")
        if (ogg.exists() && ogg.length() > 128L) return ogg
        return null
    }

    private fun applyCharacterPitchAndSpeed(
        player: MediaPlayer,
        charSpec: CharacterVoiceSpec,
    ) {
        if (abs(charSpec.playbackPitch - 1f) < 0.01f && abs(charSpec.playbackSpeed - 1f) < 0.01f) {
            return
        }
        runCatching {
            player.playbackParams = PlaybackParams().apply {
                pitch = charSpec.playbackPitch.coerceIn(0.75f, 1.45f)
                speed = charSpec.playbackSpeed.coerceIn(0.85f, 1.25f)
            }
        }.onFailure { err ->
            Log.w(TAG, "PlaybackParams pitch tuning skipped for ${charSpec.speakerKey}", err)
        }
    }

    private fun playAssetAudio(
        assetPath: String,
        volume: Float,
        charSpec: CharacterVoiceSpec,
        keepMouthActiveUntilComplete: Boolean,
    ): Boolean {
        return try {
            val player = MediaPlayer()
            mediaPlayer = player
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            context.assets.openFd(assetPath).use { afd ->
                player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            }
            player.setVolume(volume, volume)
            player.prepare()
            if (keepMouthActiveUntilComplete) {
                _isSpeaking.value = true
                startMouthSyncLoop(null, charSpec.ttsRate)
            }
            player.setOnCompletionListener {
                if (keepMouthActiveUntilComplete) {
                    stopMouthSync()
                }
                runCatching { it.release() }
                if (mediaPlayer === it) mediaPlayer = null
            }
            player.setOnErrorListener { mp, _, _ ->
                if (keepMouthActiveUntilComplete) {
                    stopMouthSync()
                }
                runCatching { mp.release() }
                if (mediaPlayer === mp) mediaPlayer = null
                true
            }
            player.start()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Bundled voice asset playback failed for $assetPath", e)
            runCatching { mediaPlayer?.release() }
            mediaPlayer = null
            if (keepMouthActiveUntilComplete) {
                stopMouthSync()
            }
            false
        }
    }

    private suspend fun generateAndPersistGeminiVoice(
        line: DialogueLine,
        charSpec: CharacterVoiceSpec,
    ): Result<File> {
        val structuredPrompt = buildGeminiTtsPrompt(line, charSpec)
        val attempts = listOf(true, false, true)
        var lastError: Exception? = null

        for ((idx, includeLang) in attempts.withIndex()) {
            try {
                if (idx > 0) delay(550L)
                val model = getOrCreateTtsModel(charSpec.geminiVoiceName, includeLanguageCode = includeLang)
                val response = withTimeout(GEMINI_TTS_TIMEOUT_MS) {
                    model.generateContent(structuredPrompt)
                }
                val inlinePart = response.candidates
                    .firstOrNull()
                    ?.content
                    ?.parts
                    ?.filterIsInstance<InlineDataPart>()
                    ?.firstOrNull { it.mimeType.startsWith("audio/") }

                if (inlinePart != null && inlinePart.inlineData.isNotEmpty()) {
                    val bytes = inlinePart.inlineData
                    val mime = inlinePart.mimeType.lowercase(Locale.ROOT)
                    val parsedSampleRate = extractSampleRateFromMime(mime) ?: 24000
                    val savedFile = withContext(Dispatchers.IO) {
                        when {
                            mime.contains("pcm") || mime.contains("l16") -> {
                                File(offlineVoicesDir, "${line.id}.wav").apply {
                                    writeBytes(wrapPcm16AsWav(bytes, sampleRate = parsedSampleRate))
                                }
                            }
                            mime.contains("mpeg") || mime.contains("mp3") -> {
                                File(offlineVoicesDir, "${line.id}.mp3").apply { writeBytes(bytes) }
                            }
                            mime.contains("ogg") -> {
                                File(offlineVoicesDir, "${line.id}.ogg").apply { writeBytes(bytes) }
                            }
                            else -> {
                                File(offlineVoicesDir, "${line.id}.wav").apply { writeBytes(bytes) }
                            }
                        }
                    }
                    if (trackedDialogueIds.isNotEmpty()) {
                        val count = withContext(Dispatchers.IO) { countAvailableOfflineLines(trackedDialogueIds) }
                        _precacheState.value = _precacheState.value.copy(cachedCount = count)
                    }
                    return Result.success(savedFile)
                } else {
                    lastError = IllegalStateException("پاسخ صوتی برای ${line.speakerName} خالی بود.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastError = e
                Log.w(TAG, "Gemini TTS attempt ${idx + 1} failed for ${line.id} (${charSpec.geminiVoiceName})", e)
            }
        }
        Log.e(TAG, "Gemini character TTS failed for ${line.id} (${charSpec.geminiVoiceName})", lastError)
        return Result.failure(lastError ?: IllegalStateException("خطا در تولید صدای ${line.speakerName}"))
    }

    private fun extractSampleRateFromMime(mime: String): Int? {
        val match = Regex("""rate=(\d{4,6})""").find(mime) ?: return null
        return match.groupValues[1].toIntOrNull()?.takeIf { it in 8000..96000 }
    }

    private fun playAudioFile(
        file: File,
        volume: Float,
        charSpec: CharacterVoiceSpec,
    ): Boolean {
        return try {
            val player = MediaPlayer()
            mediaPlayer = player
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            player.setDataSource(file.absolutePath)
            player.setVolume(volume, volume)
            player.prepare()
            applyCharacterPitchAndSpeed(player, charSpec)
            _isSpeaking.value = true
            startMouthSyncLoop(null, charSpec.ttsRate)
            player.setOnCompletionListener {
                stopMouthSync()
                runCatching { it.release() }
                if (mediaPlayer === it) mediaPlayer = null
            }
            player.setOnErrorListener { mp, _, _ ->
                stopMouthSync()
                runCatching { mp.release() }
                if (mediaPlayer === mp) mediaPlayer = null
                true
            }
            player.start()
            true
        } catch (e: Exception) {
            Log.w(TAG, "MediaPlayer failed for cached TTS file; trying fallback.", e)
            runCatching { mediaPlayer?.release() }
            mediaPlayer = null
            stopMouthSync()
            false
        }
    }

    private fun speakWithAndroidTts(
        line: DialogueLine,
        charSpec: CharacterVoiceSpec,
        volume: Float,
    ) {
        try {
            val engine = tts ?: return
            engine.setPitch(charSpec.ttsPitch)
            engine.setSpeechRate(charSpec.ttsRate)
            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume.coerceIn(0f, 1f))
            }
            val speechTranscript = formatSpeechTranscript(line.text)
            engine.speak(speechTranscript, TextToSpeech.QUEUE_FLUSH, params, line.id)
        } catch (e: Exception) {
            Log.w(TAG, "Android TTS speak error; using offline character signature.", e)
            scope.launch { runCharacterSignatureFallback(line, charSpec, volume) }
        }
    }

    private suspend fun runCharacterSignatureFallback(
        line: DialogueLine,
        charSpec: CharacterVoiceSpec,
        volume: Float,
    ) {
        val estimatedDurationMs = (line.text.length * 46L).coerceIn(1400L, 4800L)
        _isSpeaking.value = true
        val sigFileName = "${charSpec.speakerKey}.ogg"
        if (sigFileName in bundledCharacterSignatureFiles) {
            playAssetAudio(
                assetPath = charSpec.signatureAssetPath,
                volume = (volume * 0.72f).coerceIn(0.1f, 1f),
                charSpec = charSpec,
                keepMouthActiveUntilComplete = false,
            )
        } else {
            soundEngine.playSfx(SoundEffect.DIALOGUE_BLIP)
        }
        startMouthSyncLoop(line.voiceProfile, charSpec.ttsRate)
        delay(estimatedDurationMs)
        stopMouthSync()
    }

    private fun startMouthSyncLoop(profile: VoiceProfile?, customRate: Float? = null) {
        mouthJob?.cancel()
        mouthJob = scope.launch {
            var tick = 0f
            val speed = (customRate ?: profile?.rate ?: 1f) * 0.42f
            while (isActive && _isSpeaking.value) {
                tick += speed
                val wave = abs(sin(tick * 2.1f) * 0.65f + sin(tick * 3.7f) * 0.35f)
                _mouthOpenLevel.value = wave.coerceIn(0.08f, 1f)
                delay(55L)
            }
            _mouthOpenLevel.value = 0f
        }
    }

    private fun stopMouthSync() {
        _isSpeaking.value = false
        _mouthOpenLevel.value = 0f
        mouthJob?.cancel()
        mouthJob = null
    }

    fun stop() {
        activeSpeakJob?.cancel()
        activeSpeakJob = null
        activeDialogueId = null
        _isPreparingVoice.value = false
        runCatching { tts?.stop() }
        runCatching {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        }
        mediaPlayer = null
        stopMouthSync()
    }

    fun release() {
        cancelPrecaching()
        stop()
        runCatching { tts?.shutdown() }
        tts = null
    }

    private fun wrapPcm16AsWav(pcmBytes: ByteArray, sampleRate: Int): ByteArray {
        val totalDataLen = pcmBytes.size + 36
        val byteRate = sampleRate * 2
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
        header[20] = 1; header[21] = 0
        header[22] = 1; header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2; header[33] = 0
        header[34] = 16; header[35] = 0
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (pcmBytes.size and 0xff).toByte()
        header[41] = ((pcmBytes.size shr 8) and 0xff).toByte()
        header[42] = ((pcmBytes.size shr 16) and 0xff).toByte()
        header[43] = ((pcmBytes.size shr 24) and 0xff).toByte()
        return header + pcmBytes
    }

    companion object {
        /**
         * All 15 characters use 15 completely distinct official Gemini TTS voices
         * matched to each character's visual appearance, age, gender, and personality.
         */
        val CHARACTER_VOICE_SPECS: Map<String, CharacterVoiceSpec> = mapOf(
            "آریا" to CharacterVoiceSpec(
                speakerKey = "aria",
                geminiVoiceName = "Puck",
                playbackPitch = 1.24f,
                playbackSpeed = 1.03f,
                ttsPitch = 1.30f,
                ttsRate = 1.04f,
                audioProfileEn = "A 15-year-old Iranian boy with a bright, youthful, slightly boyish, curious and cheerful adolescent voice",
                pacingNoteEn = "Energetic, expressive, warm teenage boy cadence",
                personaDescription = "پسر نوجوان ۱۵ ساله کنجکاو و پرانرژی با لحن نوجوانانه و کودکانه",
            ),
            "راوی" to CharacterVoiceSpec(
                speakerKey = "narrator",
                geminiVoiceName = "Rasalgethi",
                playbackPitch = 0.96f,
                playbackSpeed = 0.96f,
                ttsPitch = 0.84f,
                ttsRate = 0.91f,
                audioProfileEn = "A warm, cinematic, wise Iranian male storyteller and grandfather figure narrating an adventure tale",
                pacingNoteEn = "Measured, captivating, gentle storytelling pace",
                personaDescription = "راوی مردانه گرم، داستان‌گو و باوقار",
            ),
            "عمو باران" to CharacterVoiceSpec(
                speakerKey = "amo_baran",
                geminiVoiceName = "Algenib",
                playbackPitch = 0.91f,
                playbackSpeed = 0.94f,
                ttsPitch = 0.78f,
                ttsRate = 0.87f,
                audioProfileEn = "A kind, weathered elderly Iranian mountain sage with a bearded grandfatherly textured voice",
                pacingNoteEn = "Calm, hospitable, warm elder cadence",
                personaDescription = "پیرمرد مهربان و دانای کوهستان بَشاگَرد",
            ),
            "خاله زرین" to CharacterVoiceSpec(
                speakerKey = "khale_zarin",
                geminiVoiceName = "Gacrux",
                playbackPitch = 0.95f,
                playbackSpeed = 0.95f,
                ttsPitch = 1.03f,
                ttsRate = 0.89f,
                audioProfileEn = "A loving, mature elderly Iranian grandmother and traditional palm-weaving artisan",
                pacingNoteEn = "Gentle, affectionate, unhurried maternal tone",
                personaDescription = "مادربزرگ مهربان و هنرمند حصیرباف میناب",
            ),
            "ماهان" to CharacterVoiceSpec(
                speakerKey = "mahan",
                geminiVoiceName = "Achird",
                playbackPitch = 1.05f,
                playbackSpeed = 1.02f,
                ttsPitch = 1.08f,
                ttsRate = 1.02f,
                audioProfileEn = "An enthusiastic young Iranian male geology explorer in his early twenties",
                pacingNoteEn = "Friendly, bright, conversational pace",
                personaDescription = "پژوهشگر جوان و خوش‌صحبت جاسک",
            ),
            "ناخدا سهراب" to CharacterVoiceSpec(
                speakerKey = "nakhoda_sohrab",
                geminiVoiceName = "Enceladus",
                playbackPitch = 0.94f,
                playbackSpeed = 0.96f,
                ttsPitch = 0.86f,
                ttsRate = 0.94f,
                audioProfileEn = "A seasoned, calm older Iranian sea captain with a deep welcoming maritime voice",
                pacingNoteEn = "Steady, reassuring, salty harbor veteran tone",
                personaDescription = "ناخدای باتجربه و مهمان‌نواز سیریک",
            ),
            "ناخدا آرش" to CharacterVoiceSpec(
                speakerKey = "nakhoda_arash",
                geminiVoiceName = "Fenrir",
                playbackPitch = 0.99f,
                playbackSpeed = 1.01f,
                ttsPitch = 0.92f,
                ttsRate = 0.97f,
                audioProfileEn = "A proud, energetic adult Iranian island captain with a bold heroic voice",
                pacingNoteEn = "Confident, spirited, inspiring cadence",
                personaDescription = "ناخدای پرصلابت و صمیمی جزیره اَبوموسی",
            ),
            "کیانوش" to CharacterVoiceSpec(
                speakerKey = "kianoosh",
                geminiVoiceName = "Zubenelgenubi",
                playbackPitch = 1.06f,
                playbackSpeed = 1.01f,
                ttsPitch = 1.06f,
                ttsRate = 1.00f,
                audioProfileEn = "A cheerful, friendly young adult Iranian coastal nature guide",
                pacingNoteEn = "Relaxed, upbeat, welcoming tone",
                personaDescription = "راهنمای جوان و پرشور پارسیان",
            ),
            "کاپیتان رهام" to CharacterVoiceSpec(
                speakerKey = "captain_roham",
                geminiVoiceName = "Alnilam",
                playbackPitch = 1.00f,
                playbackSpeed = 1.00f,
                ttsPitch = 0.94f,
                ttsRate = 0.96f,
                audioProfileEn = "A firm, charismatic adult Iranian geopark explorer captain",
                pacingNoteEn = "Crisp, adventurous, assured delivery",
                personaDescription = "کاپیتان پرانرژی و طبیعت‌دوست ژئوپارک قِشم",
            ),
            "خانم ساحل" to CharacterVoiceSpec(
                speakerKey = "sahel",
                geminiVoiceName = "Aoede",
                playbackPitch = 1.03f,
                playbackSpeed = 1.00f,
                ttsPitch = 1.18f,
                ttsRate = 0.96f,
                audioProfileEn = "A warm, gentle adult Iranian woman marine ecologist who loves mangrove forests",
                pacingNoteEn = "Breezy, caring, articulate tone",
                personaDescription = "بانوی بوم‌شناس و دوستدار جنگل‌های حَرای بَندَرخَمیر",
            ),
            "استاد بهرام" to CharacterVoiceSpec(
                speakerKey = "ostad_bahram",
                geminiVoiceName = "Charon",
                playbackPitch = 0.95f,
                playbackSpeed = 0.96f,
                ttsPitch = 0.83f,
                ttsRate = 0.90f,
                audioProfileEn = "A calm, scholarly middle-aged Iranian agricultural professor",
                pacingNoteEn = "Thoughtful, clear, encouraging educator tone",
                personaDescription = "استاد کشاورزی و باغداری دانای رودان",
            ),
            "استاد فرهاد" to CharacterVoiceSpec(
                speakerKey = "ostad_farhad",
                geminiVoiceName = "Sadaltager",
                playbackPitch = 0.98f,
                playbackSpeed = 0.98f,
                ttsPitch = 0.95f,
                ttsRate = 0.94f,
                audioProfileEn = "A knowledgeable, cultured adult Iranian heritage architect",
                pacingNoteEn = "Articulate, poised, informative tone",
                personaDescription = "معمار سنتی و خوش‌فکر بادگیرهای بَستَک",
            ),
            "بانو مهرنوش" to CharacterVoiceSpec(
                speakerKey = "banoo_mehrnoosh",
                geminiVoiceName = "Despina",
                playbackPitch = 0.98f,
                playbackSpeed = 0.97f,
                ttsPitch = 1.08f,
                ttsRate = 0.88f,
                audioProfileEn = "A dignified, graceful elderly Iranian historian woman",
                pacingNoteEn = "Smooth, refined, gentle cadence",
                personaDescription = "بانوی فرهیخته و تاریخ‌دان بَندَرلِنگه",
            ),
            "عمو صابر" to CharacterVoiceSpec(
                speakerKey = "amo_saber",
                geminiVoiceName = "Orus",
                playbackPitch = 0.90f,
                playbackSpeed = 0.94f,
                ttsPitch = 0.82f,
                ttsRate = 0.89f,
                audioProfileEn = "A wise, soft-hearted elderly Iranian farmer and watermill keeper",
                pacingNoteEn = "Firm yet kind grandfatherly cadence",
                personaDescription = "پیرمرد دانای آبشار تَزَرج و آسیاب‌های آبی حاجی‌آباد",
            ),
            "دکتر دریا" to CharacterVoiceSpec(
                speakerKey = "dr_darya",
                geminiVoiceName = "Kore",
                playbackPitch = 1.00f,
                playbackSpeed = 0.99f,
                ttsPitch = 1.15f,
                ttsRate = 0.95f,
                audioProfileEn = "A confident, intelligent adult Iranian female cultural and environmental scientist",
                pacingNoteEn = "Clear, bright, reassuring expert delivery",
                personaDescription = "پژوهشگر علمی و دقیق تاریخ و طبیعت بَندَرعَبّاس",
            ),
        )

        private val PRONUNCIATION_RULES: List<Pair<Regex, String>> = listOf(
            "بشاگرد" to "بَشاگَرد",
            "هرمزگان" to "هُرمُزگان",
            "بندرعباس" to "بَندَرعَبّاس",
            "بندرخمیر" to "بَندَرخَمیر",
            "بندر خمیر" to "بَندَر خَمیر",
            "بندرلنگه" to "بَندَرلِنگه",
            "بندر لنگه" to "بَندَر لِنگه",
            "ابوموسی" to "اَبوموسی",
            "خور آذینی" to "خورِ آذینی",
            "پنجشنبه‌بازار" to "پَنج‌شَنبه‌بازار",
            "آبسنگ‌های" to "آب‌سَنگ‌های",
            "آبسنگ" to "آب‌سَنگ",
            "برکه‌های" to "بِرکه‌های",
            "سده‌های" to "سَده‌های",
            "کم‌لغزش" to "کَم‌لَغزِش",
            "جوانه‌زنی" to "جَوانه‌زَنی",
            "چاهکوه" to "چاه‌کوه",
            "بستک" to "بَستَک",
            "تزرج" to "تَزَرج",
            "مکسر" to "مُکَسَّر",
            "کهور" to "کَهور",
            "جگین" to "جَگین",
            "کمیز" to "کُمیز",
        ).map { (plain, vocalized) ->
            Regex.fromLiteral(plain) to vocalized
        } + listOf(
            "برکه" to "بِرکه",
            "قشم" to "قِشم",
            "گنو" to "گِنو",
            "حرای" to "حَرای",
            "حرا" to "حَرا",
            "گوده" to "گودِه",
            "بویه" to "بویِه",
            "جزر" to "جَزر",
            "تنگه" to "تَنگه",
            "قلعه" to "قَلعه",
            "بذر" to "بَذر",
            "گره" to "گِره",
            "تلا" to "تَلا",
            "بنه" to "بَنِه",
        ).map { (word, vocalized) ->
            Regex("(?<![\\u0600-\\u06FF])${Regex.escape(word)}(?![\\u0600-\\u06FF])") to vocalized
        }

        /**
         * Adds accurate Persian diacritics (اعراب/حرکات) to Hormozgan proper nouns and local terms
         * before sending text to TTS, ensuring words like «بَشاگَرد» are pronounced accurately.
         */
        fun formatSpeechTranscript(rawText: String): String {
            var result = rawText
            for ((pattern, vocalized) in PRONUNCIATION_RULES) {
                result = pattern.replace(result, vocalized)
            }
            return result
        }

        /**
         * Uses Gemini TTS Audio Profile + Director's Notes + Transcript format so the model:
         * 1) adopts the exact character age/persona/appearance voice without speaking instructions aloud,
         * 2) strictly pronounces tricky Hormozgan names like «بَشاگَرد» (Ba-shā-gard, NOT shāgerd).
         */
        fun buildGeminiTtsPrompt(
            line: DialogueLine,
            charSpec: CharacterVoiceSpec,
        ): String {
            val vocalizedTranscript = formatSpeechTranscript(line.text)
            val pronunciationHints = buildList {
                if (vocalizedTranscript.contains("بَشاگَرد")) {
                    add("Pronounce \"بَشاگَرد\" strictly as Ba-shā-gard (بَ-شا-گَرد, with 'a' vowels, NEVER as be-shāgerd or shāgerd).")
                }
                if (vocalizedTranscript.contains("هُرمُزگان")) {
                    add("Pronounce \"هُرمُزگان\" as Hor-moz-gān.")
                }
                if (vocalizedTranscript.contains("گِنو")) {
                    add("Pronounce \"گِنو\" as Ge-no.")
                }
                if (vocalizedTranscript.contains("تَزَرج")) {
                    add("Pronounce \"تَزَرج\" as Ta-zarj.")
                }
                if (vocalizedTranscript.contains("بَنِه") || vocalizedTranscript.contains("کَهور")) {
                    add("Pronounce \"بَنِه\" as Ba-neh and \"کَهور\" as Ka-hoor.")
                }
                if (vocalizedTranscript.contains("تَلا")) {
                    add("Pronounce \"تَلا\" as Ta-lā.")
                }
            }
            return buildString {
                appendLine("# AUDIO PROFILE: ${charSpec.audioProfileEn}.")
                appendLine("### DIRECTOR'S NOTES")
                appendLine("Language: Standard natural Iranian Persian (Farsi).")
                appendLine("Pacing & Tone: ${charSpec.pacingNoteEn}.")
                if (pronunciationHints.isNotEmpty()) {
                    appendLine("Pronunciation: ${pronunciationHints.joinToString(" ")}")
                }
                appendLine()
                appendLine("#### TRANSCRIPT")
                append(vocalizedTranscript)
            }
        }

        fun resolveCharacterVoice(line: DialogueLine): CharacterVoiceSpec {
            return CHARACTER_VOICE_SPECS[line.speakerName] ?: CharacterVoiceSpec(
                speakerKey = if (line.isAria) "aria" else "narrator",
                geminiVoiceName = when (line.voiceProfile) {
                    VoiceProfile.ARIA -> "Puck"
                    VoiceProfile.NARRATOR -> "Rasalgethi"
                    VoiceProfile.ELDER_MALE -> "Algenib"
                    VoiceProfile.ADULT_MALE -> "Fenrir"
                    VoiceProfile.YOUTH_MALE -> "Achird"
                    VoiceProfile.ADULT_FEMALE -> "Aoede"
                    VoiceProfile.ELDER_FEMALE -> "Gacrux"
                },
                playbackPitch = if (line.isAria) 1.24f else 1.0f,
                playbackSpeed = if (line.isAria) 1.03f else 0.98f,
                ttsPitch = line.voiceProfile.pitch,
                ttsRate = line.voiceProfile.rate,
                audioProfileEn = "A natural Iranian Persian speaker (${line.speakerName})",
                pacingNoteEn = "Warm and clear Iranian Persian",
                personaDescription = line.voiceProfile.geminiStyleHint,
            )
        }
    }
}
