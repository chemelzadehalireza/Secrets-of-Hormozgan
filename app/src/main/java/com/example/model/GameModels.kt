package com.example.model

enum class AriaAnimationState {
    IDLE,           // ۱. حالت ایستادن و نفس کشیدن
    WALK,           // ۲. راه رفتن
    RUN,            // ۳. دویدن
    JUMP,           // ۴. پریدن
    LOOK_AROUND,    // ۵. نگاه کردن به اطراف
    TALK,           // ۶. صحبت کردن
    SURPRISED,      // ۷. تعجب کردن
    HAPPY,          // ۸. خوشحالی
    SAD,            // ۹. ناراحتی
    THINK,          // ۱۰. فکر کردن
    OPEN_NOTEBOOK,  // ۱۱. باز کردن دفترچه
    PICK_UP,        // ۱۲. برداشتن اشیا
    DISCOVER_CLUE,  // ۱۳. کشف یک سرنخ
    RECEIVE_REWARD, // ۱۴. دریافت جایزه
}

enum class BiomeType {
    MOUNTAIN,
    FOREST_GROVE,
    WATERFALL_VALLEY,
    COASTAL_MANGROVE,
    MARTIAN_CLIFFS,
    HISTORIC_WINDTOWER,
    ISLAND_GEOPARK,
    OCEAN_CORAL,
}

enum class PuzzleType {
    PATH_FINDING,           // مسیریابی
    SCIENCE_WATER_SLOPE,    // معمای علمی جریان آب و شیب زمین
    HIDDEN_OBJECT,          // پیدا کردن اشیای مخفی
    GEOMETRIC_PATTERN,      // الگوهای هندسی
    SEQUENCE_ORDER,         // مرتب کردن قطعات / مراحل
    IMAGE_MATCH,            // تشخیص تصاویر
    GEOGRAPHY_QUIZ,         // معمای جغرافیایی
    ECO_BALANCE,            // معمای محیط‌زیستی
    OBSERVATION_DIFF,       // مشاهده و دقت
    ENVIRONMENT_INTERACTION,// تعامل با اشیای محیط
}

enum class VoiceProfile(
    val pitch: Float,
    val rate: Float,
    val geminiStyleHint: String,
) {
    ARIA(1.12f, 1.03f, "نوجوان ۱۵ ساله پرانرژی، کنجکاو و صمیمی"),
    NARRATOR(0.84f, 0.92f, "راوی مردانه گرم، داستان‌گو و سینمایی"),
    ELDER_MALE(0.80f, 0.88f, "پیرمرد مهربان، دانا و باوقار محلی"),
    ADULT_MALE(0.92f, 0.96f, "مرد جوان یا میان‌سال گرم و مهمان‌نواز"),
    YOUTH_MALE(1.06f, 1.00f, "پژوهشگر جوان و مشتاق"),
    ADULT_FEMALE(1.18f, 0.96f, "بانوی مهربان، فرهیخته و صمیمی"),
    ELDER_FEMALE(1.05f, 0.90f, "مادربزرگ هنرمند و مهربان محلی"),
}

enum class HeadwearStyle {
    NONE,
    SOUTHERN_CAP,
    TURBAN_LIGHT,
    CAPTAIN_HAT,
    TRADITIONAL_SHAWL,
    SUN_HAT,
}

data class NpcAppearance(
    val primaryColorHex: Long,
    val accentColorHex: Long,
    val skinToneHex: Long,
    val headwear: HeadwearStyle,
    val hasBeard: Boolean,
    val isFemale: Boolean,
)

data class DialogueLine(
    val id: String,
    val speakerName: String,
    val isAria: Boolean,
    val voiceProfile: VoiceProfile,
    val ariaEmotion: AriaAnimationState,
    val text: String,
)

enum class WorldPropType {
    MOUNTAIN_SIGNPOST,
    WATER_CHANNEL_STONE,
    ROCK_CARVING_ALCOVE,
    WEAVING_LOOM,
    GARDEN_GATE,
    BOAT_DOCK,
    FOSSIL_ROCK,
    WINDTOWER_MODEL,
    SALT_CRYSTAL_SHRINE,
    MANGROVE_SAPLING,
    WATERMILL_WHEEL,
    ANCIENT_COMPASS,
    CORAL_BUOY,
}

sealed interface PuzzlePayload {
    // 1. Path finding on a 4x4 grid (0=empty, 1=hazard/cliff, 2=start, 3=goal)
    data class PathGrid(
        val rows: Int,
        val cols: Int,
        val cells: List<Int>,
        val hintText: String,
    ) : PuzzlePayload

    // 2. Water flow & ground slope puzzle: pick/order 4 elevation tiles from highest to lowest
    data class WaterSlope(
        val channelSegments: List<SlopeSegment>,
        val explanation: String,
    ) : PuzzlePayload

    // 3. Hidden object discovery inside interactive scene spots
    data class HiddenSpots(
        val spots: List<SceneSpot>,
        val targetItemName: String,
    ) : PuzzlePayload

    // 4. Geometric pattern completion (tap cells to match symmetric target motif)
    data class PatternMatrix(
        val size: Int,
        val targetPattern: List<Boolean>,
        val initialPattern: List<Boolean>,
        val motifName: String,
    ) : PuzzlePayload

    // 5. Sequence ordering (tap items in the logical/chronological order)
    data class SequenceItems(
        val correctOrder: List<String>,
        val promptNote: String,
    ) : PuzzlePayload

    // 6. Multi-choice interactive discovery (Eco, Geo, Science, Observation, Image match)
    data class InteractiveChallenge(
        val question: String,
        val visualCueTitle: String,
        val options: List<ChallengeOption>,
    ) : PuzzlePayload

    // 7. Environmental interaction: activate or balance 3 interactive levers/elements to target values
    data class MechanismBalance(
        val elements: List<MechanismElement>,
        val targetHint: String,
    ) : PuzzlePayload
}

data class SlopeSegment(
    val id: String,
    val label: String,
    val elevationMeters: Int,
)

data class SceneSpot(
    val id: String,
    val label: String,
    val xFraction: Float,
    val yFraction: Float,
    val isTarget: Boolean,
    val inspectionText: String,
)

data class ChallengeOption(
    val id: String,
    val label: String,
    val isCorrect: Boolean,
    val feedbackText: String,
)

data class MechanismElement(
    val id: String,
    val label: String,
    val targetLevel: Int, // 1..3
    val unitLabel: String,
)

data class CountyMission(
    val id: String,
    val orderIndex: Int,
    val title: String,
    val shortGoal: String,
    val worldX: Float,
    val propType: WorldPropType,
    val puzzleType: PuzzleType,
    val puzzlePayload: PuzzlePayload,
    val introDialogues: List<DialogueLine>,
    val successMessage: String,
    val clueRewardTitle: String,
)

data class TreasurePiece(
    val index: Int,
    val title: String,
    val pillarTheme: String,
    val shortMeaning: String,
    val encyclopediaDetail: String,
)

data class ScientificBrief(
    val landmarkTitle: String,
    val geographySummary: String,
    val ecologyAndScience: String,
    val culturalHeritage: String,
    val sourceNote: String,
)

data class CountyDefinition(
    val id: String,
    val order: Int,
    val nameFa: String,
    val stageTitle: String,
    val subtitle: String,
    val biome: BiomeType,
    val mapX: Float, // 0f..1f normalized from West (0) to East (1)
    val mapY: Float, // 0f..1f normalized from North (0) to South (1)
    val unlocksNextIds: List<String>,
    val npcName: String,
    val npcRole: String,
    val npcVoice: VoiceProfile,
    val npcAppearance: NpcAppearance,
    val npcWorldX: Float,
    val openingDialogues: List<DialogueLine>,
    val farewellDialogues: List<DialogueLine>,
    val missions: List<CountyMission>,
    val treasurePiece: TreasurePiece,
    val scientificBrief: ScientificBrief,
)

data class EncyclopediaPillar(
    val number: Int,
    val title: String,
    val summary: String,
    val relatedCounties: String,
)

data class CountyProgressState(
    val countyId: String,
    val isUnlocked: Boolean,
    val isCompleted: Boolean,
    val completedMissionIds: Set<String>,
    val treasureCollected: Boolean,
    val npcMet: Boolean,
)

data class AudioSettings(
    val musicVolume: Float = 0.65f,
    val voiceVolume: Float = 0.90f,
    val sfxVolume: Float = 0.80f,
    val musicEnabled: Boolean = true,
    val voiceEnabled: Boolean = true,
    val sfxEnabled: Boolean = true,
    val preferGeminiTts: Boolean = true,
)
