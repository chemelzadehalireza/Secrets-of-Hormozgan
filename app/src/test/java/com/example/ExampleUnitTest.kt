package com.example

import com.example.audio.VoiceManager
import com.example.data.catalog.HormozganCatalog
import com.example.model.AriaAnimationState
import com.example.model.AudioSettings
import com.example.model.PuzzlePayload
import com.example.util.formatPersianPercent
import com.example.util.toPersianDigits
import com.example.util.toPersianNumber
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `all 13 hormozgan counties are present with valid missions and treasures`() {
        val counties = HormozganCatalog.allCounties
        assertEquals(13, counties.size)
        assertEquals("bashagard", counties.first().id)
        assertEquals("بشاگرد", counties.first().nameFa)
        assertEquals("عمو باران", counties.first().npcName)

        val uniqueIds = counties.map { it.id }.toSet()
        assertEquals(13, uniqueIds.size)

        counties.forEachIndexed { idx, county ->
            assertEquals(idx + 1, county.treasurePiece.index)
            assertTrue("County ${county.id} should have 3 missions", county.missions.size >= 3)
            county.missions.forEach { mission ->
                when (val payload = mission.puzzlePayload) {
                    is PuzzlePayload.PathGrid -> {
                        assertEquals(payload.rows * payload.cols, payload.cells.size)
                        assertTrue(payload.cells.contains(2))
                        assertTrue(payload.cells.contains(3))
                    }
                    is PuzzlePayload.InteractiveChallenge -> {
                        assertTrue(payload.options.any { it.isCorrect })
                    }
                    is PuzzlePayload.HiddenSpots -> {
                        assertTrue(payload.spots.any { it.isTarget })
                    }
                    is PuzzlePayload.PatternMatrix -> {
                        assertEquals(payload.size * payload.size, payload.targetPattern.size)
                        assertEquals(payload.targetPattern.size, payload.initialPattern.size)
                    }
                    else -> Unit
                }
            }
        }
    }

    @Test
    fun `aria has all 14 animation states and encyclopedia has 10 pillars`() {
        assertEquals(14, AriaAnimationState.entries.size)
        assertEquals(10, HormozganCatalog.encyclopediaPillars.size)
    }

    @Test
    fun `persian number formatting converts digits accurately`() {
        assertEquals("۱۳", 13.toPersianNumber())
        assertEquals("۷۵٪", formatPersianPercent(75))
        assertEquals("ارتفاع: ۱۱۸۰ متر", "ارتفاع: 1180 متر".toPersianDigits())
    }

    @Test
    fun `audio settings default to enabling character voice synthesis with offline caching`() {
        val defaults = AudioSettings()
        assertTrue(defaults.preferGeminiTts)
        assertTrue(defaults.voiceEnabled)
        assertTrue(defaults.musicEnabled)
        assertTrue(defaults.sfxEnabled)
    }

    @Test
    fun `all 15 characters have 15 completely unique Google AI Studio voices and youth pitch for Aria`() {
        val specs = VoiceManager.CHARACTER_VOICE_SPECS
        assertEquals(15, specs.size)
        assertTrue(specs.containsKey("آریا"))
        assertTrue(specs.containsKey("راوی"))
        HormozganCatalog.allCounties.forEach { county ->
            assertTrue("Missing voice spec for ${county.npcName}", specs.containsKey(county.npcName))
        }
        assertEquals(15, specs.values.map { it.speakerKey }.toSet().size)
        assertEquals(15, specs.values.map { it.geminiVoiceName }.toSet().size)
        val ariaSpec = specs.getValue("آریا")
        assertTrue("Aria should have elevated youthful pitch", ariaSpec.playbackPitch >= 1.18f)
        specs.values.forEach { spec ->
            assertTrue(spec.geminiVoiceName.isNotBlank())
            assertTrue(spec.personaDescription.isNotBlank())
        }
        val allLines = HormozganCatalog.allDialogueLines()
        assertTrue("Expected narrative, mission goal/success, and treasure lines", allLines.size >= 170)
        assertEquals(allLines.size, allLines.map { it.id }.toSet().size)
        assertTrue(allLines.any { it.id == HormozganCatalog.introDialogueLine.id })
        assertTrue(allLines.any { it.id == HormozganCatalog.encyclopediaFinaleLine.id })
        HormozganCatalog.allCounties.forEach { county ->
            val stageLines = HormozganCatalog.countyDialogueLines(county)
            assertTrue(allLines.any { it.id == "${county.id}_treasure_reward" })
            county.missions.forEach { mission ->
                assertTrue(allLines.any { it.id == "${mission.id}_goal" })
                assertTrue(allLines.any { it.id == "${mission.id}_success" })
                val payload = mission.puzzlePayload
                if (payload is PuzzlePayload.WaterSlope) {
                    val expectedId = HormozganCatalog.missionFeedbackLineId(mission.id, "solved", payload.explanation)
                    assertTrue(stageLines.any { it.id == expectedId })
                }
                if (payload is PuzzlePayload.InteractiveChallenge) {
                    payload.options.forEach { opt ->
                        val kind = if (opt.isCorrect) "solved" else "hint"
                        val expectedId = HormozganCatalog.missionFeedbackLineId(mission.id, kind, opt.feedbackText)
                        assertTrue(stageLines.any { it.id == expectedId })
                    }
                }
            }
        }
    }

    @Test
    fun `persian speech transcript adds diacritics for Bashagard and builds explicit pronunciation prompt`() {
        assertEquals(
            "در کوهستان‌های بَشاگَرد و چاه تَلا کنار درخت کَهور",
            VoiceManager.formatSpeechTranscript("در کوهستان‌های بشاگرد و چاه تلا کنار درخت کهور"),
        )
        assertEquals("تلاش برای حفظ هُرمُزگان", VoiceManager.formatSpeechTranscript("تلاش برای حفظ هرمزگان"))

        val bashagardLine = HormozganCatalog.getById("bashagard").openingDialogues.first()
        val prompt = VoiceManager.buildGeminiTtsPrompt(
            line = bashagardLine,
            charSpec = VoiceManager.resolveCharacterVoice(bashagardLine),
        )
        assertTrue(prompt.contains("بَشاگَرد"))
        assertTrue(prompt.contains("Ba-shā-gard"))
        assertTrue(prompt.contains("#### TRANSCRIPT"))
        assertFalse(VoiceManager.CHARACTER_VOICE_SPECS.values.any { it.speakerKey.isBlank() })
    }
}
