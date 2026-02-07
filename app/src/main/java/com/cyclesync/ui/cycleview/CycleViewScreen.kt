package com.cyclesync.ui.cycleview

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyclesync.core.constants.AppColors
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
                text = "Your Journey Starts Here",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Log your first period to unlock personalized predictions, body insights, and cycle superpowers.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Tap + to get started \u2014 it takes 30 seconds",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Streak Card (commitment/consistency hook)
        if (state.streakDays > 0 || state.hasCycles) {
            StreakCard(days = state.streakDays, message = state.streakMessage)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Phase Superpower Card (identity/empowerment hook)
        if (state.phaseSuperpower.isNotEmpty() && state.hasCycles) {
            SuperpowerCard(superpower = state.phaseSuperpower, phase = state.currentPhase)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Daily Affirmation Card
        if (state.dailyAffirmation.isNotEmpty()) {
            AffirmationCard(affirmation = state.dailyAffirmation)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Body Insight Card (variable reward / knowledge hook)
        if (state.bodyInsight.isNotEmpty() && state.hasCycles) {
            BodyInsightCard(insight = state.bodyInsight, phase = state.currentPhase)
            Spacer(modifier = Modifier.height(16.dp))
        }

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

        // Wellness Tip Card
        if (state.wellnessTip.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            WellnessTipCard(tip = state.wellnessTip, phase = state.currentPhase)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AffirmationCard(affirmation: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.SelfCare.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily Affirmation",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.Primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = affirmation,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun WellnessTipCard(tip: String, phase: CyclePhase) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Wellness.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Spa,
                contentDescription = null,
                tint = AppColors.Wellness,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Wellness Tip \u2022 ${phase.displayName} Phase",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.Wellness
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
private fun StreakCard(days: Int, message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Ovulation.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = AppColors.Ovulation,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (days > 0) {
                    Text(
                        text = "$days-Day Tracking Streak",
                        style = MaterialTheme.typography.titleSmall,
                        color = AppColors.Ovulation
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun SuperpowerCard(superpower: String, phase: CyclePhase) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Follicular.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = AppColors.Follicular,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Your ${phase.displayName} Superpower",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.Follicular
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = superpower,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun BodyInsightCard(insight: String, phase: CyclePhase) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Luteal.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = AppColors.Luteal,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Your Body Right Now",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.Luteal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = insight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
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
