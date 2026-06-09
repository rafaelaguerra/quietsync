package com.rafaelaguerra.synctask.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppManagedEvent(
    val eventId: Long,
    val title: String,
    val phoneState: PhoneState,
    val startDateTimeMillis: Long,
    val endDateTimeMillis: Long,
    val createdAtMillis: Long
)
