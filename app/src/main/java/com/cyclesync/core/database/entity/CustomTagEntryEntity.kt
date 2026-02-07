package com.cyclesync.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "custom_tag_entries",
    foreignKeys = [
        ForeignKey(
            entity = DailyLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["daily_log_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CustomTagEntity::class,
            parentColumns = ["id"],
            childColumns = ["custom_tag_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CustomTagEntryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "daily_log_id") val dailyLogId: String,
    @ColumnInfo(name = "custom_tag_id") val customTagId: String,
    @ColumnInfo(name = "created_at") val createdAt: String = ""
)
