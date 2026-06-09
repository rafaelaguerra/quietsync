package com.rafaelaguerra.synctask.domain.model

enum class RepeatPeriod { DAILY, WEEKLY, MONTHLY }

data class RecurrenceConfig(
    val daysOfWeek: Set<Weekday> = emptySet(),
    val period: RepeatPeriod = RepeatPeriod.WEEKLY
) {
    fun toRRule(): String {
        return when (period) {
            RepeatPeriod.DAILY -> "FREQ=DAILY"
            RepeatPeriod.WEEKLY -> {
                val days = daysOfWeek.map { it.rruleCode }.joinToString(",")
                if (days.isNotBlank()) "FREQ=WEEKLY;BYDAY=$days" else "FREQ=WEEKLY"
            }
            RepeatPeriod.MONTHLY -> "FREQ=MONTHLY"
        }
    }
}
