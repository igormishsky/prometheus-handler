package com.cyclesync.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.cyclesync.domain.entity.Cycle
import com.cyclesync.domain.entity.DailyLog
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object CsvExporter {

    private const val MAX_CSV_FIELD_LENGTH = 10_000
    private const val FILE_PREFIX = "cyclesync_export_"
    private const val FILE_EXTENSION = ".csv"
    private const val CSV_MIME_TYPE = "text/csv"

    fun exportCyclesToCsv(context: Context, cycles: List<Cycle>, dailyLogs: List<DailyLog>): Uri? {
        return try {
            val timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val fileName = "$FILE_PREFIX$timestamp$FILE_EXTENSION"
            val file = File(context.cacheDir, fileName)

            cleanupOldExports(context)

            FileWriter(file).use { writer ->
                writeCyclesSection(writer, cycles)
                writer.append("\n")
                writeDailyLogsSection(writer, dailyLogs)
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (_: IOException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    fun createShareIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = CSV_MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun writeCyclesSection(writer: FileWriter, cycles: List<Cycle>) {
        writer.append("=== CYCLES ===\n")
        writer.append("Cycle Number,Start Date,End Date,Period Start,Period End,Cycle Length,Period Length,Excluded,Skip Reason,Notes\n")
        cycles.sortedBy { it.startDate }.forEach { cycle ->
            writer.append(buildString {
                append(cycle.cycleNumber).append(",")
                append(cycle.startDate).append(",")
                append(cycle.endDate ?: "").append(",")
                append(cycle.periodStartDate).append(",")
                append(cycle.periodEndDate ?: "").append(",")
                append(cycle.cycleLength ?: "").append(",")
                append(cycle.periodLength ?: "").append(",")
                append(if (cycle.isExcluded) "Yes" else "No").append(",")
                append(escapeCsv(cycle.skipReason?.displayName ?: "")).append(",")
                append(escapeCsv(truncateField(cycle.notes ?: "")))
                append("\n")
            })
        }
    }

    private fun writeDailyLogsSection(writer: FileWriter, dailyLogs: List<DailyLog>) {
        writer.append("=== DAILY LOGS ===\n")
        writer.append("Date,Cycle Day,Categories,Subcategories,Notes\n")
        dailyLogs.sortedBy { it.date }.forEach { log ->
            val categories = log.entries
                .map { it.category.displayName }
                .distinct()
                .joinToString("; ")
            val subcategories = log.entries.joinToString("; ") {
                "${it.category.displayName}: ${it.subcategory.replace("_", " ")}"
            }
            writer.append(buildString {
                append(log.date).append(",")
                append(log.cycleDay ?: "").append(",")
                append(escapeCsv(categories)).append(",")
                append(escapeCsv(truncateField(subcategories))).append(",")
                append(escapeCsv(truncateField(log.notes ?: "")))
                append("\n")
            })
        }
    }

    private fun cleanupOldExports(context: Context) {
        context.cacheDir.listFiles()?.filter {
            it.name.startsWith(FILE_PREFIX) && it.name.endsWith(FILE_EXTENSION)
        }?.forEach { file ->
            try {
                file.delete()
            } catch (_: SecurityException) {
                // Ignore cleanup failures
            }
        }
    }

    private fun truncateField(value: String): String {
        return if (value.length > MAX_CSV_FIELD_LENGTH) {
            value.take(MAX_CSV_FIELD_LENGTH) + "..."
        } else {
            value
        }
    }

    internal fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
