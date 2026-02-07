package com.cyclesync.core.utils

import org.junit.Assert.*
import org.junit.Test

class CsvExporterTest {

    @Test
    fun `escapeCsv returns value unchanged when no special characters`() {
        assertEquals("hello", CsvExporter.escapeCsv("hello"))
    }

    @Test
    fun `escapeCsv wraps value with commas in quotes`() {
        assertEquals("\"hello, world\"", CsvExporter.escapeCsv("hello, world"))
    }

    @Test
    fun `escapeCsv escapes double quotes`() {
        assertEquals("\"say \"\"hello\"\"\"", CsvExporter.escapeCsv("say \"hello\""))
    }

    @Test
    fun `escapeCsv wraps value with newlines in quotes`() {
        assertEquals("\"line1\nline2\"", CsvExporter.escapeCsv("line1\nline2"))
    }

    @Test
    fun `escapeCsv wraps value with carriage return in quotes`() {
        assertEquals("\"line1\rline2\"", CsvExporter.escapeCsv("line1\rline2"))
    }

    @Test
    fun `escapeCsv handles empty string`() {
        assertEquals("", CsvExporter.escapeCsv(""))
    }

    @Test
    fun `escapeCsv handles value with all special characters`() {
        val input = "has, \"quotes\" and\nnewlines"
        val result = CsvExporter.escapeCsv(input)
        assertTrue(result.startsWith("\""))
        assertTrue(result.endsWith("\""))
    }

    @Test
    fun `escapeCsv preserves normal text`() {
        val input = "Normal text without special chars 123"
        assertEquals(input, CsvExporter.escapeCsv(input))
    }
}
