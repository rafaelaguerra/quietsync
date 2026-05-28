package com.rafaelaguerra.synctask.domain.usecase

import com.rafaelaguerra.synctask.domain.repository.EventSyncRepository

class RemoveAppManagedEventUseCase(
    private val repository: EventSyncRepository
) {
    suspend operator fun invoke(eventId: Long): Result<Unit> {
        return repository.removeAppManagedEvent(eventId)
    }
}
