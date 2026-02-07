package com.cyclesync.ui.analysis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyclesync.core.constants.AppColors
import com.cyclesync.domain.entity.Trend
import java.time.format.DateTimeFormatter

@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Analysis",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!state.hasCycles) {
            Text(
                text = "Track at least one complete cycle to see your analysis.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            return@Column
        }

        state.analysis?.let { analysis ->
            // Summary cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Avg Cycle",
                    value = "${String.format("%.1f", analysis.averageCycleLength)}d",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Avg Period",
                    value = "${String.format("%.1f", analysis.averagePeriodLength)}d",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Variability",
                    value = "\u00B1${String.format("%.1f", analysis.cycleLengthVariability)}d",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Regularity",
                    value = "${analysis.regularityScore}/10",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Shortest",
                    value = "${analysis.shortestCycle}d",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Longest",
                    value = "${analysis.longestCycle}d",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Cycles Tracked",
                    value = "${analysis.totalCyclesTracked}",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Trend",
                    value = when (analysis.cycleLengthTrend) {
                        Trend.LENGTHENING -> "Lengthening"
                        Trend.SHORTENING -> "Shortening"
                        Trend.STABLE -> "Stable"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Cycle length chart
            if (analysis.cycleLengths.size >= 2) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Cycle Length History",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                CycleLengthChart(
                    lengths = analysis.cycleLengths,
                    average = analysis.averageCycleLength,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            // Symptom Patterns - Cross-cycle visualization
            if (state.symptomPatterns.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Symptom Patterns",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Most frequent symptoms across ${state.totalTrackedDays} tracked days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        state.symptomPatterns.forEachIndexed { index, symptom ->
                            SymptomPatternRow(
                                symptom = symptom,
                                maxPercentage = state.symptomPatterns.firstOrNull()?.percentage ?: 100.0
                            )
                            if (index < state.symptomPatterns.size - 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            // Cycle Summaries
            if (state.cycleSummaries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Cycle Summaries",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Recent cycle history with top symptoms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                state.cycleSummaries.forEach { summary ->
                    CycleSummaryCard(summary)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SymptomPatternRow(
    symptom: SymptomFrequency,
    maxPercentage: Double
) {
    val barFraction = if (maxPercentage > 0) (symptom.percentage / maxPercentage).toFloat() else 0f

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = symptom.subcategory.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${symptom.count}x (${String.format("%.0f", symptom.percentage)}%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(barFraction)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(AppColors.Secondary)
            )
        }
    }
}

@Composable
private fun CycleSummaryCard(summary: CycleSummary) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cycle ${summary.cycleNumber}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = summary.startDate.format(dateFormatter) +
                            (summary.endDate?.let { " - ${it.format(dateFormatter)}" } ?: " - ongoing"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                summary.cycleLength?.let {
                    Column {
                        Text("Length", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${it}d", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                summary.periodLength?.let {
                    Column {
                        Text("Period", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${it}d", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (summary.topSymptoms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Top symptoms: ${summary.topSymptoms.joinToString(", ") { it.replaceFirstChar { c -> c.uppercase() } }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            summary.notes?.let { notes ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CycleLengthChart(
    lengths: List<Int>,
    average: Double,
    modifier: Modifier = Modifier
) {
    val barColor = AppColors.Secondary
    val avgColor = AppColors.Primary
    val surfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = modifier) {
        if (lengths.isEmpty()) return@Canvas

        val maxVal = lengths.max().toFloat()
        val minVal = (lengths.min() - 2).coerceAtLeast(0).toFloat()
        val range = (maxVal - minVal).coerceAtLeast(1f)
        val barWidth = (size.width / lengths.size) * 0.7f
        val gap = (size.width / lengths.size) * 0.3f

        // Draw bars
        lengths.forEachIndexed { index, length ->
            val barHeight = ((length - minVal) / range) * size.height * 0.85f
            val x = index * (barWidth + gap) + gap / 2
            drawRect(
                color = barColor,
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barWidth, barHeight)
            )
        }

        // Draw average line
        val avgY = size.height - ((average.toFloat() - minVal) / range) * size.height * 0.85f
        drawLine(
            color = avgColor,
            start = Offset(0f, avgY),
            end = Offset(size.width, avgY),
            strokeWidth = 2.dp.toPx()
        )
    }
}
