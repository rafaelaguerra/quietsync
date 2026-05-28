package com.rafaelaguerra.synctask.domain.model

data class CalendarEvent(
    val title: String,
    val description: String,
    val location: String,
    val startDateTimeMillis: Long,
    val endDateTimeMillis: Long,
    val phoneState: PhoneState = PhoneState.NORMAL,
    val recurrenceConfig: RecurrenceConfig? = null
)
