package com.cyclesync.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tracking_entries",
    indices = [
        Index(value = ["category", "subcategory"]),
        Index(value = ["daily_log_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = DailyLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["daily_log_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TrackingEntryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "daily_log_id") val dailyLogId: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "subcategory") val subcategory: String,
    @ColumnInfo(name = "intensity") val intensity: Int? = null,
    @ColumnInfo(name = "custom_value") val customValue: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: String = ""
)
