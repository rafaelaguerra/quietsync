package com.rafaelaguerra.synctask.domain.usecase

import com.rafaelaguerra.synctask.domain.model.PhoneState
import com.rafaelaguerra.synctask.domain.repository.EventSyncRepository

class SchedulePhoneStateForEventUseCase(
    private val repository: EventSyncRepository
) {
    suspend operator fun invoke(
        eventId: Long,
        startDateTimeMillis: Long,
        endDateTimeMillis: Long,
        phoneState: PhoneState
    ): Result<Unit> {
        return repository.schedulePhoneState(
            eventId = eventId,
            startDateTimeMillis = startDateTimeMillis,
            endDateTimeMillis = endDateTimeMillis,
            phoneState = phoneState
        )
    }
}
