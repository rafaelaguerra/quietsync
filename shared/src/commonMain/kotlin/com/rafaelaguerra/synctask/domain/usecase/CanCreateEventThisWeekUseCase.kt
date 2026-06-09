package com.rafaelaguerra.synctask.domain.usecase

import com.rafaelaguerra.synctask.domain.model.AppManagedEvent
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

class CanCreateEventThisWeekUseCase {
    operator fun invoke(
        isPremium: Boolean,
        events: List<AppManagedEvent>,
        nowMillis: Long = Clock.System.now().toEpochMilliseconds(),
        weeklyLimit: Int = FREE_WEEKLY_LIMIT
    ): Boolean {
        if (isPremium) return true

        val weekStart = weekStartMillis(nowMillis)
        val weekEnd = weekStart + WEEK_MILLIS
        val createdThisWeek = events.count { event ->
            event.createdAtMillis in weekStart until weekEnd
        }
        return createdThisWeek < weeklyLimit
    }

    private fun weekStartMillis(nowMillis: Long): Long {
        val localDate = Instant.fromEpochMilliseconds(nowMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
        val daysFromMonday = localDate.dayOfWeek.daysFromMonday()
        val monday = localDate.minus(DatePeriod(days = daysFromMonday))
        return monday.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }

    companion object {
        const val FREE_WEEKLY_LIMIT = 3
        private const val WEEK_MILLIS = 7 * 24 * 60 * 60 * 1000L

        private fun DayOfWeek.daysFromMonday(): Int = when (this) {
            DayOfWeek.MONDAY -> 0
            DayOfWeek.TUESDAY -> 1
            DayOfWeek.WEDNESDAY -> 2
            DayOfWeek.THURSDAY -> 3
            DayOfWeek.FRIDAY -> 4
            DayOfWeek.SATURDAY -> 5
            DayOfWeek.SUNDAY -> 6
        }
    }
}
