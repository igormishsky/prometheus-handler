package com.cyclesync.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cycles",
    indices = [
        Index(value = ["start_date"]),
        Index(value = ["end_date"]),
        Index(value = ["is_excluded"])
    ]
)
data class CycleEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "cycle_number") val cycleNumber: Int,
    @ColumnInfo(name = "start_date") val startDate: String,
    @ColumnInfo(name = "end_date") val endDate: String? = null,
    @ColumnInfo(name = "period_start_date") val periodStartDate: String,
    @ColumnInfo(name = "period_end_date") val periodEndDate: String? = null,
    @ColumnInfo(name = "cycle_length") val cycleLength: Int? = null,
    @ColumnInfo(name = "period_length") val periodLength: Int? = null,
    @ColumnInfo(name = "is_excluded") val isExcluded: Boolean = false,
    @ColumnInfo(name = "notes") val notes: String? = null,
    @ColumnInfo(name = "skip_reason") val skipReason: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: String = "",
    @ColumnInfo(name = "updated_at") val updatedAt: String = ""
)
