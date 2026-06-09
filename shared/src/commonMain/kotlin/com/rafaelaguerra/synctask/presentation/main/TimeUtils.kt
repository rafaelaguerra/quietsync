package com.rafaelaguerra.synctask.presentation.main

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

internal fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()

/**
 * Suggests the next :00 or :30 boundary in the user's local time, matching the
 * previous Calendar-based behavior but with kotlinx-datetime so it runs on both platforms.
 */
internal fun nextHalfHourMillis(nowMillis: Long = currentTimeMillis()): Long {
    val tz = TimeZone.currentSystemDefault()
    val ldt = Instant.fromEpochMilliseconds(nowMillis).toLocalDateTime(tz)
    return if (ldt.minute < 30) {
        LocalDateTime(ldt.year, ldt.monthNumber, ldt.dayOfMonth, ldt.hour, 30, 0, 0)
            .toInstant(tz)
            .toEpochMilliseconds()
    } else {
        LocalDateTime(ldt.year, ldt.monthNumber, ldt.dayOfMonth, ldt.hour, 0, 0, 0)
            .toInstant(tz)
            .plus(1, DateTimeUnit.HOUR, tz)
            .toEpochMilliseconds()
    }
}

/**
 * Material3's DatePicker works in UTC. Converts a local epoch-millis instant to the
 * UTC-midnight millis of the same calendar date so it preselects correctly.
 */
internal fun toUtcStartOfDayMillis(localMillis: Long): Long {
    val date = Instant.fromEpochMilliseconds(localMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
    return LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, 0, 0, 0, 0)
        .toInstant(TimeZone.UTC)
        .toEpochMilliseconds()
}

/** Rebuilds a local epoch-millis from a UTC-midnight date (from the picker) plus a local time. */
internal fun combineUtcDateWithLocalTime(utcDateMillis: Long, hour: Int, minute: Int): Long {
    val date = Instant.fromEpochMilliseconds(utcDateMillis)
        .toLocalDateTime(TimeZone.UTC)
        .date
    return LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, hour, minute, 0, 0)
        .toInstant(TimeZone.currentSystemDefault())
        .toEpochMilliseconds()
}

internal fun hourOf(localMillis: Long): Int =
    Instant.fromEpochMilliseconds(localMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .hour

internal fun minuteOf(localMillis: Long): Int =
    Instant.fromEpochMilliseconds(localMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .minute
