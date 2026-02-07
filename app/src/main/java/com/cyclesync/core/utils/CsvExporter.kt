package com.cyclesync.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.cyclesync.domain.entity.Cycle
import com.cyclesync.domain.entity.DailyLog
import java.io.File
import java.io.FileWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object CsvExporter {

    fun exportCyclesToCsv(context: Context, cycles: List<Cycle>, dailyLogs: List<DailyLog>): Uri? {
        val timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val fileName = "cyclesync_export_$timestamp.csv"
        val file = File(context.cacheDir, fileName)

        FileWriter(file).use { writer ->
            // Cycles section
            writer.append("=== CYCLES ===\n")
            writer.append("Cycle Number,Start Date,End Date,Period Start,Period End,Cycle Length,Period Length,Notes\n")
            cycles.sortedBy { it.startDate }.forEach { cycle ->
                writer.append(buildString {
                    append(cycle.cycleNumber).append(",")
                    append(cycle.startDate).append(",")
                    append(cycle.endDate ?: "").append(",")
                    append(cycle.periodStartDate).append(",")
                    append(cycle.periodEndDate ?: "").append(",")
                    append(cycle.cycleLength ?: "").append(",")
                    append(cycle.periodLength ?: "").append(",")
                    append(escapeCsv(cycle.notes ?: ""))
                    append("\n")
                })
            }

            writer.append("\n")

            // Daily logs section
            writer.append("=== DAILY LOGS ===\n")
            writer.append("Date,Cycle Day,Categories,Subcategories,Notes\n")
            dailyLogs.sortedBy { it.date }.forEach { log ->
                val categories = log.entries.map { it.category.displayName }.distinct().joinToString("; ")
                val subcategories = log.entries.joinToString("; ") { "${it.category.displayName}: ${it.subcategory.replace("_", " ")}" }
                writer.append(buildString {
                    append(log.date).append(",")
                    append(log.cycleDay ?: "").append(",")
                    append(escapeCsv(categories)).append(",")
                    append(escapeCsv(subcategories)).append(",")
                    append(escapeCsv(log.notes ?: ""))
                    append("\n")
                })
            }
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun createShareIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
