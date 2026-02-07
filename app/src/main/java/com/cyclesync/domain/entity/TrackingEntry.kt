package com.cyclesync.domain.entity

data class TrackingEntry(
    val id: String,
    val dailyLogId: String,
    val category: TrackingCategory,
    val subcategory: String,
    val intensity: Int? = null,
    val customValue: String? = null
) {
    val hasIntensity: Boolean
        get() = intensity != null

    val hasCustomValue: Boolean
        get() = !customValue.isNullOrBlank()

    val displaySubcategory: String
        get() = subcategory.replace("_", " ").replaceFirstChar { it.uppercase() }

    val key: String
        get() = "${category.name}_$subcategory"
}
