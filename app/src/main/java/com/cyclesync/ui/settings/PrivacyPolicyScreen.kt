package com.cyclesync.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
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
            Text(
                text = "CycleSync Privacy Policy",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Last updated: February 2026",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
            PolicySection(
                title = "Overview",
                body = "CycleSync is a privacy-first menstrual cycle tracking application. " +
                        "We believe your most intimate health data belongs to you and only you. " +
                        "CycleSync is designed from the ground up to ensure your data never leaves your device."
            )

            PolicySection(
                title = "Data Collection",
                body = "CycleSync does NOT collect, transmit, or share any personal data. " +
                        "The app has no internet permission and cannot connect to any server. " +
                        "All data you enter \u2014 cycle dates, symptoms, mood, notes, and predictions \u2014 " +
                        "is stored exclusively on your device in an encrypted database."
            )

            PolicySection(
                title = "Data Storage & Encryption",
                body = "All data is stored locally on your device using AES-256 encrypted database storage (SQLCipher). " +
                        "Encryption keys are managed by the Android Keystore system. " +
                        "No data is backed up to cloud services \u2014 automatic backup is explicitly disabled."
            )

            PolicySection(
                title = "Third-Party Services",
                body = "CycleSync uses zero third-party services. There are no analytics, " +
                        "no crash reporting services, no advertising SDKs, and no tracking of any kind. " +
                        "The app contains no third-party code that transmits data."
            )

            PolicySection(
                title = "Permissions",
                body = "CycleSync requests only the following permissions:\n\n" +
                        "\u2022 Notifications \u2014 To send local cycle reminders you configure\n" +
                        "\u2022 Exact Alarms \u2014 To schedule reminders at the correct time\n" +
                        "\u2022 Boot Completed \u2014 To restore your reminder schedule after device restart\n" +
                        "\u2022 Vibrate \u2014 For haptic feedback on notifications\n\n" +
                        "CycleSync does NOT request internet, camera, location, contacts, or any other sensitive permission."
            )

            PolicySection(
                title = "Data Export & Deletion",
                body = "You can export all your data as a CSV file at any time from Settings. " +
                        "You can permanently delete all data from Settings > Delete All Data. " +
                        "Once deleted, data cannot be recovered as there are no server-side copies."
            )

            PolicySection(
                title = "Children\u2019s Privacy",
                body = "CycleSync does not knowingly collect information from children under 13. " +
                        "The app does not collect any information from any user regardless of age."
            )

            PolicySection(
                title = "Changes to This Policy",
                body = "Any changes to this privacy policy will be reflected in app updates. " +
                        "Since CycleSync collects no data, the fundamental privacy guarantee \u2014 " +
                        "your data never leaves your device \u2014 will never change."
            )

            PolicySection(
                title = "Contact",
                body = "If you have questions about this privacy policy, please reach out through " +
                        "the app\u2019s listing page on the Google Play Store."
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PolicySection(title: String, body: String) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = body,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}
