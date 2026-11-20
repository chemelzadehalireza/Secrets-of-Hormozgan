package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.AudioSettings
import com.example.model.BiomeType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

enum class SoundEffect {
    FOOTSTEP,
    OPEN_JOURNAL,
    CLUE_FOUND,
    PUZZLE_SOLVED,
    REWARD_FANFARE,
    STAGE_UNLOCK,
    DIALOGUE_BLIP,
    OBJECT_INTERACT,
    MENU_BUTTON,
    ERROR_GENTLE,
}

class SoundEngine {
    private val sampleRate = 22050
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var settings: AudioSettings = AudioSettings()

    @Volatile
    private var currentBiome: BiomeType = BiomeType.MOUNTAIN

    @Volatile
    private var isPaused: Boolean = false

    private var ambientJob: Job? = null
    private var lastFootstepMs = 0L

    private val cachedSfx: Map<SoundEffect, ShortArray> by lazy {
        mapOf(
            SoundEffect.FOOTSTEP to synthesizeNoiseTap(95, 0.25f),
            SoundEffect.OPEN_JOURNAL to synthesizeSweep(280f, 560f, 180, 0.35f),
            SoundEffect.CLUE_FOUND to synthesizeArpeggio(listOf(523.25f, 659.25f, 783.99f, 1046.5f), 75, 0.50f),
            SoundEffect.PUZZLE_SOLVED to synthesizeArpeggio(listOf(440f, 554.37f, 659.25f, 880f), 90, 0.55f),
            SoundEffect.REWARD_FANFARE to synthesizeArpeggio(listOf(392f, 493.88f, 587.33f, 783.99f, 987.77f), 115, 0.65f),
            SoundEffect.STAGE_UNLOCK to synthesizeArpeggio(listOf(329.63f, 440f, 554.37f, 659.25f), 100, 0.50f),
            SoundEffect.DIALOGUE_BLIP to synthesizeTone(580f, 38, 0.22f),
            SoundEffect.OBJECT_INTERACT to synthesizeTone(420f, 75, 0.38f),
            SoundEffect.MENU_BUTTON to synthesizeTone(510f, 55, 0.32f),
            SoundEffect.ERROR_GENTLE to synthesizeSweep(260f, 175f, 170, 0.38f),
        )
    }

    fun updateSettings(newSettings: AudioSettings) {
        settings = newSettings
        if (!newSettings.musicEnabled && ambientJob != null) {
            ambientJob?.cancel()
            ambientJob = null
        } else if (newSettings.musicEnabled && !isPaused && ambientJob == null) {
            startAmbientMusic(currentBiome)
        }
    }

    fun setBiome(biome: BiomeType) {
        if (currentBiome != biome) {
            currentBiome = biome
            if (settings.musicEnabled && !isPaused) {
                startAmbientMusic(biome)
            }
        }
    }

    fun onPause() {
        isPaused = true
        ambientJob?.cancel()
        ambientJob = null
    }

    fun onResume() {
        isPaused = false
        if (settings.musicEnabled) {
            startAmbientMusic(currentBiome)
        }
    }

    fun playFootstep() {
        val now = System.currentTimeMillis()
        if (now - lastFootstepMs < 240L) return
        lastFootstepMs = now
        playSfx(SoundEffect.FOOTSTEP)
    }

    fun playSfx(effect: SoundEffect) {
        val cfg = settings
        if (!cfg.sfxEnabled || cfg.sfxVolume <= 0.01f) return
        val pcm = cachedSfx[effect] ?: return
        val vol = cfg.sfxVolume
        scope.launch {
            playPcmBuffer(pcm, vol)
        }
    }

    fun startAmbientMusic(biome: BiomeType) {
        currentBiome = biome
        ambientJob?.cancel()
        if (!settings.musicEnabled || isPaused) return

        ambientJob = scope.launch {
            // Soft traditional southern Iranian / pentatonic atmospheric notes + gentle nature texture
            val scaleNotes = when (biome) {
                BiomeType.MOUNTAIN -> listOf(220.0f, 261.63f, 293.66f, 329.63f, 392.0f, 440.0f)
                BiomeType.COASTAL_MANGROVE, BiomeType.OCEAN_CORAL -> listOf(196.0f, 246.94f, 293.66f, 329.63f, 392.0f)
                BiomeType.FOREST_GROVE, BiomeType.WATERFALL_VALLEY -> listOf(261.63f, 293.66f, 349.23f, 392.0f, 440.0f, 523.25f)
                BiomeType.HISTORIC_WINDTOWER -> listOf(220.0f, 246.94f, 293.66f, 329.63f, 369.99f, 440.0f)
                BiomeType.ISLAND_GEOPARK, BiomeType.MARTIAN_CLIFFS -> listOf(174.61f, 220.0f, 261.63f, 329.63f, 392.0f)
            }
            var stepIndex = 0
            while (isActive) {
                val cfg = settings
                if (!cfg.musicEnabled || isPaused) {
                    delay(400)
                    continue
                }
                val note = scaleNotes[(stepIndex * 3 + Random.nextInt(0, 2)) % scaleNotes.size]
                val bar = synthesizeAmbientPhrase(note, biome)
                playPcmBuffer(bar, cfg.musicVolume * 0.28f)
                stepIndex++
                delay(420)
            }
        }
    }

    private fun synthesizeAmbientPhrase(freq: Float, biome: BiomeType): ShortArray {
        val durationMs = 1350
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        var noiseState = 0f
        val rnd = Random(freq.toInt())
        val windStrength = when (biome) {
            BiomeType.MOUNTAIN, BiomeType.MARTIAN_CLIFFS -> 0.14f
            BiomeType.COASTAL_MANGROVE, BiomeType.OCEAN_CORAL -> 0.18f
            BiomeType.WATERFALL_VALLEY -> 0.16f
            else -> 0.08f
        }
        for (i in 0 until count) {
            val t = i.toFloat() / sampleRate
            val env = sin(PI.toFloat() * (i.toFloat() / count)) * exp(-t * 1.4f)
            // Plucked santoor/oud harmonic warmth
            val fundamental = sin(2.0 * PI * freq * t).toFloat()
            val harmonic = 0.35f * sin(2.0 * PI * freq * 2.0 * t).toFloat() * exp(-t * 3.0f)
            // Filtered soft wind/wave texture
            val white = rnd.nextFloat() * 2f - 1f
            noiseState = noiseState * 0.94f + white * 0.06f
            val waveMod = 0.5f + 0.5f * sin(2.0 * PI * 0.45 * t).toFloat()
            val sample = ((fundamental + harmonic) * env * 0.48f) + (noiseState * windStrength * waveMod)
            out[i] = (sample.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    private fun synthesizeTone(freq: Float, durationMs: Int, amp: Float): ShortArray {
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toFloat() / sampleRate
            val env = sin(PI.toFloat() * (i.toFloat() / count))
            val s = sin(2.0 * PI * freq * t).toFloat() * env * amp
            out[i] = (s.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    private fun synthesizeSweep(startFreq: Float, endFreq: Float, durationMs: Int, amp: Float): ShortArray {
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val progress = i.toFloat() / count
            val freq = startFreq + (endFreq - startFreq) * progress
            phase += 2.0 * PI * freq / sampleRate
            val env = sin(PI.toFloat() * progress)
            val s = sin(phase).toFloat() * env * amp
            out[i] = (s.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    private fun synthesizeArpeggio(freqs: List<Float>, noteMs: Int, amp: Float): ShortArray {
        val perNote = (sampleRate * noteMs) / 1000
        val total = perNote * freqs.size + (sampleRate * 160) / 1000
        val out = ShortArray(total)
        freqs.forEachIndexed { idx, freq ->
            val start = idx * perNote
            val len = minOf(total - start, perNote + (sampleRate * 140) / 1000)
            for (i in 0 until len) {
                val t = i.toFloat() / sampleRate
                val env = exp(-t * 9.0f) * minOf(1f, i / 120f)
                val v = (sin(2.0 * PI * freq * t).toFloat() + 0.25f * sin(4.0 * PI * freq * t).toFloat()) * env * amp
                val mixed = (out[start + i] / 32767f) + v
                out[start + i] = (mixed.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
            }
        }
        return out
    }

    private fun synthesizeNoiseTap(durationMs: Int, amp: Float): ShortArray {
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        val rnd = Random(42)
        var lp = 0f
        for (i in 0 until count) {
            val p = i.toFloat() / count
            val env = (1f - p) * (1f - p)
            val n = rnd.nextFloat() * 2f - 1f
            lp = lp * 0.72f + n * 0.28f
            out[i] = (lp * env * amp * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    private fun playPcmBuffer(pcm: ShortArray, volume: Float) {
        try {
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
            )
            val byteCount = pcm.size * 2
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setBufferSizeInBytes(maxOf(minBuf, byteCount))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.setVolume(volume.coerceIn(0f, 1f))
            track.write(pcm, 0, pcm.size)
            track.play()
            val sleepMs = (pcm.size * 1000L) / sampleRate + 30L
            Thread.sleep(sleepMs)
            track.stop()
            track.release()
        } catch (_: Throwable) {
            // Audio device unavailable or interrupted; ignore safely
        }
    }

    fun release() {
        ambientJob?.cancel()
        ambientJob = null
    }
}
