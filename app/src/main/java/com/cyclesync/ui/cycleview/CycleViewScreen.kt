package com.cyclesync.ui.cycleview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyclesync.domain.entity.CyclePhase
import com.cyclesync.ui.cycleview.components.CycleRing
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CycleViewScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToTracking: (String) -> Unit,
    viewModel: CycleViewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = state.mode,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onNavigateToSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cycle Ring
        CycleRing(
            currentDay = state.currentCycleDay,
            totalDays = state.predictedCycleLength,
            periodLength = state.periodLength,
            currentPhase = state.currentPhase,
            modifier = Modifier.size(280.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Phase and day info
        if (state.hasCycles) {
            Text(
                text = state.phaseName.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = getPhaseColor(state.currentPhase)
            )

            Text(
                text = "Day ${state.currentCycleDay} of ${state.predictedCycleLength}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            state.daysUntilNextPeriod?.let { days ->
                val text = when {
                    days > 0 -> "Next period in ~$days days"
                    days == 0 -> "Period predicted today"
                    else -> "Period is ${-days} days late"
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Confidence: ${(state.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "No cycles tracked yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap + to log your period",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Prediction cards
        if (state.hasCycles) {
            PredictionInfoCard(
                title = "Next Period",
                date = state.nextPeriodDate,
                subtitle = state.daysUntilNextPeriod?.let { "$it days away" }
            )

            state.ovulationDate?.let {
                Spacer(modifier = Modifier.height(8.dp))
                PredictionInfoCard(
                    title = "Ovulation",
                    date = it,
                    subtitle = if (it.isAfter(LocalDate.now())) "Estimated" else "Passed"
                )
            }

            state.fertileWindowStart?.let { start ->
                state.fertileWindowEnd?.let { end ->
                    Spacer(modifier = Modifier.height(8.dp))
                    PredictionInfoCard(
                        title = "Fertile Window",
                        date = null,
                        subtitle = "${start.format(DateTimeFormatter.ofPattern("MMM d"))} - ${end.format(DateTimeFormatter.ofPattern("MMM d"))}"
                    )
                }
            }
        }
    }
}

@Composable
private fun PredictionInfoCard(
    title: String,
    date: LocalDate?,
    subtitle: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            date?.let {
                Text(
                    text = it.format(DateTimeFormatter.ofPattern("MMM d")),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun getPhaseColor(phase: CyclePhase): androidx.compose.ui.graphics.Color {
    return when (phase) {
        CyclePhase.MENSTRUATION -> com.cyclesync.core.constants.AppColors.Menstruation
        CyclePhase.FOLLICULAR -> com.cyclesync.core.constants.AppColors.Follicular
        CyclePhase.OVULATION -> com.cyclesync.core.constants.AppColors.Ovulation
        CyclePhase.LUTEAL -> com.cyclesync.core.constants.AppColors.Luteal
        CyclePhase.PMS -> com.cyclesync.core.constants.AppColors.PMS
    }
}
