package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AudioSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.audioDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "hormozgan_audio_settings",
)

class AudioSettingsStore(private val context: Context) {
    private object Keys {
        val MUSIC_VOLUME = floatPreferencesKey("music_volume")
        val VOICE_VOLUME = floatPreferencesKey("voice_volume")
        val SFX_VOLUME = floatPreferencesKey("sfx_volume")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        val SFX_ENABLED = booleanPreferencesKey("sfx_enabled")
        val PREFER_GEMINI_TTS = booleanPreferencesKey("prefer_gemini_tts")
    }

    val audioSettings: Flow<AudioSettings> = context.audioDataStore.data.map { prefs ->
        AudioSettings(
            musicVolume = prefs[Keys.MUSIC_VOLUME] ?: 0.65f,
            voiceVolume = prefs[Keys.VOICE_VOLUME] ?: 0.90f,
            sfxVolume = prefs[Keys.SFX_VOLUME] ?: 0.80f,
            musicEnabled = prefs[Keys.MUSIC_ENABLED] ?: true,
            voiceEnabled = prefs[Keys.VOICE_ENABLED] ?: true,
            sfxEnabled = prefs[Keys.SFX_ENABLED] ?: true,
            preferGeminiTts = prefs[Keys.PREFER_GEMINI_TTS] ?: true,
        )
    }

    suspend fun updateSettings(transform: (AudioSettings) -> AudioSettings) {
        context.audioDataStore.edit { prefs ->
            val current = AudioSettings(
                musicVolume = prefs[Keys.MUSIC_VOLUME] ?: 0.65f,
                voiceVolume = prefs[Keys.VOICE_VOLUME] ?: 0.90f,
                sfxVolume = prefs[Keys.SFX_VOLUME] ?: 0.80f,
                musicEnabled = prefs[Keys.MUSIC_ENABLED] ?: true,
                voiceEnabled = prefs[Keys.VOICE_ENABLED] ?: true,
                sfxEnabled = prefs[Keys.SFX_ENABLED] ?: true,
                preferGeminiTts = prefs[Keys.PREFER_GEMINI_TTS] ?: true,
            )
            val updated = transform(current)
            prefs[Keys.MUSIC_VOLUME] = updated.musicVolume.coerceIn(0f, 1f)
            prefs[Keys.VOICE_VOLUME] = updated.voiceVolume.coerceIn(0f, 1f)
            prefs[Keys.SFX_VOLUME] = updated.sfxVolume.coerceIn(0f, 1f)
            prefs[Keys.MUSIC_ENABLED] = updated.musicEnabled
            prefs[Keys.VOICE_ENABLED] = updated.voiceEnabled
            prefs[Keys.SFX_ENABLED] = updated.sfxEnabled
            prefs[Keys.PREFER_GEMINI_TTS] = updated.preferGeminiTts
        }
    }
}
