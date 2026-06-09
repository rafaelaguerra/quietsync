package com.rafaelaguerra.synctask.data.device

import com.rafaelaguerra.synctask.data.source.PhoneStateScheduler
import com.rafaelaguerra.synctask.domain.model.PhoneState

/**
 * iOS does not allow apps to toggle ringer/DND like Android.
 * Scheduling is recorded locally so the shared domain flow succeeds.
 */
class NoOpPhoneStateScheduler : PhoneStateScheduler {
    override fun schedulePhoneStateChange(
        eventId: Long,
        startDateTimeMillis: Long,
        endDateTimeMillis: Long,
        phoneState: PhoneState
    ) = Unit

    override fun cancelPhoneStateChange(eventId: Long) = Unit
}
