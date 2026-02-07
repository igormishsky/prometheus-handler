package com.cyclesync.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "temperature_logs",
    indices = [Index(value = ["date"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = DailyLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["daily_log_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class TemperatureLogEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "temperature") val temperature: Double,
    @ColumnInfo(name = "time_taken") val timeTaken: String? = null,
    @ColumnInfo(name = "method") val method: String = "oral",
    @ColumnInfo(name = "is_flagged") val isFlagged: Boolean = false,
    @ColumnInfo(name = "daily_log_id") val dailyLogId: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: String = ""
)
