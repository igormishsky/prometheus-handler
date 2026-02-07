package com.cyclesync.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyclesync.domain.entity.AppMode
import com.cyclesync.domain.entity.BmiCategory
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val currentStep by viewModel.currentStep.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(6) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (index == currentStep) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index <= currentStep) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedContent(targetState = currentStep, label = "onboarding") { step ->
            when (step) {
                0 -> WelcomeStep()
                1 -> PrivacyStep()
                2 -> CycleLengthStep(viewModel)
                3 -> LastPeriodStep(viewModel)
                4 -> ModeSelectionStep(viewModel)
                5 -> CompletionStep()
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentStep > 0) {
                OutlinedButton(onClick = { viewModel.previousStep() }) {
                    Text("Back")
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            Button(
                onClick = {
                    if (currentStep == 5) {
                        viewModel.completeOnboarding()
                        onComplete()
                    } else {
                        viewModel.nextStep()
                    }
                }
            ) {
                Text(if (currentStep == 5) "Get Started" else "Next")
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "CycleSync",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your data stays on YOUR device.\nNo accounts. No cloud. No tracking.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun PrivacyStep() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Privacy First",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        val points = listOf(
            "All data encrypted on device",
            "Zero network connections",
            "No analytics or tracking",
            "Optional biometric lock",
            "Emergency data wipe available"
        )
        points.forEach { point ->
            Text(
                text = "  $point",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun CycleLengthStep(viewModel: OnboardingViewModel) {
    val cycleLength by viewModel.cycleLength.collectAsState()
    val periodLength by viewModel.periodLength.collectAsState()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Your Cycle",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text("Typical cycle length: $cycleLength days", style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = cycleLength.toFloat(),
            onValueChange = { viewModel.setCycleLength(it.roundToInt()) },
            valueRange = 21f..45f,
            steps = 23,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Typical period length: $periodLength days", style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = periodLength.toFloat(),
            onValueChange = { viewModel.setPeriodLength(it.roundToInt()) },
            valueRange = 2f..10f,
            steps = 7,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = {
            viewModel.setCycleLength(29)
            viewModel.setPeriodLength(5)
        }) {
            Text("Not sure? Use defaults")
        }
    }
}

@Composable
private fun LastPeriodStep(viewModel: OnboardingViewModel) {
    val lastPeriodDate by viewModel.lastPeriodDate.collectAsState()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Last Period",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "When did your last period start?",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Simple date selection via day offsets
        val options = listOf(
            "Today" to 0L,
            "Yesterday" to 1L,
            "2 days ago" to 2L,
            "3 days ago" to 3L,
            "1 week ago" to 7L,
            "2 weeks ago" to 14L,
            "3 weeks ago" to 21L,
            "4 weeks ago" to 28L
        )

        options.forEach { (label, daysAgo) ->
            val date = LocalDate.now().minusDays(daysAgo)
            val isSelected = lastPeriodDate == date
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { viewModel.setLastPeriodDate(date) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { viewModel.setLastPeriodDate(null) }) {
            Text("Skip - I'll log it later")
        }
    }
}

@Composable
private fun ModeSelectionStep(viewModel: OnboardingViewModel) {
    val selectedMode by viewModel.selectedMode.collectAsState()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Choose Your Mode",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You can switch modes anytime",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        val modes = listOf(
            AppMode.PERIOD_TRACKING to "Track your cycle, symptoms, and get predictions",
            AppMode.CONCEIVE to "Fertility tracking with fertile window predictions",
            AppMode.PREGNANCY to "Week-by-week pregnancy tracking",
            AppMode.PERIMENOPAUSE to "Adapted for irregular cycles and transition symptoms"
        )

        modes.forEach { (mode, description) ->
            val isSelected = selectedMode == mode
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { viewModel.setSelectedMode(mode) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = mode.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletionStep() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "You're All Set",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "CycleSync is ready to use.\n\nAll your data is encrypted and stored only on this device. No one else can access it.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
