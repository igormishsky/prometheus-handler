package com.cyclesync.domain.entity

enum class TrackingCategory(val displayName: String) {
    BLEEDING("Bleeding"),
    PAIN("Pain"),
    MOOD("Mood"),
    ENERGY("Energy"),
    SLEEP("Sleep"),
    EXERCISE("Exercise"),
    SEX("Sex"),
    DISCHARGE("Discharge"),
    SKIN("Skin"),
    HAIR("Hair"),
    DIGESTION("Digestion"),
    MENTAL("Mental"),
    SOCIAL("Social"),
    STOOL("Stool"),
    TEMPERATURE("Temperature"),
    WEIGHT("Weight"),
    MEDICATION("Medication"),
    PERIMENOPAUSE("Perimenopause"),
    PREGNANCY("Pregnancy")
}

object TrackingSubcategories {
    val bleeding = mapOf(
        "flow" to listOf("light", "medium", "heavy", "super_heavy"),
        "spotting" to listOf("yes"),
        "color" to listOf("bright_red", "dark_red", "brown", "pink", "orange"),
        "clots" to listOf("none", "small", "large"),
        "product" to listOf("pad", "tampon", "cup", "disc", "liner", "underwear", "free_bleeding")
    )

    val pain = listOf(
        "cramps", "headache", "migraine", "lower_back", "ovulation_pain",
        "breast_tenderness", "joint_pain", "body_aches", "pelvic_pain", "leg_pain"
    )

    val mood = listOf(
        "happy", "calm", "anxious", "sad", "irritable", "mood_swings",
        "sensitive", "confident", "apathetic", "angry", "overwhelmed",
        "depressed", "grateful", "restless", "indifferent", "weepy"
    )

    val energy = listOf("energized", "high", "moderate", "low", "exhausted", "wired")

    val sleep = mapOf(
        "quality" to listOf("good", "fair", "poor", "insomnia"),
        "symptoms" to listOf("vivid_dreams", "night_sweats", "trouble_falling_asleep", "woke_up_early")
    )

    val exercise = mapOf(
        "type" to listOf(
            "running", "walking", "yoga", "pilates", "swimming", "cycling",
            "weights", "hiit", "dance", "martial_arts", "team_sports", "climbing",
            "stretching", "other"
        ),
        "intensity" to listOf("light", "moderate", "intense")
    )

    val sex = mapOf(
        "activity" to listOf("protected", "unprotected", "withdrawal", "oral", "masturbation"),
        "orgasm" to listOf("yes", "no"),
        "libido" to listOf("high", "low", "none"),
        "pain_during" to listOf("yes", "no")
    )

    val discharge = mapOf(
        "type" to listOf("dry", "sticky", "creamy", "watery", "egg_white", "atypical"),
        "amount" to listOf("light", "moderate", "heavy"),
        "color" to listOf("clear", "white", "yellow", "green", "brown")
    )

    val skin = listOf(
        "clear", "oily", "dry", "acne_mild", "acne_moderate", "acne_severe",
        "glow", "rash", "eczema_flare"
    )

    val hair = listOf("good_day", "oily", "dry", "shedding")

    val digestion = mapOf(
        "symptoms" to listOf("bloating", "nausea", "constipation", "diarrhea", "gas", "heartburn"),
        "appetite" to listOf("increased", "normal", "decreased", "none"),
        "cravings" to listOf("sweet", "salty", "carbs", "spicy", "dairy", "specific")
    )

    val mental = listOf(
        "focused", "brain_fog", "forgetful", "creative", "motivated",
        "distracted", "productive", "overwhelmed"
    )

    val social = listOf("social", "withdrawn", "conflict_prone", "nurturing", "isolating", "connected")

    val bristolStool = listOf("1", "2", "3", "4", "5", "6", "7")

    val perimenopause = listOf(
        "hot_flash", "night_sweats", "vaginal_dryness", "heart_palpitations",
        "brain_fog", "joint_stiffness", "urinary_changes"
    )

    val pregnancy = mapOf(
        "nausea_morning" to emptyList<String>(),
        "fetal_movement" to listOf("none", "light", "moderate", "strong"),
        "braxton_hicks" to listOf("yes"),
        "swelling" to listOf("none", "mild", "moderate", "severe"),
        "supplement_taken" to listOf("prenatal", "iron", "folate", "dha", "other")
    )
}
