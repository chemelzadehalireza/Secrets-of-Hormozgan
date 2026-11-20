package com.example.data.catalog

import com.example.model.*
import com.example.util.toPersianNumber

val westernAndIslandCounties: List<CountyDefinition> = listOf(
    // ۷. قشم
    CountyDefinition(
        id = "qeshm",
        order = 7,
        nameFa = "قشم",
        stageTitle = "شگفتی‌های ژئوپارک جهانی",
        subtitle = "دره ستارگان، تنگه چاهکوه، غار نمکدان و بافت تاریخی لافت",
        biome = BiomeType.ISLAND_GEOPARK,
        mapX = 0.50f,
        mapY = 0.62f,
        unlocksNextIds = listOf("abumusa", "khamir"),
        npcName = "کاپیتان رهام",
        npcRole = "راهنمای ژئوپارک جهانی و طبیعت‌گرد قشم (شخصیت تخیلی)",
        npcVoice = VoiceProfile.ADULT_MALE,
        npcAppearance = NpcAppearance(
            primaryColorHex = 0xFF0E7490,
            accentColorHex = 0xFFE09F3E,
            skinToneHex = 0xFFE0AC69,
            headwear = HeadwearStyle.SUN_HAT,
            hasBeard = false,
            isFemale = false,
        ),
        npcWorldX = 660f,
        openingDialogues = listOf(
            DialogueLine(
                id = "qsh_open_1",
                speakerName = "کاپیتان رهام",
                isAria = false,
                voiceProfile = VoiceProfile.ADULT_MALE,
                ariaEmotion = AriaAnimationState.SURPRISED,
                text = "به بزرگ‌ترین جزیره خلیج فارس و نخستین ژئوپارک جهانی یونسکو در خاورمیانه خوش آمدی آریا! من رهام هستم.",
            ),
            DialogueLine(
                id = "qsh_open_2",
                speakerName = "آریا",
                isAria = true,
                voiceProfile = VoiceProfile.ARIA,
                ariaEmotion = AriaAnimationState.LOOK_AROUND,
                text = "وای! ستون‌های صخره‌ای دره ستارگان و دیواره‌های تراش‌خورده تنگه چاهکوه مثل یک سیاره افسانه‌ای هستند!",
            ),
        ),
        farewellDialogues = listOf(
            DialogueLine(
                id = "qsh_bye_1",
                speakerName = "کاپیتان رهام",
                isAria = false,
                voiceProfile = VoiceProfile.ADULT_MALE,
                ariaEmotion = AriaAnimationState.RECEIVE_REWARD,
                text = "عالی بود آریا! حالا می‌دانی چرا ژئوپارک قشم گنجینه‌ای جهانی برای علم زمین‌شناسی است. این هم قطعه هفتم گنج!",
            ),
        ),
        missions = listOf(
            CountyMission(
                id = "qsh_m1",
                orderIndex = 1,
                title = "راز دیواره‌های تنگه چاهکوه",
                shortGoal = "تشخیص چگونگی تراش خوردن حفره‌های کروی در صخره",
                worldX = 1140f,
                propType = WorldPropType.FOSSIL_ROCK,
                puzzleType = PuzzleType.GEOGRAPHY_QUIZ,
                puzzlePayload = PuzzlePayload.InteractiveChallenge(
                    question = "حفره‌ها و مجسمه‌های طبیعی دیواره‌های تنگه چاهکوه و دره ستارگان چگونه پدید آمده‌اند؟",
                    visualCueTitle = "نمودار فرسایش ژئوپارک قشم",
                    options = listOf(
                        ChallengeOption("q1", "انحلال و فرسایش میلیون‌ساله آب باران، باد و شوری در سنگ‌های رسوبی", true, "آفرین! اختلاف سختی لایه‌های سنگ در برابر آب و باد، حفره‌ها و ستون‌های زیبا را تراشیده است."),
                        ChallengeOption("q2", "برخورد شهاب‌سنگ‌های غول‌پیکر در هفته گذشته", false, "برخورد شهاب‌سنگ در دره ستارگان افسانه‌ای محلی است؛ علم زمین‌شناسی فرسایش تدریجی را نشان می‌دهد."),
                        ChallengeOption("q3", "تراشکاری با ابزار برقی مدرن", false, "این پدیده کاملاً طبیعی و میراث زمین‌شناسی یونسکو است."),
                    ),
                ),
                introDialogues = listOf(
                    DialogueLine(
                        id = "qsh_m1_1",
                        speakerName = "کاپیتان رهام",
                        isAria = false,
                        voiceProfile = VoiceProfile.ADULT_MALE,
                        ariaEmotion = AriaAnimationState.THINK,
                        text = "مردم قدیم می‌گفتند ستاره‌ای به زمین افتاده و دره را ساخته، اما علم زمین‌شناسی راز دقیق‌تری دارد.",
                    ),
                ),
                successMessage = "راز زمین‌شناختی ژئوپارک جهانی قشم را با موفقیت کشف کردی!",
                clueRewardTitle = "منشور ژئوپارک یونسکو",
            ),
            CountyMission(
                id = "qsh_m2",
                orderIndex = 2,
                title = "چاه‌های تلا در روستای لافت",
                shortGoal = "چیدن مراحل نگهداری آب در چاه‌های تاریخی لافت",
                worldX = 1660f,
                propType = WorldPropType.WINDTOWER_MODEL,
                puzzleType = PuzzleType.SEQUENCE_ORDER,
                puzzlePayload = PuzzlePayload.SequenceItems(
                    correctOrder = listOf(
                        "بارش باران فصلی روی تپه‌های مرجانی-آهکی لافت",
                        "هدایت جریان آب به حوضچه و چاه‌های تلا",
                        "جلوگیری لایه گچی کف چاه از نفوذ آب به لایه شور زیرین",
                        "استفاده منظم اهالی در طول سال",
                    ),
                    promptNote = "نحوه ذخیره هوشمند آب شیرین در بندر تاریخی لافت را مرتب کن.",
                ),
                introDialogues = listOf(
                    DialogueLine(
                        id = "qsh_m2_1",
                        speakerName = "آریا",
                        isAria = true,
                        voiceProfile = VoiceProfile.ARIA,
                        ariaEmotion = AriaAnimationState.OPEN_NOTEBOOK,
                        text = "اینجا در بندر لافت، در دل جزیره‌ای میان آب شور دریا، چاه‌های آب شیرین تلا قرار دارند!",
                    ),
                ),
                successMessage = "هوشمندی نیاکان در نگهداری آب شیرین جزیره قشم آشکار شد!",
                clueRewardTitle = "نشان بادگیر لافت",
            ),
            CountyMission(
                id = "qsh_m3",
                orderIndex = 3,
                title = "بلور غار نمکدان",
                shortGoal = "کشف بلور نمک طبیعی در نزدیکی قندیل‌های نمکی",
                worldX = 2140f,
                propType = WorldPropType.SALT_CRYSTAL_SHRINE,
                puzzleType = PuzzleType.HIDDEN_OBJECT,
                puzzlePayload = PuzzlePayload.HiddenSpots(
                    targetItemName = "بلور شفاف نمک (هالیت) نشانه‌دار",
                    spots = listOf(
                        SceneSpot("qs1", "توده قندیل‌های نمکی سفید", 0.70f, 0.42f, true, "یافت شد! بلور مکعبی شفاف نمک با نور چراغ‌قوه می‌درخشد."),
                        SceneSpot("qs2", "ساحل سنگی کنار غار", 0.24f, 0.68f, false, "ماسه و صدف‌های خردشده کنار موج دریا."),
                        SceneSpot("qs3", "تابلوی ورودی ژئوپارک", 0.46f, 0.30f, false, "نقشه راهنمای مسیر گردشگران در غار نمکدان."),
                    ),
                ),
                introDialogues = listOf(
                    DialogueLine(
                        id = "qsh_m3_1",
                        speakerName = "کاپیتان رهام",
                        isAria = false,
                        voiceProfile = VoiceProfile.ADULT_MALE,
                        ariaEmotion = AriaAnimationState.TALK,
                        text = "غار نمکدان قشم به عنوان طولانی‌ترین غار نمکی شناخته‌شده جهان، بلورهای منظم و شگفت‌انگیزی دارد.",
                    ),
                ),
                successMessage = "بلور شفاف پیدا شد و قطعه هفتم گنج به دفترچه پیوست!",
                clueRewardTitle = "بلور نمکدان قشم",
            ),
        ),
        treasurePiece = TreasurePiece(
            index = 7,
            title = "تندیس زمان در سنگ و نمک",
            pillarTheme = "شناخت زمین‌شناسی",
            shortMeaning = "ژئوپارک‌ها و ژئوتوریسم مسئولانه بدون تخریب پدیده‌های طبیعی",
            encyclopediaDetail = "ژئوپارک جهانی قشم نشان می‌دهد که حفاظت از میراث زمین‌شناختی (دره ستارگان، تنگه چاهکوه و غار نمکدان) در کنار گردشگری علمی پایدار می‌تواند به اقتصاد جامعه محلی کمک کند.",
        ),
        scientificBrief = ScientificBrief(
            landmarkTitle = "ژئوپارک جهانی قشم، دره ستارگان، تنگه چاهکوه و غار نمکدان",
            geographySummary = "قشم بزرگ‌ترین جزیره خلیج فارس است که در تنگه هرمز قرار دارد و جاذبه‌های زمین‌شناختی، دریایی و تاریخی کم‌نظیری را در خود جای داده است.",
            ecologyAndScience = "ژئوپارک قشم نخستین و تنها ژئوپارک ثبت‌شده یونسکو در خاورمیانه است. دره ستارگان، تنگه چاهکوه، جزایر ناز و غار نمکدان (از طولانی‌ترین غارهای نمکی جهان) در آن قرار دارند.",
            culturalHeritage = "بافت تاریخی بندر لافت با بادگیرهای زیبا و چاه‌های آب شیرین تلا، غارهای دست‌کند خربس و قلعه پرتغالی‌ها از آثار شاخص جزیره هستند.",
            sourceNote = "مبتنی بر داده‌های ژئوپارک جهانی قشم، یونسکو و میراث فرهنگی (شخصیت کاپیتان رهام تخیلی است).",
        ),
    ),

    // ۸. بندر خمیر
    CountyDefinition(
        id = "khamir",
        order = 8,
        nameFa = "بندر خمیر",
        stageTitle = "رقص جذر و مد در خورخوران",
        subtitle = "بزرگ‌ترین تالاب حرای ایران، دلفین‌ها و چشمه‌های آب‌گرم",
        biome = BiomeType.COASTAL_MANGROVE,
        mapX = 0.42f,
        mapY = 0.46f,
        unlocksNextIds = listOf("bastak", "bandar_lengeh"),
        npcName = "خانم ساحل",
        npcRole = "زیست‌شناس تالاب و حافظ پرندگان مهاجر (شخصیت تخیلی)",
        npcVoice = VoiceProfile.ADULT_FEMALE,
        npcAppearance = NpcAppearance(
            primaryColorHex = 0xFF2D6A4F,
            accentColorHex = 0xFF22B8CF,
            skinToneHex = 0xFFE5B887,
            headwear = HeadwearStyle.TRADITIONAL_SHAWL,
            hasBeard = false,
            isFemale = true,
        ),
        npcWorldX = 640f,
        openingDialogues = listOf(
            DialogueLine(
                id = "khm_open_1",
                speakerName = "خانم ساحل",
                isAria = false,
                voiceProfile = VoiceProfile.ADULT_FEMALE,
                ariaEmotion = AriaAnimationState.HAPPY,
                text = "سلام آریا! من ساحل هستم. به تالاب بین‌المللی خورخوران در بندرخمیر، بزرگ‌ترین جنگل دریایی حرای ایران خوش آمدی.",
            ),
            DialogueLine(
                id = "khm_open_2",
                speakerName = "آریا",
                isAria = true,
                voiceProfile = VoiceProfile.ARIA,
                ariaEmotion = AriaAnimationState.SURPRISED,
                text = "چطور درختان حرا می‌توانند آب شور خلیج فارس را بنوشند و سبز بمانند؟!",
            ),
        ),
        farewellDialogues = listOf(
            DialogueLine(
                id = "khm_bye_1",
                speakerName = "خانم ساحل",
                isAria = false,
                voiceProfile = VoiceProfile.ADULT_FEMALE,
                ariaEmotion = AriaAnimationState.RECEIVE_REWARD,
                text = "عالی بود آریا! دانش پالایش زیستی درختان حرا، الهام‌بخش علم امروز است. قطعه هشتم گنج از آنِ توست!",
            ),
        ),
        missions = listOf(
            CountyMission(
                id = "khm_m1",
                orderIndex = 1,
                title = "راز زیست‌شناسی درخت حرا",
                shortGoal = "شناخت دستگاه تصفیه طبیعی ریشه و برگ مانگرو",
                worldX = 1120f,
                propType = WorldPropType.MANGROVE_SAPLING,
                puzzleType = PuzzleType.GEOGRAPHY_QUIZ,
                puzzlePayload = PuzzlePayload.InteractiveChallenge(
                    question = "درختان حرا (مانگرو) در تالاب خورخوران چگونه در آب شور دریا زنده می‌مانند؟",
                    visualCueTitle = "آزمایش ریشه و برگ حرا",
                    options = listOf(
                        ChallengeOption("k1", "ریشه‌ها بخش عمده نمک را فیلتر می‌کنند و ریشه‌های هوایی در جذر نفس می‌کشند", true, "آفرین! حرا مانند یک دستگاه شیرین‌کن طبیعی و زیست‌شناختی عمل می‌کند."),
                        ChallengeOption("k2", "فقط باران‌های برفی زمستان را می‌نوشند", false, "جنگل حرا مستقیماً درون آب شور جزر و مدی خلیج فارس رشد می‌کند."),
                        ChallengeOption("k3", "چوب آن‌ها از پلاستیک ساخته شده است", false, "حرا گیاهی زنده و سرشار از کلروفیل است."),
                    ),
                ),
                introDialogues = listOf(
                    DialogueLine(
                        id = "khm_m1_1",
                        speakerName = "خانم ساحل",
                        isAria = false,
                        voiceProfile = VoiceProfile.ADULT_FEMALE,
                        ariaEmotion = AriaAnimationState.THINK,
                        text = "اگر با ذره‌بین به پشت برگ حرا و ریشه‌های عمودی بیرون‌آمده از گل نگاه کنی، جواب معما را می‌بینی.",
                    ),
                ),
                successMessage = "شگفتی تصفیه آب در ریشه‌های حرا را به درستی درک کردی!",
                clueRewardTitle = "برگ تصفیه‌گر حرا",
            ),
            CountyMission(
                id = "khm_m2",
                orderIndex = 2,
                title = "توازن اکوسیستم خورخوران",
                shortGoal = "تنظیم شرایط سلامت تالاب برای پرندگان و دلفین‌ها",
                worldX = 1650f,
                propType = WorldPropType.BOAT_DOCK,
                puzzleType = PuzzleType.ENVIRONMENT_INTERACTION,
                puzzlePayload = PuzzlePayload.MechanismBalance(
                    elements = listOf(
                        MechanismElement("me1", "فاصله قایق گردشگری از لانه پرنده", 3, "حریم"),
                        MechanismElement("me2", "کاشت نهال جدید حرا", 2, "دسته"),
                        MechanismElement("me3", "آلودگی صوتی موتور قایق", 1, "کمترین"),
                    ),
                    targetHint = "حریم پرنده روی ۳، کاشت نهال روی ۲ و صدای قایق روی کمترین (۱) تنظیم شود.",
                ),
                introDialogues = listOf(
                    DialogueLine(
                        id = "khm_m2_1",
                        speakerName = "آریا",
                        isAria = true,
                        voiceProfile = VoiceProfile.ARIA,
                        ariaEmotion = AriaAnimationState.THINK,
                        text = "برای اینکه پرنده‌های مهاجر و دلفین‌ها همیشه به خورخوران برگردند، باید توازن گردشگری را درست تنظیم کنیم.",
                    ),
                ),
                successMessage = "تالاب خورخوران با آرامش و سرسبزی می‌درخشد!",
                clueRewardTitle = "نشان پرنده مهاجر",
            ),
            CountyMission(
                id = "khm_m3",
                orderIndex = 3,
                title = "پیدا کردن بذر جوانه‌زده حرا",
                shortGoal = "یافتن نهال آماده کاشت در حاشیه جزر",
                worldX = 2120f,
                propType = WorldPropType.ROCK_CARVING_ALCOVE,
                puzzleType = PuzzleType.HIDDEN_OBJECT,
                puzzlePayload = PuzzlePayload.HiddenSpots(
                    targetItemName = "نهال جوانه‌زده حرا با برچسب پدربزرگ",
                    spots = listOf(
                        SceneSpot("ks1", "حاشیه گل‌ولای ریشه‌های حرا", 0.32f, 0.64f, true, "پیدا شد! نهال حرا همین‌جا ریشه زده تا جنگل سبزتر شود."),
                        SceneSpot("ks2", "صخره قلعه برکه لاری", 0.72f, 0.34f, false, "دیواره تاریخی قلعه بندرخمیر با معماری آب‌انبارهای کهن."),
                        SceneSpot("ks3", "سطح آب آرام خور", 0.54f, 0.74f, false, "انعکاس نور خورشید روی آب زلال تالاب."),
                    ),
                ),
                introDialogues = listOf(
                    DialogueLine(
                        id = "khm_m3_1",
                        speakerName = "خانم ساحل",
                        isAria = false,
                        voiceProfile = VoiceProfile.ADULT_FEMALE,
                        ariaEmotion = AriaAnimationState.TALK,
                        text = "بذر حرا روی خود درخت جوانه می‌زند و بعد در گل‌ولای ساحل می‌نشیند. یکی را پیدا کن تا نشانه‌گذاری کنیم.",
                    ),
                ),
                successMessage = "نهال حرا کاشته شد و قطعه هشتم گنج به دفترچه اضافه شد!",
                clueRewardTitle = "نهال ثبت‌شده خورخوران",
            ),
        ),
        treasurePiece = TreasurePiece(
            index = 8,
            title = "ریشه‌های تنفس‌کننده در آب شور",
            pillarTheme = "حفاظت از دریا",
            shortMeaning = "جذب کربن آبی، تصفیه طبیعی و حفاظت از آب‌های خلیج فارس",
            encyclopediaDetail = "جنگل‌های حرای خورخوران در میان قشم و بندرخمیر از مهم‌ترین جذب‌کننده‌های طبیعی کربن در جهان هستند و با پاکسازی آب دریا، زیستگاه دلفین‌ها و پرندگان مهاجر را زنده نگه می‌دارند.",
        ),
        scientificBrief = ScientificBrief(
            landmarkTitle = "تالاب بین‌المللی خورخوران و قلعه تاریخی بندرخمیر",
            geographySummary = "شهرستان بندرخمیر در غرب بندرعباس و روبه‌روی جزیره قشم قرار دارد و بزرگ‌ترین گستره جنگل‌های مانگرو (حرا) ایران را در تالاب خورخوران در بر می‌گیرد.",
            ecologyAndScience = "تالاب خورخوران در فهرست ذخیره‌گاه‌های زیست‌کره و کنوانسیون رامسر/یونسکو شناخته می‌شود و پناهگاه پرندگان مهاجر و دلفین‌هاست. چشمه‌های آب‌گرم معدنی نیز در این شهرستان وجود دارند.",
            culturalHeritage = "قلعه تاریخی بندرخمیر (برکه لاری) و آب‌انبارهای سنتی با معماری بومی از آثار تاریخی این شهرستان‌اند.",
            sourceNote = "مبتنی بر منابع سازمان حفاظت محیط‌زیست و یونسکو (شخصیت خانم ساحل تخیلی است).",
        ),
    ),

    // ۹. حاجی‌آباد
    CountyDefinition(
        id = "hajiabad",
        order = 9,
        nameFa = "حاجی‌آباد",
        stageTitle = "آبشار در دل نخلستان",
        subtitle = "آبشار تزرج، چشمه‌های سبزپوشان و آسیاب‌های آبی قدیمی",
        biome = BiomeType.WATERFALL_VALLEY,
        mapX = 0.53f,
        mapY = 0.17f,
        unlocksNextIds = listOf("bastak"),
        npcName = "عمو صابر",
        npcRole = "نگهدارنده آسیاب آبی قدیمی تزرج (شخصیت تخیلی)",
        npcVoice = VoiceProfile.ELDER_MALE,
        npcAppearance = NpcAppearance(
            primaryColorHex = 0xFF6D4C41,
            accentColorHex = 0xFF52B788,
            skinToneHex = 0xFFD99B66,
            headwear = HeadwearStyle.SOUTHERN_CAP,
            hasBeard = true,
            isFemale = false,
        ),
        npcWorldX = 660f,
        openingDialogues = listOf(
            DialogueLine(
                id = "haj_open_1",
                speakerName = "عمو صابر",
                isAria = false,
                voiceProfile = VoiceProfile.ELDER_MALE,
                ariaEmotion = AriaAnimationState.HAPPY,
                text = "خوش آمدی پسرم! به شمالی‌ترین دروازه هرمزگان، حاجی‌آباد رسیدی. صدای آبشار تزرج در میان نخلستان‌ها شنیدنی است.",
            ),
            DialogueLine(
                id = "haj_open_2",
                speakerName = "آریا",
                isAria = true,
                voiceProfile = VoiceProfile.ARIA,
                ariaEmotion = AriaAnimationState.LOOK_AROUND,
                text = "چه منظره شگفت‌انگیزی عمو صابر! یک آبشار خنک درست کنار نخل‌های گرمسیری و یک آسیاب آبی قدیمی!",
            ),
        ),
        farewellDialogues = listOf(
            DialogueLine(
                id = "haj_bye_1",
                speakerName = "عمو صابر",
                isAria = false,
                voiceProfile = VoiceProfile.ELDER_MALE,
                ariaEmotion = AriaAnimationState.RECEIVE_REWARD,
                text = "آفرین آریا! با انرژی پاک آب، چرخ آسیاب و امید روستا را به گردش درآوردی. قطعه نهم گنج مبارک تو باشد.",
            ),
        ),
        missions = listOf(
            CountyMission(
                id = "haj_m1",
                orderIndex = 1,
                title = "راه‌اندازی چرخ آسیاب آبی",
                shortGoal = "تنظیم دریچه و دبی آب برای چرخش بدون آسیب پره‌ها",
                worldX = 1130f,
                propType = WorldPropType.WATERMILL_WHEEL,
                puzzleType = PuzzleType.ENVIRONMENT_INTERACTION,
                puzzlePayload = PuzzlePayload.MechanismBalance(
                    elements = listOf(
                        MechanismElement("wm1", "دریچه تنوره آب", 2, "سطح"),
                        MechanismElement("wm2", "زاویه پره‌های چوبی", 3, "حالت"),
                        MechanismElement("wm3", "کانال بازگشت آب به نخلستان", 2, "مسیر"),
                    ),
                    targetHint = "دریچه روی ۲، زاویه پره روی ۳ و مسیر بازگشت آب به نخلستان روی ۲ تنظیم شود.",
                ),
                introDialogues = listOf(
                    DialogueLine(
                        id = "haj_m1_1",
                        speakerName = "عمو صابر",
                        isAria = false,
                        voiceProfile = VoiceProfile.ELDER_MALE,
                        ariaEmotion = AriaAnimationState.THINK,
                        text = "آسیاب آبی قدیمی نه دود دارد و نه سوخت فسیلی مصرف می‌کند؛ فقط با نیروی آب جاری می‌چرخد و بعد آب دوباره به نخل‌ها می‌رسد.",
                    ),
                ),
                successMessage = "چرخ آسیاب با نیروی پاک آب به حرکت درآمد!",
                clueRewardTitle = "پره‌ی انرژی پاک",
            ),
            CountyMission(
                id = "haj_m2",
                orderIndex = 2,
                title = "چرا آبشار در دل نخلستان جاری است؟",
                shortGoal = "درک نقش چشمه‌های کوهستانی در ایجاد واحه‌های سرسبز",
                worldX = 1660f,
                propType = WorldPropType.WATER_CHANNEL_STONE,
                puzzleType = PuzzleType.SCIENCE_WATER_SLOPE,
                puzzlePayload = PuzzlePayload.WaterSlope(
                    channelSegments = listOf(
                        SlopeSegment("h_low", "نخلستان دشت تزرج", 690),
                        SlopeSegment("h_top", "ارتفاعات و چشمه کوه بابا حاجی", 1450),
                        SlopeSegment("h_mid2", "تاج آبشار تزرج", 920),
                        SlopeSegment("h_mid1", "دره سنگی بالایی", 1180),
                    ),
                    explanation = "چشمه‌های کوهستانی از شکستگی‌های زمین در ارتفاعات سرچشمه می‌گیرند و هنگام سرازیر شدن به دشت، آبشار تزرج و نخلستان‌های سرسبز را پدید می‌آورند.",
                ),
                introDialogues = listOf(
                    DialogueLine(
                        id = "haj_m2_1",
                        speakerName = "آریا",
                        isAria = true,
                        voiceProfile = VoiceProfile.ARIA,
                        ariaEmotion = AriaAnimationState.THINK,
                        text = "بیایید مسیر آب را از کوهستان شمالی تا پای نخلستان تزرج بر اساس ارتفاع طبیعی مرتب کنیم.",
                    ),
                ),
                successMessage = "راز جغرافیایی واحه تزرج را به درستی کشف کردی!",
                clueRewardTitle = "نقشه چشمه تزرج",
            ),
            CountyMission(
                id = "haj_m3",
                orderIndex = 3,
                title = "سنگ‌آسیاب مینیاتوری یادگار",
                shortGoal = "پیدا کردن نشانه تاریخی در کنار قلعه حاجی‌آباد",
                worldX = 2120f,
                propType = WorldPropType.ROCK_CARVING_ALCOVE,
                puzzleType = PuzzleType.HIDDEN_OBJECT,
                puzzlePayload = PuzzlePayload.HiddenSpots(
                    targetItemName = "سنگ‌آسیاب کوچک حکاکی‌شده",
                    spots = listOf(
                        SceneSpot("hs1", "پایه خشتی آسیاب قدیمی", 0.65f, 0.58f, true, "اینجاست! یک سنگ‌آسیاب کوچک حکاکی‌شده با نشان شماره ۹!"),
                        SceneSpot("hs2", "شاخه‌های سبز نخل", 0.28f, 0.28f, false, "خرمای سبز در سایه شاخه‌های بلند نخل."),
                        SceneSpot("hs3", "حوضچه زیر آبشار", 0.44f, 0.72f, false, "حباب‌های روشن و صدای دلنشین ریزش آب."),
                    ),
                ),
                introDialogues = listOf(
                    DialogueLine(
                        id = "haj_m3_1",
                        speakerName = "عمو صابر",
                        isAria = false,
                        voiceProfile = VoiceProfile.ELDER_MALE,
                        ariaEmotion = AriaAnimationState.TALK,
                        text = "پدربزرگت کنار همین آسیاب و قلعه کهن، یک سنگ یادگار کوچک پنهان کرده بود.",
                    ),
                ),
                successMessage = "قطعه نهم گنج در دفترچه اسرار ثبت شد!",
                clueRewardTitle = "سنگ‌آسیاب حاجی‌آباد",
            ),
        ),
        treasurePiece = TreasurePiece(
            index = 9,
            title = "انرژی پاک آب در قلب نخل‌ها",
            pillarTheme = "انرژی پاک",
            shortMeaning = "الهام از آسیاب‌های آبی سنتی برای تولید انرژی تجدیدپذیر",
            encyclopediaDetail = "آسیاب‌های آبی کهن حاجی‌آباد گواهی هستند بر اینکه می‌توان از نیروی جریان آب و خورشید تابان جنوب، انرژی پاک و بدون آلایندگی برای نیازهای روزمره تولید کرد.",
        ),
        scientificBrief = ScientificBrief(
            landmarkTitle = "آبشار تزرج، چشمه‌های سبزپوشان و بالنگان و آسیاب‌های آبی",
            geographySummary = "شهرستان حاجی‌آباد در شمال استان هرمزگان و در همسایگی ارتفاعات زاگرس جنوبی قرار گرفته و آب‌وهوای معتدل‌تری نسبت به نوار ساحلی دارد.",
            ecologyAndScience = "آبشار تزرج در میان نخلستان‌های گرمسیری جلوه‌ای کم‌نظیر از تضاد آب‌وهوایی می‌سازد. چشمه‌های سبزپوشان و بالنگان نیز از منابع آبی ارزشمند این منطقه‌اند.",
            culturalHeritage = "قلعه کهن حاجی‌آباد و بقایای آسیاب‌های آبی قدیمی از آثار تاریخی قابل توجه شهرستان هستند.",
            sourceNote = "مبتنی بر داده‌های جغرافیایی و گردشگری هرمزگان (شخصیت عمو صابر تخیلی است).",
        ),
    ),

    // ۱۰. بستک
    CountyDefinition(
        id = "bastak",
        order = 10,
        nameFa = "بستک",
        stageTitle = "نسیم خنک بادگیرها",
        subtitle = "بادگیرهای تاریخی، کاروانسرای قاجاری و منطقه حفاظت‌شده گوده",
        biome = BiomeType.HISTORIC_WINDTOWER,
        mapX = 0.25f,
        mapY = 0.34f,
        unlocksNextIds = listOf("bandar_lengeh", "parsian"),
        npcName = "استاد فرهاد",
        npcRole = "معمار سنتی و مرمت‌کار بادگیرهای بستک (شخصیت تخیلی)",
        npcVoice = VoiceProfile.ADULT_MALE,
        npcAppearance = NpcAppearance(
            primaryColorHex = 0xFFC85A32,
            accentColorHex = 0xFFFFF8E8,
            skinToneHex = 0xFFE0AC69,
            headwear = HeadwearStyle.NONE,
            hasBeard = true,
            isFemale = false,
        ),
        npcWorldX = 650f,
        openingDialogues = listOf(
            DialogueLine(
                id = "bas_open_1",
                speakerName = "استاد فرهاد",
                isAria = false,
                voiceProfile = VoiceProfile.ADULT_MALE,
                ariaEmotion = AriaAnimationState.HAPPY,
                text = "سلام آریا! به بستک، سرزمین بادگیرهای اصیل جنوب خوش آمدی. من فرهاد هستم و روی مرمت کاروانسرا و خانه‌های تاریخی کار می‌کنم.",
            ),
            DialogueLine(
                id = "bas_open_2",
                speakerName = "آریا",
                isAria = true,
                voiceProfile = VoiceProfile.ARIA,
                ariaEmotion = AriaAnimationState.SURPRISED,
                text = "با اینکه هوا بیرون گرم است، داخل این خانه قدیمی کاملاً خنک است! رازش همین بادگیرهاست؟",
            ),
        ),
        farewellDialogues = listOf(
            DialogueLine(
                id = "bas_bye_1",
                speakerName = "استاد فرهاد",
                isAria = false,
                voiceProfile = VoiceProfile.ADULT_MALE,
                ariaEmotion = AriaAnimationState.RECEIVE_REWARD,
                text = "تو علم معماری هم‌ساز با اقلیم را عالی فهمیدی آریا! قطعه دهم گنج یادگار بادگیرهای بستک است.",
            ),
        ),
        missions = listOf(
            CountyMission(
                id = "bas_m1",
                orderIndex = 1,
                title = "فیزیک ساده بادگیر",
                shortGoal = "فهم نحوه خنک‌سازی طبیعی ساختمان بدون مصرف برق",
                worldX = 1130f,
                propType = WorldPropType.WINDTOWER_MODEL,
                puzzleType = PuzzleType.GEOGRAPHY_QUIZ,
                puzzlePayload = PuzzlePayload.InteractiveChallenge(
                    question = "بادگیرهای مشهور بستک چگونه خانه را در روزهای گرم خنک می‌کنند؟",
                    visualCueTitle = "جریان همرفتی در بادگیر",
                    options = listOf(
                        ChallengeOption("ba1", "باد خنک ارتفاع بالا را به داخل هدایت کرده و هوای گرم داخل را از طرف دیگر بیرون می‌رانند", true, "کاملاً درست! اختلاف فشار و جابه‌جایی هوا (همرفت طبیعی) خانه را بدون هیچ برقی خنک می‌کند."),
                        ChallengeOption("ba2", "داخل برج قطعات بزرگ یخ پنهان می‌کنند", false, "بادگیرها فقط با استفاده از فیزیک جریان هوا و سایه عمل می‌کنند."),
                        ChallengeOption("ba3", "سوراخ‌ها صرفاً برای تزیین بدون عبور هوا هستند", false, "دریچه‌های بادگیر کاملاً برای هدایت جریان باد طراحی شده‌اند."),
                    ),
                ),
                introDialogues = listOf(
                    DialogueLine(
                        id = "bas_m1_1",
                        speakerName = "استاد فرهاد",
                        isAria = false,
                        voiceProfile = VoiceProfile.ADULT_MALE,
                        ariaEmotion = AriaAnimationState.THINK,
                        text = "نسیم در ارتفاع چند متری خنک‌تر و تمیزتر از سطح کوچه است. بادگیر دقیقاً همین نسیم را شکار می‌کند!",
                    ),
                ),
                successMessage = "اصول فیزیکی خنک‌سازی طبیعی در بادگیرها را کشف کردی!",
                clueRewardTitle = "طرح جریان بادگیر",
            ),
            CountyMission(
                id = "bas_m2",
                orderIndex = 2,
                title = "تزئینات گچ‌بری و مشبک",
                shortGoal = "بازسازی الگوی هندسی پنجره مشبک سنتی",
                worldX = 1650f,
                propType = WorldPropType.WEAVING_LOOM,
                puzzleType = PuzzleType.GEOMETRIC_PATTERN,
                puzzlePayload = PuzzlePayload.PatternMatrix(
                    size = 3,
                    targetPattern = listOf(
                        false, true, false,
                        true, true, true,
                        false, true, false,
                    ),
                    initialPattern = listOf(
                        false, true, false,
                        false, true, false,
                        false, false, false,
                    ),
                    motifName = "پنجره مشبک چلیپای نورگیر",
                ),
                introDialogues = listOf(
                    DialogueLine(
                        id = "bas_m2_1",
                        speakerName = "آریا",
                        isAria = true,
                        voiceProfile = VoiceProfile.ARIA,
                        ariaEmotion = AriaAnimationState.THINK,
                        text = "این پنجره‌های مشبک نور شدید آفتاب را می‌شکنند و سایه‌روشن زیبایی می‌سازند. باید الگوی متقارنش را کامل کنم.",
                    ),
                ),
                successMessage = "پنجره مشبک بستکی کامل شد و نسیم خنکی وزید!",
                clueRewardTitle = "الگوی مشبک کاروانسرا",
            ),
            CountyMission(
                id = "bas_m3",
                orderIndex = 3,
                title = "حفاظت از منطقه گوده",
                shortGoal = "شناخت زیستگاه طبیعی اطراف بستک",
                worldX = 2120f,
                propType = WorldPropType.MOUNTAIN_SIGNPOST,
                puzzleType = PuzzleType.ECO_BALANCE,
                puzzlePayload = PuzzlePayload.InteractiveChallenge(
                    question = "منطقه حفاظت‌شده گوده در شهرستان بستک چه نقشی در استان هرمزگان دارد؟",
                    visualCueTitle = "تابلوی منطقه گوده",
                    options = listOf(
                        ChallengeOption("bg1", "حفظ حیات‌وحش بومی کوهستانی و پوشش گیاهی زاگرس جنوبی", true, "بسیار درست! مناطق حفاظت‌شده از زیستگاه گونه‌های کمیاب دفاع می‌کنند."),
                        ChallengeOption("bg2", "بزرگ‌ترین بندر کشتی‌های اقیانوس‌پیما", false, "بستک شهرستانی درونی-کوهستانی است، نه بندر ساحلی."),
                        ChallengeOption("bg3", "محل دفن زباله‌های صنعتی", false, "منطقه حفاظت‌شده برای صیانت از طبیعت ایجاد می‌شود."),
                    ),
                ),
                introDialogues = listOf(
                    DialogueLine(
                        id = "bas_m3_1",
                        speakerName = "استاد فرهاد",
                        isAria = false,
                        voiceProfile = VoiceProfile.ADULT_MALE,
                        ariaEmotion = AriaAnimationState.TALK,
                        text = "معماری بستک فقط به خانه محدود نیست؛ احترام به کوهستان و منطقه گوده بخش مهمی از فرهنگ ماست.",
                    ),
                ),
                successMessage = "قطعه دهم گنج در دفترچه پدربزرگ جای گرفت!",
                clueRewardTitle = "نشان بادگیر بستک",
            ),
        ),
        treasurePiece = TreasurePiece(
            index = 10,
            title = "معماری هم‌ساز با اقلیم و خورشید",
            pillarTheme = "معماری سازگار با اقلیم",
            shortMeaning = "کاهش مصرف انرژی در ساختمان‌ها با الهام از بادگیرها و سایه‌اندازی بومی",
            encyclopediaDetail = "بادگیرهای بستک و الگوی معماری جنوب ایران نشان می‌دهند که طراحی درست جهت باد، دیوارهای ضخیم با مصالح بومی و پنجره‌های مشبک می‌تواند نیاز به سرمایش پرمصرف را به شدت کاهش دهد.",
        ),
        scientificBrief = ScientificBrief(
            landmarkTitle = "بادگیرهای بستک، کاروانسرای قاجاری و منطقه گوده",
            geographySummary = "شهرستان بستک در غرب استان هرمزگان و در میان رشته‌کوه‌های زاگرس جنوبی واقع شده است.",
            ecologyAndScience = "بادگیرهای بستک از الگوهای برجسته معماری بومی خلیج فارس‌اند که با بهره‌گیری از جریان همرفت هوا به تهویه طبیعی فضا کمک می‌کنند. منطقه حفاظت‌شده گوده نیز زیستگاه ارزشمند حیات‌وحش بومی است.",
            culturalHeritage = "مجموعه تاریخی بستک شامل خانه بنی‌عباسی، حمام‌های قدیمی، بازار و کاروانسرای دوره قاجار است.",
            sourceNote = "مبتنی بر منابع میراث فرهنگی و معماری جنوب ایران (شخصیت استاد فرهاد تخیلی است).",
        ),
    ),

    // ۱۱. بندرلنگه
    CountyDefinition(
        id = "bandar_lengeh",
        order = 11,
        nameFa = "بندر لنگه",
        stageTitle = "مروارید سواحل و خانه فکری",
        subtitle = "بافت تاریخی، خانه فکری، قلعه مغویه و ساحل مکسر",
        biome = BiomeType.HISTORIC_WINDTOWER,
        mapX = 0.28f,
        mapY = 0.57f,
        unlocksNextIds = listOf("parsian"),
        npcName = "بانو مهرنوش",
        npcRole = "راوی قصه‌های دریانوردان قدیم لنگه (شخصیت تخیلی)",
        npcVoice = VoiceProfile.ELDER_FEMALE,
        npcAppearance = NpcAppearance(
            primaryColorHex = 0xFF7C2D12,
            accentColorHex = 0xFFF6C453,
            skinToneHex = 0xFFE0AC69,
            headwear = HeadwearStyle.TRADITIONAL_SHAWL,
            hasBeard = false,
            isFemale = true,
        ),
        npcWorldX = 650f,
        openingDialogues = listOf(
            DialogueLine(
                id = "len_open_1",
                speakerName = "بانو مهرنوش",
                isAria = false,
                voiceProfile = VoiceProfile.ELDER_FEMALE,
                ariaEmotion = AriaAnimationState.HAPPY,
                text = "خوش آمدی آریا جان! به بندرلنگه، شهر خانه‌های فکری و مختاری و دریانوردان بااصالت خوش آمدی.",
            ),
            DialogueLine(
                id = "len_open_2",
                speakerName = "آریا",
                isAria = true,
                voiceProfile = VoiceProfile.ARIA,
                ariaEmotion = AriaAnimationState.LOOK_AROUND,
                text = "گچ‌بری‌های ظریف و پنجره‌های رنگی خانه فکری فوق‌العاده‌اند! اینجا بوی تاریخ دریانوردی می‌دهد.",
            ),
        ),
        farewellDialogues = listOf(
            DialogueLine(
                id = "len_bye_1",
                speakerName = "بانو مهرنوش",
                isAria = false,
                voiceProfile = VoiceProfile.ELDER_FEMALE,
                ariaEmotion = AriaAnimationState.RECEIVE_REWARD,
                text = "سفرت پر از آگاهی باشد پسرم! قطعه یازدهم گنج، یادگار هنر دریانوردی و معماری بندرلنگه است.",
            ),
        ),
        missions = listOf(
            CountyMission(
                id = "len_m1",
                orderIndex = 1,
                title = "مسیر لنج‌های بومی خلیج فارس",
                shortGoal = "مرتب کردن مراحل سفر ایمن دریایی سنتی",
                worldX = 1120f,
                propType = WorldPropType.BOAT_DOCK,
                puzzleType = PuzzleType.SEQUENCE_ORDER,
                puzzlePayload = PuzzlePayload.SequenceItems(
                    correctOrder = listOf(
                        "بررسی نقشه ستارگان، باد و جزر و مد",
                        "بارگیری متعادل کالا روی لنج چوبی",
                        "ناوبری از کنار جزایر و ساحل صخره‌ای",
                        "پهلوگیری امن در اسکله بندرلنگه",
                    ),
                    promptNote = "مراحل یک سفر موفق دریانوردان قدیمی بندرلنگه را به ترتیب بچین.",
                ),
                introDialogues = listOf(
                    DialogueLine(
                        id = "len_m1_1",
                        speakerName = "بانو مهرنوش",
                        isAria = false,
                        voiceProfile = VoiceProfile.ELDER_FEMALE,
                        ariaEmotion = AriaAnimationState.THINK,
                        text = "ناخداهای قدیم بندرلنگه با ستاره‌ها و جهت باد راه دریا را پیدا می‌کردند.",
                    ),
                ),
                successMessage = "نظم و دقت ناخداهای خلیج فارس را به درستی چیدی!",
                clueRewardTitle = "دفتر ناوبری لنج",
            ),
            CountyMission(
                id = "len_m2",
                orderIndex = 2,
                title = "جزئیات خانه فکری و آب‌انبار",
                shortGoal = "تشخیص مصالح سازگار با رطوبت ساحل",
                worldX = 1660f,
                propType = WorldPropType.WINDTOWER_MODEL,
                puzzleType = PuzzleType.OBSERVATION_DIFF,
                puzzlePayload = PuzzlePayload.InteractiveChallenge(
                    question = "در معماری خانه‌های تاریخی بندرلنگه مانند خانه فکری، برای مقابله با گرما و رطوبت ساحلی از چه تمهیدی استفاده می‌شد؟",
                    visualCueTitle = "نمای حیاط مرکزی خانه فکری",
                    options = listOf(
                        ChallengeOption("l1", "حیاط مرکزی، ایوان‌های بلند سایه‌دار و بادگیرهای چهارطرفه", true, "دقیقاً! ایوان و حیاط مرکزی جریان نسیم دریایی را در تمام اتاق‌ها می‌گرداند."),
                        ChallengeOption("l2", "دیوارهای آهنی تماماً بسته بدون پنجره", false, "آهن در رطوبت دریا زنگ می‌زند و نبود پنجره هوا را خفه می‌کند."),
                        ChallengeOption("l3", "سقف‌های شیروانی برف‌گیر", false, "در اقلیم گرمسیری جنوب بام‌های مسطح و گنبدی رایج‌اند."),
                    ),
                ),
                introDialogues = listOf(
                    DialogueLine(
                        id = "len_m2_1",
                        speakerName = "آریا",
                        isAria = true,
                        voiceProfile = VoiceProfile.ARIA,
                        ariaEmotion = AriaAnimationState.LOOK_AROUND,
                        text = "چقدر باد در حیاط خانه فکری خنک می‌چرخد! حتماً رازش در همین ایوان‌هاست.",
                    ),
                ),
                successMessage = "هنر معماری خانه فکری بندرلنگه را به خوبی شناختی!",
                clueRewardTitle = "نقشه خانه فکری",
            ),
            CountyMission(
                id = "len_m3",
                orderIndex = 3,
                title = "قطب‌نمای برنجی دریانورد",
                shortGoal = "کشف قطب‌نمای قدیمی در حیاط تاریخی",
                worldX = 2130f,
                propType = WorldPropType.ANCIENT_COMPASS,
                puzzleType = PuzzleType.HIDDEN_OBJECT,
                puzzlePayload = PuzzlePayload.HiddenSpots(
                    targetItemName = "قطب‌نمای برنجی حکاکی‌شده",
                    spots = listOf(
                        SceneSpot("ls1", "حاشیه حوضچه حیاط مرکزی", 0.56f, 0.65f, true, "اینجاست! قطب‌نمای برنجی پدربزرگ کنار حوضچه فیروزه‌ای پنهان شده بود."),
                        SceneSpot("ls2", "ستون گچ‌بری شده ایوان", 0.25f, 0.38f, false, "نقش‌های اسلیمی و گیاهی زیبا روی گچ‌بری سفید."),
                        SceneSpot("ls3", "چوب‌لباسی بادگیر", 0.78f, 0.44f, false, "طناب قدیمی دریچه تهویه؛ شیء گمشده اینجا نیست."),
                    ),
                ),
                introDialogues = listOf(
                    DialogueLine(
                        id = "len_m3_1",
                        speakerName = "بانو مهرنوش",
                        isAria = false,
                        voiceProfile = VoiceProfile.ELDER_FEMALE,
                        ariaEmotion = AriaAnimationState.TALK,
                        text = "یک یادگار برنجی در حیاط خانه فکری منتظر توست، دقیق نگاه کن!",
                    ),
                ),
                successMessage = "قطب‌نما پیدا شد و قطعه یازدهم گنج درخشید!",
                clueRewardTitle = "قطب‌نمای بندرلنگه",
            ),
        ),
        treasurePiece = TreasurePiece(
            index = 11,
            title = "هنر دریانوردی و تجارت پایدار",
            pillarTheme = "فرهنگ و میراث",
            shortMeaning = "حفظ میراث تاریخی بنادر در کنار توسعه دریانوردی پاک",
            encyclopediaDetail = "بندرلنگه با بافت تاریخی باشکوه و سابقه ساخت لنج‌های چوبی، نشان می‌دهد چگونه می‌توان هویت تاریخی بنادر ایرانی خلیج فارس را حفظ و به نسل‌های آینده منتقل کرد.",
        ),
        scientificBrief = ScientificBrief(
            landmarkTitle = "بافت تاریخی بندرلنگه، خانه فکری، قلعه مغویه و آب‌انبارها",
            geographySummary = "شهرستان بندرلنگه در بخش غربی سواحل خلیج فارس در استان هرمزگان واقع شده و جزایر و بنادر تاریخی متعددی را در بر می‌گیرد.",
            ecologyAndScience = "سواحل صخره‌ای و جزر و مدی و جزایر پیرامونی مانند لاوان از اکوسیستم‌های ارزشمند دریایی خلیج فارس هستند.",
            culturalHeritage = "خانه فکری، خانه مختاری، قلعه مغویه، آب‌انبارهای تاریخی (مانند آب‌انبار مفوض) و بادگیرهای جنوب از مهم‌ترین آثار ثبت‌شده تاریخی این شهرند.",
            sourceNote = "مبتنی بر منابع میراث فرهنگی و گردشگری هرمزگان (شخصیت بانو مهرنوش تخیلی است).",
        ),
    ),

    // ۱۲. پارسیان
    CountyDefinition(
        id = "parsian",
        order = 12,
        nameFa = "پارسیان",
        stageTitle = "تلاقی کوه و دریا در بوچیر",
        subtitle = "تنگه و چشمه بوچیر، ساحل مکسر و بستانو در غربی‌ترین کرانه",
        biome = BiomeType.COASTAL_MANGROVE,
        mapX = 0.10f,
        mapY = 0.44f,
        unlocksNextIds = listOf("abumusa"),
        npcName = "کیانوش",
        npcRole = "غارنورد و حافظ تنگه‌های کوهستانی پارسیان (شخصیت تخیلی)",
        npcVoice = VoiceProfile.YOUTH_MALE,
        npcAppearance = NpcAppearance(
            primaryColorHex = 0xFF0F4C5C,
            accentColorHex = 0xFF52B788,
            skinToneHex = 0xFFE5B887,
            headwear = HeadwearStyle.NONE,
            hasBeard = false,
            isFemale = false,
        ),
        npcWorldX = 660f,
        openingDialogues = listOf(
            DialogueLine(
                id = "par_open_1",
                speakerName = "کیانوش",
                isAria = false,
                voiceProfile = VoiceProfile.YOUTH_MALE,
                ariaEmotion = AriaAnimationState.HAPPY,
                text = "سلام آریا! من کیانوشم. به غربی‌ترین دروازه هرمزگان، شهرستان پارسیان خوش آمدی؛ جایی که کوهستان زاگرس درست تا لب آب‌های خلیج فارس می‌آید!",
            ),
            DialogueLine(
                id = "par_open_2",
                speakerName = "آریا",
                isAria = true,
                voiceProfile = VoiceProfile.ARIA,
                ariaEmotion = AriaAnimationState.SURPRISED,
                text = "تماشای صخره‌های ساحل مکسر و آب خنک تنگه بوچیر واقعاً شگفت‌انگیز است!",
            ),
        ),
        farewellDialogues = listOf(
            DialogueLine(
                id = "par_bye_1",
                speakerName = "کیانوش",
                isAria = false,
                voiceProfile = VoiceProfile.YOUTH_MALE,
                ariaEmotion = AriaAnimationState.RECEIVE_REWARD,
                text = "آفرین! فقط یک جزیره نیلگون باقی مانده تا سیزدهمین راز هم کامل شود. راهی ابوموسی شو آریا!",
            ),
        ),
        missions = listOf(
            CountyMission(
                id = "par_m1",
                orderIndex = 1,
                title = "عبور ایمن از تنگه آبی بوچیر",
                shortGoal = "پیدا کردن مسیر امن در میان دیواره تنگه و حوضچه‌های آب",
                worldX = 1130f,
                propType = WorldPropType.MOUNTAIN_SIGNPOST,
                puzzleType = PuzzleType.PATH_FINDING,
                puzzlePayload = PuzzlePayload.PathGrid(
                    rows = 4,
                    cols = 4,
                    cells = listOf(
                        2, 0, 0, 1,
                        1, 1, 0, 0,
                        0, 0, 1, 0,
                        0, 1, 0, 3,
                    ),
                    hintText = "از روی سنگ‌های ایمن تنگه عبور کن تا به چشمه خنک بوچیر برسی.",
                ),
                introDialogues = listOf(
                    DialogueLine(
                        id = "par_m1_1",
                        speakerName = "کیانوش",
                        isAria = false,
                        voiceProfile = VoiceProfile.YOUTH_MALE,
                        ariaEmotion = AriaAnimationState.THINK,
                        text = "در تنگه بوچیر باید مسیرهای کم‌لغزش و امن را انتخاب کنی تا به چشمه زلال برسی.",
                    ),
                ),
                successMessage = "با مهارت کامل از تنگه زیبای بوچیر عبور کردی!",
                clueRewardTitle = "مسیرنامه بوچیر",
            ),
            CountyMission(
                id = "par_m2",
                orderIndex = 2,
                title = "تلاقی کوه و دریا در ساحل مکسر",
                shortGoal = "تشخیص پدیده زمین‌ریخت‌شناسی ساحل مکسر و بستانو",
                worldX = 1660f,
                propType = WorldPropType.FOSSIL_ROCK,
                puzzleType = PuzzleType.IMAGE_MATCH,
                puzzlePayload = PuzzlePayload.InteractiveChallenge(
                    question = "صخره‌های منحصربه‌فرد ساحل مکسر در مسیر پارسیان-بستانو چگونه شکل گرفته‌اند؟",
                    visualCueTitle = "دیواره‌های موج‌تراش ساحل مکسر",
                    options = listOf(
                        ChallengeOption("p1", "برخورد پیوسته امواج خلیج فارس با صخره‌های ساحلی کوهستان", true, "درست است! امواج دریا با سایش تدریجی، حفره‌ها و قوس‌های طبیعی شگفت‌انگیزی در صخره ایجاد کرده‌اند."),
                        ChallengeOption("p2", "ساخته شده با گچ و گل رس مصنوعی", false, "این صخره‌ها کاملاً طبیعی و سخت‌سنگ هستند."),
                        ChallengeOption("p3", "ریزش باران اسیدی از آتشفشان فعال", false, "در این ساحل آتشفشان فعالی وجود ندارد."),
                    ),
                ),
                introDialogues = listOf(
                    DialogueLine(
                        id = "par_m2_1",
                        speakerName = "آریا",
                        isAria = true,
                        voiceProfile = VoiceProfile.ARIA,
                        ariaEmotion = AriaAnimationState.LOOK_AROUND,
                        text = "نگاه کن موج‌های دریا چطور سنگ‌های کنار ساحل را مثل مجسمه تراش داده‌اند!",
                    ),
                ),
                successMessage = "پدیده زیبای ساحل مکسر را به درستی شناختی!",
                clueRewardTitle = "سنگ موج‌تراش مکسر",
            ),
            CountyMission(
                id = "par_m3",
                orderIndex = 3,
                title = "حفظ پاکیزگی تنگه‌ها",
                shortGoal = "پاسخ به پرسش اکوتوریسم مسئولانه",
                worldX = 2130f,
                propType = WorldPropType.MANGROVE_SAPLING,
                puzzleType = PuzzleType.ECO_BALANCE,
                puzzlePayload = PuzzlePayload.InteractiveChallenge(
                    question = "اولین اصل طبیعت‌گردی مسئولانه در تنگه بوچیر و ساحل بستانو چیست؟",
                    visualCueTitle = "منشور طبیعت‌گردی",
                    options = listOf(
                        ChallengeOption("pe1", "به جا نگذاشتن هیچ‌گونه زباله و آسیب نزدن به صخره‌ها و پوشش گیاهی", true, "آفرین! در طبیعت فقط جای پا بگذاریم و فقط عکس با خود ببریم."),
                        ChallengeOption("pe2", "حک کردن اسم خود روی صخره‌های طبیعی", false, "نوشتن یا کندن روی صخره‌ها به زیبایی و اصالت طبیعت آسیب می‌زند."),
                        ChallengeOption("pe3", "ریختن شوینده شیمیایی در آب چشمه", false, "شوینده‌ها آب شیرین و آبزیان تنگه را مسموم می‌کنند."),
                    ),
                ),
                introDialogues = listOf(
                    DialogueLine(
                        id = "par_m3_1",
                        speakerName = "کیانوش",
                        isAria = false,
                        voiceProfile = VoiceProfile.YOUTH_MALE,
                        ariaEmotion = AriaAnimationState.TALK,
                        text = "زیبایی بوچیر و ساحل مکسر وقتی ماندگار می‌ماند که هر مسافری حافظ آن باشد.",
                    ),
                ),
                successMessage = "قطعه دوازدهم گنج آماده درخشش شد!",
                clueRewardTitle = "پیمان اکوتوریسم پارسیان",
            ),
        ),
        treasurePiece = TreasurePiece(
            index = 12,
            title = "هم‌زیستی صخره و موج",
            pillarTheme = "حفاظت از محیط‌زیست",
            shortMeaning = "گردشگری مسئولانه و صیانت از تنگه‌های آبی و سواحل صخره‌ای",
            encyclopediaDetail = "پارسیان نشان می‌دهد که کوهستان و دریا می‌توانند در یک نقطه به زیباترین شکل تلاقی پیدا کنند؛ حفظ این شگفتی‌ها نیازمند پرهیز از زباله‌ریزی و احترام به چرخه‌های طبیعی است.",
        ),
        scientificBrief = ScientificBrief(
            landmarkTitle = "تنگه و چشمه بوچیر، ساحل مکسر و روستای کوشکنار",
            geographySummary = "شهرستان پارسیان (گاوبندی سابق) غربی‌ترین شهرستان استان هرمزگان است که به علت مجاورت کوه و دریا، چشم‌اندازهای کوهستانی-ساحلی کم‌نظیری دارد.",
            ecologyAndScience = "دره و چشمه بوچیر با آب خنک در میان دیواره‌های سنگی و ساحل صخره‌ای مکسر و بستانو که توسط امواج تراش خورده‌اند، از مهم‌ترین پدیده‌های طبیعی این منطقه‌اند.",
            culturalHeritage = "آثار باستانی و بافت‌های سنتی روستایی مانند کوشکنار بخشی از پیشینه فرهنگی پارسیان هستند.",
            sourceNote = "مبتنی بر داده‌های جغرافیایی و گردشگری شناخته‌شده هرمزگان (شخصیت کیانوش تخیلی است).",
        ),
    ),

    // ۱۳. ابوموسی
    CountyDefinition(
        id = "abumusa",
        order = 13,
        nameFa = "ابوموسی",
        stageTitle = "نگین نیلگون جنوب",
        subtitle = "آب‌های شفاف خلیج فارس، صخره‌های مرجانی و اسکله جزیره",
        biome = BiomeType.OCEAN_CORAL,
        mapX = 0.38f,
        mapY = 0.84f,
        unlocksNextIds = emptyList(),
        npcName = "ناخدا آرش",
        npcRole = "محافظ مرجان‌ها و اسکله جزیره ابوموسی (شخصیت تخیلی)",
        npcVoice = VoiceProfile.ADULT_MALE,
        npcAppearance = NpcAppearance(
            primaryColorHex = 0xFF0E7490,
            accentColorHex = 0xFFF6C453,
            skinToneHex = 0xFFD99B66,
            headwear = HeadwearStyle.CAPTAIN_HAT,
            hasBeard = true,
            isFemale = false,
        ),
        npcWorldX = 650f,
        openingDialogues = listOf(
            DialogueLine(
                id = "abu_open_1",
                speakerName = "ناخدا آرش",
                isAria = false,
                voiceProfile = VoiceProfile.ADULT_MALE,
                ariaEmotion = AriaAnimationState.HAPPY,
                text = "خوش آمدی قهرمان کوچک! من ناخدا آرش هستم. اینجا جزیره ابوموسی است؛ جنوبی‌ترین نگین آب‌های نیلگون و شفاف خلیج فارس.",
            ),
            DialogueLine(
                id = "abu_open_2",
                speakerName = "آریا",
                isAria = true,
                voiceProfile = VoiceProfile.ARIA,
                ariaEmotion = AriaAnimationState.SURPRISED,
                text = "باورنکردنیه! آب دریا آن‌قدر شفاف و تمیز است که مرجان‌ها و ماهی‌های رنگارنگ زیر موج‌ها دیده می‌شوند!",
            ),
        ),
        farewellDialogues = listOf(
            DialogueLine(
                id = "abu_bye_1",
                speakerName = "ناخدا آرش",
                isAria = false,
                voiceProfile = VoiceProfile.ADULT_MALE,
                ariaEmotion = AriaAnimationState.RECEIVE_REWARD,
                text = "تبریک آریا! تو هر ۱۳ راز سرزمین هرمزگان را کشف کردی. حالا وقت آن است که قطعات دفترچه را کنار هم بگذاری!",
            ),
        ),
        missions = listOf(
            CountyMission(
                id = "abu_m1",
                orderIndex = 1,
                title = "نجات صخره‌های مرجانی",
                shortGoal = "شناخت ارزش آبسنگ‌های مرجانی در حیات دریا",
                worldX = 1120f,
                propType = WorldPropType.CORAL_BUOY,
                puzzleType = PuzzleType.ECO_BALANCE,
                puzzlePayload = PuzzlePayload.InteractiveChallenge(
                    question = "چرا صخره‌های مرجانی پیرامون جزیره ابوموسی و خلیج فارس بسیار حیاتی هستند؟",
                    visualCueTitle = "نقشه زیستگاه مرجانی",
                    options = listOf(
                        ChallengeOption("a1", "خانه و پناهگاه صدها گونه ماهی و حفظ زنجیره حیات دریایی هستند", true, "عالی! مرجان‌ها به عنوان «جنگل‌های بارانی دریا» از تنوع زیستی خلیج فارس محافظت می‌کنند."),
                        ChallengeOption("a2", "تخته سنگ‌های بی‌جان بدون هیچ فایده‌ای هستند", false, "مرجان‌ها جانداران زنده و بسیار حساس دریایی هستند."),
                        ChallengeOption("a3", "باعث خشک شدن آب اقیانوس می‌شوند", false, "مرجان‌ها به پاکیزگی و تعادل زیستی دریا کمک می‌کنند."),
                    ),
                ),
                introDialogues = listOf(
                    DialogueLine(
                        id = "abu_m1_1",
                        speakerName = "ناخدا آرش",
                        isAria = false,
                        voiceProfile = VoiceProfile.ADULT_MALE,
                        ariaEmotion = AriaAnimationState.THINK,
                        text = "آب‌های گرم و شفاف اینجا خانه آبسنگ‌های مرجانی است. اهمیتشان را می‌دانی؟",
                    ),
                ),
                successMessage = "پیمان نگهبانی از مرجان‌های خلیج فارس را با افتخار ثبت کردی!",
                clueRewardTitle = "منشور مرجان آبی",
            ),
            CountyMission(
                id = "abu_m2",
                orderIndex = 2,
                title = "شناخت انرژی پاک جزیره",
                shortGoal = "انتخاب ترکیب پایدار آب شیرین و انرژی در جزایر",
                worldX = 1660f,
                propType = WorldPropType.CORAL_BUOY,
                puzzleType = PuzzleType.ENVIRONMENT_INTERACTION,
                puzzlePayload = PuzzlePayload.MechanismBalance(
                    elements = listOf(
                        MechanismElement("ab1", "بهره‌گیری از انرژی خورشید و باد", 3, "حداکثر"),
                        MechanismElement("ab2", "شیرین‌سازی کم‌مصرف آب دریا با بازیافت شورابه", 2, "متعادل"),
                        MechanismElement("ab3", "حفاظت از ساحل ماسه‌ای نرم", 3, "کامل"),
                    ),
                    targetHint = "انرژی خورشید روی ۳، شیرین‌سازی متعادل روی ۲ و حفاظت ساحل روی ۳ تنظیم شود.",
                ),
                introDialogues = listOf(
                    DialogueLine(
                        id = "abu_m2_1",
                        speakerName = "آریا",
                        isAria = true,
                        voiceProfile = VoiceProfile.ARIA,
                        ariaEmotion = AriaAnimationState.THINK,
                        text = "در یک جزیره دورافتاده، فناوری پاک مثل نور خورشید و انرژی موج بهترین دوست زندگی مردم است.",
                    ),
                ),
                successMessage = "طرح پایداری جزیره سبز ابوموسی آماده شد!",
                clueRewardTitle = "طرح انرژی جزیره",
            ),
            CountyMission(
                id = "abu_m3",
                orderIndex = 3,
                title = "مروارید سیزدهم دفترچه",
                shortGoal = "کشف آخرین قطعه گنج پدربزرگ در ساحل ماسه‌ای",
                worldX = 2130f,
                propType = WorldPropType.ROCK_CARVING_ALCOVE,
                puzzleType = PuzzleType.HIDDEN_OBJECT,
                puzzlePayload = PuzzlePayload.HiddenSpots(
                    targetItemName = "جعبه صدفی قطعه سیزدهم",
                    spots = listOf(
                        SceneSpot("as1", "کنار اسکله سنگی آرام", 0.66f, 0.56f, true, "یافت شد! آخرین قطعه گنج در یک جعبه صدفی زیبا با یادداشت پایانی پدربزرگ!"),
                        SceneSpot("as2", "ماسه نرم و روشن ساحل", 0.30f, 0.70f, false, "شن‌های نرم و سفید ساحل ابوموسی زیر آفتاب."),
                        SceneSpot("as3", "چراغ هدایت دریایی", 0.48f, 0.32f, false, "نمای دور افق آبی خلیج فارس و جزایر پیرامون."),
                    ),
                ),
                introDialogues = listOf(
                    DialogueLine(
                        id = "abu_m3_1",
                        speakerName = "ناخدا آرش",
                        isAria = false,
                        voiceProfile = VoiceProfile.ADULT_MALE,
                        ariaEmotion = AriaAnimationState.TALK,
                        text = "پدربزرگت آخرین نشانه را همین‌جا، کنار آب‌های زلال اسکله به یادگار گذاشت. پیدایش کن آریا!",
                    ),
                ),
                successMessage = "تبریک! هر ۱۳ قطعه گنج پیدا شد و «دانشنامه آینده هرمزگان» آماده گشودن است!",
                clueRewardTitle = "مروارید خلیج فارس",
            ),
        ),
        treasurePiece = TreasurePiece(
            index = 13,
            title = "فناوری دریایی، انرژی موج و آینده سبز خلیج فارس",
            pillarTheme = "فناوری و نوآوری",
            shortMeaning = "همراهی علوم دریایی نوین، انرژی خورشیدی و حفاظت از مرجان‌ها",
            encyclopediaDetail = "ابوموسی و جزایر خلیج فارس نشان می‌دهند که آینده هرمزگان بر پایه اقتصاد آبی پایدار، پژوهش‌های دریایی، استفاده از انرژی خورشید و امواج و حفظ کامل مرجان‌ها استوار است.",
        ),
        scientificBrief = ScientificBrief(
            landmarkTitle = "سواحل شفاف و صخره‌های مرجانی شهرستان ابوموسی",
            geographySummary = "شهرستان ابوموسی از شهرستان‌های جزیره‌ای استان هرمزگان در جنوبی‌ترین بخش آب‌های خلیج فارس است.",
            ecologyAndScience = "آب‌های شفاف و زلال پیرامون جزیره زیستگاه ارزشمند مرجان‌ها و آبزیان متنوع خلیج فارس هستند و سواحل ماسه‌ای نرم و پاکیزه از ویژگی‌های طبیعی آن به شمار می‌روند.",
            culturalHeritage = "بافت بومی ساحلی، اسکله‌های تاریخی و نمادهای دیرینه دریانوردی و حاکمیت ملی ایران در خلیج فارس هویت این جزیره را شکل داده‌اند.",
            sourceNote = "مبتنی بر منابع جغرافیایی رسمی استان هرمزگان (شخصیت ناخدا آرش تخیلی است).",
        ),
    ),
)

object HormozganCatalog {
    val introDialogueLine: DialogueLine = DialogueLine(
        id = "intro_grandpa_letter",
        speakerName = "راوی",
        isAria = false,
        voiceProfile = VoiceProfile.NARRATOR,
        ariaEmotion = AriaAnimationState.OPEN_NOTEBOOK,
        text = "آریای عزیزم؛ در این سرزمین گنجی پنهان شده است. اما برای پیدا کردن آن باید سیزده راز را کشف کنی. هر شهرستان یک نشانه دارد و هر نشانه بخشی از یک حقیقت بزرگتر را آشکار می‌کند.",
    )

    val encyclopediaFinaleLine: DialogueLine = DialogueLine(
        id = "encyclopedia_finale_intro",
        speakerName = "راوی",
        isAria = false,
        voiceProfile = VoiceProfile.NARRATOR,
        ariaEmotion = AriaAnimationState.RECEIVE_REWARD,
        text = "آفرین آریا! این گنج از طلا و جواهر نیست؛ دانش ارزشمندی است که با سفر در هر ۱۳ شهرستان آموختی تا آب، دریا، زمین، فرهنگ و آینده هرمزگان را با عشق و آگاهی بسازی.",
    )

    val allCounties: List<CountyDefinition> =
        (easternCounties + westernAndIslandCounties).sortedBy { it.order }

    fun getById(id: String): CountyDefinition =
        allCounties.firstOrNull { it.id == id } ?: allCounties.first()

    object PuzzleVoiceHints {
        const val PATH_GRID_BLOCKED = "این خانه صخره‌ای یا پرخطر است! از خانه‌های هموار مجاور عبور کن."
        const val WATER_SLOPE_WRONG = "یادت باشد آب به دلیل جاذبه باید از ارتفاع بالاتر به ارتفاع کمتر سرازیر شود!"
        const val SEQUENCE_WRONG = "ترتیب دقیق‌تر را امتحان کن؛ از نخستین گام شروع کن."
    }

    fun missionFeedbackLineId(
        missionId: String,
        kind: String,
        text: String,
    ): String = "${missionId}_${kind}_${text.hashCode().toUInt()}"

    fun treasureRewardLine(county: CountyDefinition): DialogueLine = DialogueLine(
        id = "${county.id}_treasure_reward",
        speakerName = "راوی",
        isAria = false,
        voiceProfile = VoiceProfile.NARRATOR,
        ariaEmotion = AriaAnimationState.RECEIVE_REWARD,
        text = "قطعه ${county.treasurePiece.index.toPersianNumber()} از ۱۳ گنج کشف شد! ${county.treasurePiece.title}. ${county.treasurePiece.encyclopediaDetail}",
    )

    fun countyDialogueLines(county: CountyDefinition): List<DialogueLine> =
        buildList {
            fun addNpcLine(
                id: String,
                text: String,
                emotion: AriaAnimationState = AriaAnimationState.DISCOVER_CLUE,
            ) {
                add(
                    DialogueLine(
                        id = id,
                        speakerName = county.npcName,
                        isAria = false,
                        voiceProfile = county.npcVoice,
                        ariaEmotion = emotion,
                        text = text,
                    ),
                )
            }

            addAll(county.openingDialogues)
            county.missions.forEach { mission ->
                addAll(mission.introDialogues)
                addNpcLine(id = "${mission.id}_goal", text = mission.shortGoal, emotion = AriaAnimationState.THINK)
                addNpcLine(id = "${mission.id}_success", text = mission.successMessage)

                when (val payload = mission.puzzlePayload) {
                    is PuzzlePayload.PathGrid -> {
                        addNpcLine(
                            id = missionFeedbackLineId(mission.id, "hint", PuzzleVoiceHints.PATH_GRID_BLOCKED),
                            text = PuzzleVoiceHints.PATH_GRID_BLOCKED,
                            emotion = AriaAnimationState.THINK,
                        )
                    }
                    is PuzzlePayload.WaterSlope -> {
                        addNpcLine(
                            id = missionFeedbackLineId(mission.id, "hint", PuzzleVoiceHints.WATER_SLOPE_WRONG),
                            text = PuzzleVoiceHints.WATER_SLOPE_WRONG,
                            emotion = AriaAnimationState.THINK,
                        )
                        addNpcLine(
                            id = missionFeedbackLineId(mission.id, "solved", payload.explanation),
                            text = payload.explanation,
                        )
                    }
                    is PuzzlePayload.HiddenSpots -> {
                        payload.spots.forEach { spot ->
                            val kind = if (spot.isTarget) "solved" else "spot"
                            addNpcLine(
                                id = missionFeedbackLineId(mission.id, kind, spot.inspectionText),
                                text = spot.inspectionText,
                                emotion = if (spot.isTarget) AriaAnimationState.DISCOVER_CLUE else AriaAnimationState.LOOK_AROUND,
                            )
                        }
                    }
                    is PuzzlePayload.SequenceItems -> {
                        addNpcLine(
                            id = missionFeedbackLineId(mission.id, "hint", PuzzleVoiceHints.SEQUENCE_WRONG),
                            text = PuzzleVoiceHints.SEQUENCE_WRONG,
                            emotion = AriaAnimationState.THINK,
                        )
                    }
                    is PuzzlePayload.InteractiveChallenge -> {
                        payload.options.forEach { option ->
                            val kind = if (option.isCorrect) "solved" else "hint"
                            addNpcLine(
                                id = missionFeedbackLineId(mission.id, kind, option.feedbackText),
                                text = option.feedbackText,
                                emotion = if (option.isCorrect) AriaAnimationState.DISCOVER_CLUE else AriaAnimationState.THINK,
                            )
                        }
                    }
                    is PuzzlePayload.PatternMatrix,
                    is PuzzlePayload.MechanismBalance,
                    -> Unit
                }
            }
            add(treasureRewardLine(county))
            addAll(county.farewellDialogues)
        }.distinctBy { it.id }

    fun allDialogueLines(): List<DialogueLine> =
        buildList {
            add(introDialogueLine)
            allCounties.forEach { addAll(countyDialogueLines(it)) }
            add(encyclopediaFinaleLine)
        }.distinctBy { it.id }

    val encyclopediaPillars: List<EncyclopediaPillar> = listOf(
        EncyclopediaPillar(1, "مدیریت منابع آب", "بهره‌گیری از شیب طبیعی زمین در کوهستان‌های بشاگرد و آب‌انبارهای گنبدی بندرعباس و چاه‌های تلا برای مهار بدون هدررفت باران.", "بشاگرد، بندرعباس، قشم"),
        EncyclopediaPillar(2, "حفاظت از محیط‌زیست", "نگهداری از چشمه‌ها، تنگه‌های طبیعی و مناطق حفاظت‌شده گنو و گوده با گردشگری مسئولانه و بدون زباله.", "پارسیان، بستک، بندرعباس"),
        EncyclopediaPillar(3, "شناخت زمین‌شناسی", "مطالعه ژئوپارک جهانی قشم، غار نمکدان و کوه‌های مریخی جاسک برای حفظ میراث میلیون‌ساله زمین.", "قشم، جاسک"),
        EncyclopediaPillar(4, "حفاظت از دریا", "صیانت از آبسنگ‌های مرجانی ابوموسی، جلوگیری از آلودگی دریایی و پاسداری از خورهای ساحلی.", "ابوموسی، بندرعباس، بندرلنگه"),
        EncyclopediaPillar(5, "تنوع زیستی", "حمایت از جنگل‌های دریایی حرا در خورخوران و خور آذینی به عنوان مهد پرورش آبزیان، لاک‌پشت‌ها و پرندگان مهاجر.", "بندر خمیر، سیریک"),
        EncyclopediaPillar(6, "انرژی پاک", "الهام از آسیاب‌های آبی قدیمی حاجی‌آباد و استفاده گسترده از انرژی خورشیدی و بادی در بنادر و جزایر.", "حاجی‌آباد، سیریک، ابوموسی"),
        EncyclopediaPillar(7, "کشاورزی هوشمند", "آبیاری دقیق، حفظ رطوبت خاک و پرورش گونه‌های سازگار با اقلیم مثل انبه، لیمو و نخل در دشت رودان و میناب.", "رودان، میناب"),
        EncyclopediaPillar(8, "معماری سازگار با اقلیم", "الگوبرداری از بادگیرهای بستک و لافت، حیاط‌های مرکزی و پنجره‌های مشبک برای خنک‌سازی طبیعی ساختمان‌ها.", "بستک، بندر لنگه، قشم"),
        EncyclopediaPillar(9, "دانش بومی", "ارج نهادن به هنرهایی چون حصیربافی میناب و بشاگرد، لنج‌سازی و تجربه‌های نسل‌های گذشته در هم‌زیستی با طبیعت.", "میناب، بشاگرد، بندرلنگه"),
        EncyclopediaPillar(10, "فناوری و نوآوری", "ترکیب دانش نوین، رصد زیست‌محیطی و فناوری‌های دریایی پاک برای ساخت فردایی آباد در سراسر هرمزگان.", "سراسر ۱۳ شهرستان هرمزگان"),
    )
}
