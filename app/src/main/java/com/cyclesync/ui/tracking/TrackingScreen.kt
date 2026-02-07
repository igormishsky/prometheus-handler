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

            // Digestion symptoms
            TrackingSection(
                title = "Digestion",
                items = TrackingSubcategories.digestion["symptoms"] ?: emptyList(),
                category = TrackingCategory.DIGESTION,
                selectedEntries = state.entries,
                onToggle = { viewModel.toggleEntry(TrackingCategory.DIGESTION, it) }
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
