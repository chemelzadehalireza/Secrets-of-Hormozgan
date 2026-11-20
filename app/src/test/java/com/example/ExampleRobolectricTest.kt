package com.example

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import androidx.test.core.app.ApplicationProvider
import com.example.audio.VoiceManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read game title and all local persian font resources from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("هرمزگان؛ سرزمین رازهای پنهان", appName)

        val fontIds = listOf(
            R.font.vazirmatn_family,
            R.font.vazirmatn,
            R.font.vazirmatn_bold,
            R.font.estedad,
            R.font.estedad_bold,
            R.font.shabnam,
            R.font.gulzar,
            R.font.noto_nastaliq_urdu,
            R.font.lalezar,
            R.font.markazi_text,
            R.font.ibm_plex_sans_arabic,
        )
        fontIds.forEach { fontId ->
            assertNotNull("Font $fontId failed to load", ResourcesCompat.getFont(context, fontId))
        }
    }

    @Test
    fun `bundled offline character signatures and dialogue script exist in assets`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val voiceRoot = context.assets.list("voices")?.toSet().orEmpty()
        assertTrue(voiceRoot.contains("DIALOGUE_AUDIO_MAP.txt"))
        assertTrue(voiceRoot.contains("dialogues_script.json"))

        val charAssets = context.assets.list("voices/characters")?.toSet().orEmpty()
        assertEquals(15, VoiceManager.CHARACTER_VOICE_SPECS.size)
        VoiceManager.CHARACTER_VOICE_SPECS.values.forEach { spec ->
            assertTrue(
                "Missing character audio signature ${spec.speakerKey}.ogg",
                charAssets.contains("${spec.speakerKey}.ogg"),
            )
        }

        val allLines = com.example.data.catalog.HormozganCatalog.allDialogueLines()
        val missingAudioIds = allLines.map { it.id }.filter { id -> !voiceRoot.contains("$id.ogg") }
        assertTrue("Missing bundled APK voice files: $missingAudioIds", missingAudioIds.isEmpty())
    }
}
