package com.rafaelaguerra.synctask.domain.model

import java.util.Calendar

enum class RepeatPeriod { DAILY, WEEKLY, MONTHLY }

data class RecurrenceConfig(
    val daysOfWeek: Set<Int> = emptySet(), // Calendar.MONDAY … Calendar.SUNDAY
    val period: RepeatPeriod = RepeatPeriod.WEEKLY
) {
    fun toRRule(): String {
        return when (period) {
            RepeatPeriod.DAILY -> "FREQ=DAILY"
            RepeatPeriod.WEEKLY -> {
                val days = daysOfWeek.mapNotNull { calendarDayToRrule(it) }.joinToString(",")
                if (days.isNotBlank()) "FREQ=WEEKLY;BYDAY=$days" else "FREQ=WEEKLY"
            }
            RepeatPeriod.MONTHLY -> "FREQ=MONTHLY"
        }
    }

    companion object {
        fun calendarDayToRrule(calendarDay: Int): String? = when (calendarDay) {
            Calendar.MONDAY    -> "MO"
            Calendar.TUESDAY   -> "TU"
            Calendar.WEDNESDAY -> "WE"
            Calendar.THURSDAY  -> "TH"
            Calendar.FRIDAY    -> "FR"
            Calendar.SATURDAY  -> "SA"
            Calendar.SUNDAY    -> "SU"
            else               -> null
        }
    }
}
