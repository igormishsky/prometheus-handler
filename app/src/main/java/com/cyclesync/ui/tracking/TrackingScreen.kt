package com.cyclesync.ui.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyclesync.core.constants.AppColors
import com.cyclesync.domain.entity.SkipReason
import com.cyclesync.domain.entity.TrackingCategory
import com.cyclesync.domain.entity.TrackingSubcategories
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TrackingScreen(
    date: String,
    onNavigateBack: () -> Unit,
    viewModel: TrackingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(state.date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Period toggle
            Card(
                isPeriod = state.isPeriodDay,
                onToggle = { viewModel.togglePeriod(it) },
                flowIntensity = state.flowIntensity,
                onFlowChange = { viewModel.setFlowIntensity(it) }
            )

            // Irregular cycle toggle (shows when period is on)
            if (state.isPeriodDay) {
                Spacer(modifier = Modifier.height(8.dp))
                IrregularCycleCard(
                    isIrregular = state.isIrregular,
                    skipReason = state.skipReason,
                    onToggleIrregular = { viewModel.toggleIrregular(it) },
                    onSelectReason = { viewModel.setSkipReason(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pain section
            TrackingSection(
                title = "Pain",
                items = TrackingSubcategories.pain,
                category = TrackingCategory.PAIN,
                selectedEntries = state.entries,
                onToggle = { viewModel.toggleEntry(TrackingCategory.PAIN, it) }
            )

            // Mood section
            TrackingSection(
                title = "Mood",
                items = TrackingSubcategories.mood,
                category = TrackingCategory.MOOD,
                selectedEntries = state.entries,
                onToggle = { viewModel.toggleEntry(TrackingCategory.MOOD, it) }
            )

            // Energy section
            TrackingSection(
                title = "Energy",
                items = TrackingSubcategories.energy,
                category = TrackingCategory.ENERGY,
                selectedEntries = state.entries,
                onToggle = { viewModel.toggleEntry(TrackingCategory.ENERGY, it) }
            )

            // Skin section
            TrackingSection(
                title = "Skin",
                items = TrackingSubcategories.skin,
                category = TrackingCategory.SKIN,
                selectedEntries = state.entries,
                onToggle = { viewModel.toggleEntry(TrackingCategory.SKIN, it) }
            )

            // Mental section
            TrackingSection(
                title = "Mental",
                items = TrackingSubcategories.mental,
                category = TrackingCategory.MENTAL,
                selectedEntries = state.entries,
                onToggle = { viewModel.toggleEntry(TrackingCategory.MENTAL, it) }
            )

            // Sleep section
            TrackingSection(
                title = "Sleep Quality",
                items = TrackingSubcategories.sleep["quality"] ?: emptyList(),
                category = TrackingCategory.SLEEP,
                selectedEntries = state.entries,
                onToggle = { viewModel.toggleEntry(TrackingCategory.SLEEP, it) }
            )

            TrackingSection(
                title = "Sleep Issues",
                items = TrackingSubcategories.sleep["symptoms"] ?: emptyList(),
                category = TrackingCategory.SLEEP,
                selectedEntries = state.entries,
                onToggle = { viewModel.toggleEntry(TrackingCategory.SLEEP, it) }
            )

            // Exercise section
            TrackingSection(
                title = "Exercise Type",
                items = TrackingSubcategories.exercise["type"] ?: emptyList(),
                category = TrackingCategory.EXERCISE,
                selectedEntries = state.entries,
                onToggle = { viewModel.toggleEntry(TrackingCategory.EXERCISE, it) }
            )

            TrackingSection(
                title = "Exercise Intensity",
                items = TrackingSubcategories.exercise["intensity"] ?: emptyList(),
                category = TrackingCategory.EXERCISE,
                selectedEntries = state.entries,
                onToggle = { viewModel.toggleEntry(TrackingCategory.EXERCISE, it) }
            )

            // Digestion symptoms
            TrackingSection(
                title = "Digestion",
                items = TrackingSubcategories.digestion["symptoms"] ?: emptyList(),
                category = TrackingCategory.DIGESTION,
                selectedEntries = state.entries,
                onToggle = { viewModel.toggleEntry(TrackingCategory.DIGESTION, it) }
            )

            // Appetite & Cravings
            TrackingSection(
                title = "Appetite",
                items = TrackingSubcategories.digestion["appetite"] ?: emptyList(),
                category = TrackingCategory.DIGESTION,
                selectedEntries = state.entries,
                onToggle = { viewModel.toggleEntry(TrackingCategory.DIGESTION, it) }
            )

            TrackingSection(
                title = "Cravings",
                items = TrackingSubcategories.digestion["cravings"] ?: emptyList(),
                category = TrackingCategory.DIGESTION,
                selectedEntries = state.entries,
                onToggle = { viewModel.toggleEntry(TrackingCategory.DIGESTION, it) }
            )

            // Social section
            TrackingSection(
                title = "Social",
                items = TrackingSubcategories.social,
                category = TrackingCategory.SOCIAL,
                selectedEntries = state.entries,
                onToggle = { viewModel.toggleEntry(TrackingCategory.SOCIAL, it) }
            )

            // Hair section
            TrackingSection(
                title = "Hair",
                items = TrackingSubcategories.hair,
                category = TrackingCategory.HAIR,
                selectedEntries = state.entries,
                onToggle = { viewModel.toggleEntry(TrackingCategory.HAIR, it) }
            )

            // Discharge section
            TrackingSection(
                title = "Discharge Type",
                items = TrackingSubcategories.discharge["type"] ?: emptyList(),
                category = TrackingCategory.DISCHARGE,
                selectedEntries = state.entries,
                onToggle = { viewModel.toggleEntry(TrackingCategory.DISCHARGE, it) }
            )

            TrackingSection(
                title = "Discharge Amount",
                items = TrackingSubcategories.discharge["amount"] ?: emptyList(),
                category = TrackingCategory.DISCHARGE,
                selectedEntries = state.entries,
                onToggle = { viewModel.toggleEntry(TrackingCategory.DISCHARGE, it) }
            )

            // Sex section
            TrackingSection(
                title = "Libido",
                items = TrackingSubcategories.sex["libido"] ?: emptyList(),
                category = TrackingCategory.SEX,
                selectedEntries = state.entries,
                onToggle = { viewModel.toggleEntry(TrackingCategory.SEX, it) }
            )

            // Stool (Bristol Scale)
            TrackingSection(
                title = "Stool (Bristol Scale)",
                items = TrackingSubcategories.bristolStool,
                category = TrackingCategory.STOOL,
                selectedEntries = state.entries,
                onToggle = { viewModel.toggleEntry(TrackingCategory.STOOL, it) }
            )

            // Notes
            Spacer(modifier = Modifier.height(16.dp))
            Text("Notes", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.notes,
                onValueChange = { viewModel.setNotes(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder = { Text("How are you feeling today?") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save button
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Save", modifier = Modifier.padding(vertical = 8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun Card(
    isPeriod: Boolean,
    onToggle: (Boolean) -> Unit,
    flowIntensity: String?,
    onFlowChange: (String) -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPeriod) AppColors.Menstruation.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Period",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isPeriod) AppColors.Menstruation else MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = isPeriod,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = AppColors.Menstruation
                    )
                )
            }

            if (isPeriod) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Flow", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("light", "medium", "heavy", "super_heavy").forEach { flow ->
                        val isSelected = flowIntensity == flow
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) AppColors.Menstruation
                                    else MaterialTheme.colorScheme.surface
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) AppColors.Menstruation else MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onFlowChange(flow) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = flow.replace("_", " ").replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IrregularCycleCard(
    isIrregular: Boolean,
    skipReason: SkipReason?,
    onToggleIrregular: (Boolean) -> Unit,
    onSelectReason: (SkipReason?) -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isIrregular) AppColors.Warning.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mark as irregular",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isIrregular) AppColors.Warning else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Exclude from predictions (illness, stress, etc.)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isIrregular,
                    onCheckedChange = onToggleIrregular,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = AppColors.Warning
                    )
                )
            }

            if (isIrregular) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Reason", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SkipReason.entries.forEach { reason ->
                        val isSelected = skipReason == reason
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) AppColors.Warning
                                    else MaterialTheme.colorScheme.surface
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) AppColors.Warning else MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectReason(if (isSelected) null else reason) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = reason.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrackingSection(
    title: String,
    items: List<String>,
    category: TrackingCategory,
    selectedEntries: Map<String, com.cyclesync.domain.entity.TrackingEntry>,
    onToggle: (String) -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(title, style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            val key = "${category.name}_$item"
            val isSelected = selectedEntries.containsKey(key)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onToggle(item) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = item.replace("_", " ").replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
