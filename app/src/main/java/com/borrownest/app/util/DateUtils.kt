package com.borrownest.app.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Focused, crash-safe date helpers. These operate on calendar dates only
 * (LocalDate) and never throw into the Compose UI: parsing failures return null.
 */
object DateUtils {

    private val ISO_DATE: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val ISO_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val DISPLAY_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    private val DISPLAY_DATE_SHORT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")

    /** Today's date, always read from the local device clock. */
    fun today(): LocalDate = LocalDate.now()

    /** Parse a YYYY-MM-DD string. Returns null on blank or malformed input. */
    fun parse(value: String?): LocalDate? {
        if (value.isNullOrBlank()) return null
        return try {
            LocalDate.parse(value.trim(), ISO_DATE)
        } catch (e: Exception) {
            null
        }
    }

    fun isValid(value: String?): Boolean = parse(value) != null

    /** True only when the string is non-blank AND fails to parse. */
    fun isNonBlankButInvalid(value: String?): Boolean =
        !value.isNullOrBlank() && parse(value) == null

    fun format(date: LocalDate): String = date.format(ISO_DATE)

    fun nowTimestamp(): String = LocalDateTime.now().toString()

    fun currentTimeHhmm(): String = LocalDateTime.now().format(ISO_TIME)

    /** Human-friendly full date, or a fallback string. */
    fun displayDate(value: String?, fallback: String = "—"): String {
        val d = parse(value) ?: return fallback
        return d.format(DISPLAY_DATE)
    }

    fun displayDateShort(value: String?, fallback: String = "—"): String {
        val d = parse(value) ?: return fallback
        return d.format(DISPLAY_DATE_SHORT)
    }

    /** Positive when the date is in the future, negative when in the past. */
    fun daysUntil(target: String?, from: LocalDate = today()): Long? {
        val t = parse(target) ?: return null
        return ChronoUnit.DAYS.between(from, t)
    }

    /** Positive count of days a date is past `from`. Null when not overdue/invalid. */
    fun daysOverdue(target: String?, from: LocalDate = today()): Long? {
        val t = parse(target) ?: return null
        val diff = ChronoUnit.DAYS.between(t, from)
        return if (diff > 0) diff else null
    }

    /**
     * Inclusive duration in days between two calendar dates.
     * Returns null if either date is missing/invalid or end precedes start.
     */
    fun durationDays(start: String?, end: String?): Long? {
        val s = parse(start) ?: return null
        val e = parse(end) ?: return null
        val diff = ChronoUnit.DAYS.between(s, e)
        return if (diff >= 0) diff else null
    }

    fun isBeforeToday(value: String?, from: LocalDate = today()): Boolean {
        val d = parse(value) ?: return false
        return d.isBefore(from)
    }

    fun isToday(value: String?, from: LocalDate = today()): Boolean {
        val d = parse(value) ?: return false
        return d.isEqual(from)
    }

    /** Year-month key "YYYY-MM" used for monthly grouping. Null if invalid. */
    fun yearMonthKey(value: String?): String? {
        val d = parse(value) ?: return null
        return "%04d-%02d".format(d.year, d.monthValue)
    }

    fun year(value: String?): Int? = parse(value)?.year
}
