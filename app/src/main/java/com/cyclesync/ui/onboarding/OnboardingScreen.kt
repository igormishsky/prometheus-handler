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
                Text(if (currentStep == 5) "Start My Journey" else "Next")
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
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "The cycle tracker that\nactually respects you.",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Most period apps sell your intimate health data to advertisers, insurers, and data brokers. CycleSync is different \u2014 your data never leaves your device. Ever.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Trusted by women who refuse to\nbe the product.",
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun PrivacyStep() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Your Body, Your Data.",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No one should profit from your most personal health information.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
        val points = listOf(
            "Military-grade encryption on your device",
            "Zero internet access \u2014 we can\u2019t see your data even if we wanted to",
            "No accounts, no email, no phone number",
            "Screenshot protection \u2014 your data stays off screen captures",
            "Delete all data instantly from Settings if ever needed"
        )
        points.forEach { point ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = point,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun CycleLengthStep(viewModel: OnboardingViewModel) {
    val cycleLength by viewModel.cycleLength.collectAsState()
    val periodLength by viewModel.periodLength.collectAsState()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Let\u2019s Personalize",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This helps our Bayesian prediction engine learn your unique pattern faster.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            Text("Not sure? Use averages \u2014 it gets smarter over time")
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
            text = "What\u2019s Your Goal?",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "CycleSync adapts to your life stage. Switch anytime.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        val modes = listOf(
            AppMode.PERIOD_TRACKING to "Understand your body with AI-powered cycle predictions",
            AppMode.CONCEIVE to "Pinpoint your fertile window with scientific precision",
            AppMode.PREGNANCY to "Track your pregnancy journey week by week",
            AppMode.PERIMENOPAUSE to "Navigate the transition with personalized insights"
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
            text = "You\u2019re In Control.",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Welcome to the only cycle tracker that puts you first.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "What happens next:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                val steps = listOf(
                    "Log your first entry \u2014 it only takes 30 seconds",
                    "Predictions improve with each cycle you track",
                    "Discover patterns you never knew about your body"
                )
                steps.forEachIndexed { index, step ->
                    Text(
                        text = "${index + 1}. $step",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your data is encrypted and stored only on this device.\nNo one else can ever access it.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
