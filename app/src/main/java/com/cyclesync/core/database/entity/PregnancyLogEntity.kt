package com.cyclesync.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "pregnancy_logs",
    foreignKeys = [
        ForeignKey(
            entity = PregnancyEntity::class,
            parentColumns = ["id"],
            childColumns = ["pregnancy_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PregnancyLogEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "pregnancy_id") val pregnancyId: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "gestational_week") val gestationalWeek: Int? = null,
    @ColumnInfo(name = "gestational_day") val gestationalDay: Int? = null,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "subcategory") val subcategory: String? = null,
    @ColumnInfo(name = "value") val value: String? = null,
    @ColumnInfo(name = "notes") val notes: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: String = ""
)
