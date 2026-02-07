package com.cyclesync.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "algorithm_state")
data class AlgorithmStateEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "model_version") val modelVersion: String = "1.0",
    @ColumnInfo(name = "prior_cycle_mean") val priorCycleMean: Double = 29.3,
    @ColumnInfo(name = "prior_cycle_variance") val priorCycleVariance: Double = 56.25,
    @ColumnInfo(name = "prior_follicular_mean") val priorFollicularMean: Double = 16.9,
    @ColumnInfo(name = "prior_luteal_mean") val priorLutealMean: Double = 12.4,
    @ColumnInfo(name = "prior_luteal_variance") val priorLutealVariance: Double = 5.76,
    @ColumnInfo(name = "posterior_cycle_mean") val posteriorCycleMean: Double? = null,
    @ColumnInfo(name = "posterior_cycle_variance") val posteriorCycleVariance: Double? = null,
    @ColumnInfo(name = "posterior_follicular_mean") val posteriorFollicularMean: Double? = null,
    @ColumnInfo(name = "posterior_luteal_mean") val posteriorLutealMean: Double? = null,
    @ColumnInfo(name = "posterior_luteal_variance") val posteriorLutealVariance: Double? = null,
    @ColumnInfo(name = "population_weight") val populationWeight: Double = 1.0,
    @ColumnInfo(name = "individual_weight") val individualWeight: Double = 0.0,
    @ColumnInfo(name = "cycles_tracked") val cyclesTracked: Int = 0,
    @ColumnInfo(name = "skip_probability") val skipProbability: Double = 0.0,
    @ColumnInfo(name = "last_recalculated") val lastRecalculated: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: String = ""
)
