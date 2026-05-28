package com.rafaelaguerra.synctask.domain.usecase

import com.rafaelaguerra.synctask.domain.repository.EventSyncRepository

class CleanupExpiredManagedEventsUseCase(
    private val repository: EventSyncRepository
) {
    suspend operator fun invoke(nowMillis: Long = System.currentTimeMillis()): Result<Unit> {
        return repository.cleanupExpiredManagedEvents(nowMillis)
    }
}
