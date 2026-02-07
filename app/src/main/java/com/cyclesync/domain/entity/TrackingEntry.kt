package com.cyclesync.domain.entity

data class TrackingEntry(
    val id: String,
    val dailyLogId: String,
    val category: TrackingCategory,
    val subcategory: String,
    val intensity: Int? = null,
    val customValue: String? = null
)
