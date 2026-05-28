package com.rafaelaguerra.synctask.domain.model

data class AppManagedEvent(
    val eventId: Long,
    val title: String,
    val phoneState: PhoneState,
    val startDateTimeMillis: Long,
    val endDateTimeMillis: Long,
    val createdAtMillis: Long
)
