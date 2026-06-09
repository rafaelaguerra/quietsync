package com.rafaelaguerra.synctask.data.source

import com.rafaelaguerra.synctask.domain.model.PhoneState

interface PhoneStateScheduler {
    fun schedulePhoneStateChange(
        eventId: Long,
        startDateTimeMillis: Long,
        endDateTimeMillis: Long,
        phoneState: PhoneState
    )

    fun cancelPhoneStateChange(eventId: Long)
}
