package com.rafaelaguerra.synctask.domain.usecase

import com.rafaelaguerra.synctask.domain.model.AppManagedEvent
import com.rafaelaguerra.synctask.domain.model.PhoneState
import com.rafaelaguerra.synctask.domain.repository.EventSyncRepository

class UpdateAppManagedEventPhoneStateUseCase(
    private val repository: EventSyncRepository
) {
    suspend operator fun invoke(
        eventId: Long,
        phoneState: PhoneState
    ): Result<AppManagedEvent> {
        return repository.updateAppManagedEventPhoneState(eventId, phoneState)
    }
}
