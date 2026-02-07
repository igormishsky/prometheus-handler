package com.cyclesync.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pregnancy")
data class PregnancyEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "lmp_date") val lmpDate: String? = null,
    @ColumnInfo(name = "due_date") val dueDate: String,
    @ColumnInfo(name = "conception_date") val conceptionDate: String? = null,
    @ColumnInfo(name = "started_at") val startedAt: String = "",
    @ColumnInfo(name = "ended_at") val endedAt: String? = null,
    @ColumnInfo(name = "outcome") val outcome: String? = null,
    @ColumnInfo(name = "notes") val notes: String? = null
)
