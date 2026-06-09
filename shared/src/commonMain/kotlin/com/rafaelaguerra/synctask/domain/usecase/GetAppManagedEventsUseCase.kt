package com.rafaelaguerra.synctask.domain.usecase

import com.rafaelaguerra.synctask.domain.model.AppManagedEvent
import com.rafaelaguerra.synctask.domain.repository.EventSyncRepository

class GetAppManagedEventsUseCase(
    private val repository: EventSyncRepository
) {
    suspend operator fun invoke(): Result<List<AppManagedEvent>> {
        return repository.getAppManagedEvents()
    }
}
