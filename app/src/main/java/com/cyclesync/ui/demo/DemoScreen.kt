package com.cyclesync.ui.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun DemoScreen(
    onContinue: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(150)
        showContent = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Text(
                text = "CycleSync",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Your cycle. Your data. Your control.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Benefits cards
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + slideInVertically { it / 4 }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BenefitCard(
                        icon = Icons.Filled.CalendarMonth,
                        title = "Smart Cycle Tracking",
                        description = "Track your period, symptoms, and moods with an intuitive calendar and visual cycle ring. Log flow intensity, pain levels, energy, and more.",
                        accent = MaterialTheme.colorScheme.primary
                    )

                    BenefitCard(
                        icon = Icons.Filled.TrendingUp,
                        title = "Accurate Predictions",
                        description = "Bayesian prediction engine learns from your history to forecast your next period, fertile window, and PMS — getting smarter over time.",
                        accent = MaterialTheme.colorScheme.secondary
                    )

                    BenefitCard(
                        icon = Icons.Filled.Analytics,
                        title = "Cycle Insights",
                        description = "Visualize patterns in your cycle length, symptoms, and mood trends. Understand your body with personalized analysis and statistics.",
                        accent = MaterialTheme.colorScheme.tertiary
                    )

                    BenefitCard(
                        icon = Icons.Filled.HealthAndSafety,
                        title = "4 Tracking Modes",
                        description = "Period Tracking, Trying to Conceive, Pregnancy, and Perimenopause. Switch modes anytime — your data adapts with you.",
                        accent = MaterialTheme.colorScheme.primary
                    )

                    BenefitCard(
                        icon = Icons.Filled.Shield,
                        title = "Complete Privacy",
                        description = "All data is encrypted and stored only on your device. No accounts, no cloud, no analytics. Zero network connections — ever.",
                        accent = MaterialTheme.colorScheme.secondary
                    )

                    BenefitCard(
                        icon = Icons.Filled.Lock,
                        title = "Protected Access",
                        description = "PIN lock keeps your data safe. Screenshot prevention and notification privacy ensure no one sees your information without consent.",
                        accent = MaterialTheme.colorScheme.tertiary
                    )

                    BenefitCard(
                        icon = Icons.Filled.Notifications,
                        title = "Gentle Reminders",
                        description = "Get notified before your period, PMS, and fertile window. Daily log reminders help you track consistently without stress.",
                        accent = MaterialTheme.colorScheme.primary
                    )

                    BenefitCard(
                        icon = Icons.Filled.Visibility,
                        title = "Health Education",
                        description = "Learn about your cycle phases, fertility signs, and wellness tips from medically-reviewed content built right into the app.",
                        accent = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom continue button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Continue to CycleSync",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun BenefitCard(
    icon: ImageVector,
    title: String,
    description: String,
    accent: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }
        }
    }
}
