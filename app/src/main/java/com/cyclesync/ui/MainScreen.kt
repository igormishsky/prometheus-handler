package com.cyclesync.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.cyclesync.ui.analysis.AnalysisScreen
import com.cyclesync.ui.calendar.CalendarScreen
import com.cyclesync.ui.content.ContentScreen
import com.cyclesync.ui.cycleview.CycleViewScreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MainScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToTracking: (String) -> Unit
) {
    val navItems = listOf(
        BottomNavItem("Cycle", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
        BottomNavItem("Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
        BottomNavItem("Analysis", Icons.Filled.Analytics, Icons.Outlined.Analytics),
        BottomNavItem("Learn", Icons.Filled.LibraryBooks, Icons.Outlined.LibraryBooks)
    )

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    onNavigateToTracking(today)
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log today")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> CycleViewScreen(
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToTracking = onNavigateToTracking
                )
                1 -> CalendarScreen(onNavigateToTracking = onNavigateToTracking)
                2 -> AnalysisScreen()
                3 -> ContentScreen()
            }
        }
    }
}
