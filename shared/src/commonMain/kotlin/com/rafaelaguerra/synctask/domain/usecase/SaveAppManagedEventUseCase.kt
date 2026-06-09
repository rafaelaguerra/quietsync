package com.rafaelaguerra.synctask.domain.usecase

import com.rafaelaguerra.synctask.domain.model.PhoneState
import com.rafaelaguerra.synctask.domain.repository.EventSyncRepository

class SaveAppManagedEventUseCase(
    private val repository: EventSyncRepository
) {
    suspend operator fun invoke(
        eventId: Long,
        title: String,
        phoneState: PhoneState,
        startDateTimeMillis: Long,
        endDateTimeMillis: Long,
        createdAtMillis: Long
    ): Result<Unit> {
        return repository.saveAppManagedEvent(
            eventId = eventId,
            title = title,
            phoneState = phoneState,
            startDateTimeMillis = startDateTimeMillis,
            endDateTimeMillis = endDateTimeMillis,
            createdAtMillis = createdAtMillis
        )
    }
}
