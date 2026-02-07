package com.cyclesync.ui.cycleview.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cyclesync.core.constants.AppColors
import com.cyclesync.domain.entity.CyclePhase
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CycleRing(
    currentDay: Int,
    totalDays: Int,
    periodLength: Int,
    currentPhase: CyclePhase,
    modifier: Modifier = Modifier
) {
    val phaseColor = when (currentPhase) {
        CyclePhase.MENSTRUATION -> AppColors.Menstruation
        CyclePhase.FOLLICULAR -> AppColors.Follicular
        CyclePhase.OVULATION -> AppColors.Ovulation
        CyclePhase.LUTEAL -> AppColors.Luteal
        CyclePhase.PMS -> AppColors.PMS
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(modifier = modifier) {
            val strokeWidth = 24.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)
            val arcSize = Size(radius * 2, radius * 2)
            val topLeft = Offset(center.x - radius, center.y - radius)
            val degreesPerDay = 360f / totalDays

            // Draw phase segments
            val segments = calculatePhaseSegments(totalDays, periodLength)
            segments.forEach { (startDay, endDay, color) ->
                val startAngle = -90f + (startDay - 1) * degreesPerDay
                val sweepAngle = (endDay - startDay + 1) * degreesPerDay
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
            }

            // Draw current day indicator
            if (currentDay in 1..totalDays) {
                val angle = Math.toRadians((-90.0 + (currentDay - 1) * degreesPerDay))
                val indicatorX = center.x + radius * cos(angle).toFloat()
                val indicatorY = center.y + radius * sin(angle).toFloat()
                drawCircle(
                    color = Color.White,
                    radius = strokeWidth / 2 + 4.dp.toPx(),
                    center = Offset(indicatorX, indicatorY)
                )
                drawCircle(
                    color = phaseColor,
                    radius = strokeWidth / 2,
                    center = Offset(indicatorX, indicatorY)
                )
            }
        }

        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = currentPhase.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = phaseColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Day $currentDay",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun calculatePhaseSegments(
    totalDays: Int,
    periodLength: Int
): List<Triple<Int, Int, Color>> {
    val ovulationDay = totalDays - 14
    val fertileStart = (ovulationDay - 5).coerceAtLeast(periodLength + 1)
    val fertileEnd = (ovulationDay + 1).coerceAtMost(totalDays)
    val pmsStart = (totalDays - 7).coerceAtLeast(fertileEnd + 1)

    val segments = mutableListOf<Triple<Int, Int, Color>>()

    // Menstruation
    segments.add(Triple(1, periodLength, AppColors.Menstruation))

    // Follicular (after period, before fertile window)
    if (periodLength + 1 < fertileStart) {
        segments.add(Triple(periodLength + 1, fertileStart - 1, AppColors.Follicular))
    }

    // Fertile/Ovulation window
    if (fertileStart <= fertileEnd) {
        segments.add(Triple(fertileStart, fertileEnd, AppColors.Ovulation))
    }

    // Luteal (after fertile, before PMS)
    if (fertileEnd + 1 < pmsStart) {
        segments.add(Triple(fertileEnd + 1, pmsStart - 1, AppColors.Luteal))
    }

    // PMS
    if (pmsStart <= totalDays) {
        segments.add(Triple(pmsStart, totalDays, AppColors.PMS))
    }

    return segments
}
