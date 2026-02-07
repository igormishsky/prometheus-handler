package com.cyclesync.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "predictions",
    indices = [
        Index(value = ["type", "predicted_date"]),
        Index(value = ["cycle_id"])
    ]
)
data class PredictionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "cycle_id") val cycleId: String? = null,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "predicted_date") val predictedDate: String,
    @ColumnInfo(name = "confidence") val confidence: Double,
    @ColumnInfo(name = "lower_bound") val lowerBound: String? = null,
    @ColumnInfo(name = "upper_bound") val upperBound: String? = null,
    @ColumnInfo(name = "algorithm_version") val algorithmVersion: String,
    @ColumnInfo(name = "generated_at") val generatedAt: String = "",
    @ColumnInfo(name = "is_stale") val isStale: Boolean = false
)
