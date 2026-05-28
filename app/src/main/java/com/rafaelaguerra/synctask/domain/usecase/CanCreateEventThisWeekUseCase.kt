package com.rafaelaguerra.synctask.domain.usecase

import com.rafaelaguerra.synctask.domain.model.AppManagedEvent
import java.util.Calendar

class CanCreateEventThisWeekUseCase {
    operator fun invoke(
        isPremium: Boolean,
        events: List<AppManagedEvent>,
        nowMillis: Long = System.currentTimeMillis(),
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
        val calendar = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val deltaToMonday = (7 + (dayOfWeek - Calendar.MONDAY)) % 7
        calendar.add(Calendar.DAY_OF_YEAR, -deltaToMonday)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    companion object {
        const val FREE_WEEKLY_LIMIT = 3
        private const val WEEK_MILLIS = 7 * 24 * 60 * 60 * 1000L
    }
}
