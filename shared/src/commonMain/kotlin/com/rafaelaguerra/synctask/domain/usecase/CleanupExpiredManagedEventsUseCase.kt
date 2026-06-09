package com.rafaelaguerra.synctask.domain.usecase

import com.rafaelaguerra.synctask.domain.repository.EventSyncRepository
import kotlinx.datetime.Clock

class CleanupExpiredManagedEventsUseCase(
    private val repository: EventSyncRepository
) {
    suspend operator fun invoke(
        nowMillis: Long = Clock.System.now().toEpochMilliseconds()
    ): Result<Unit> {
        return repository.cleanupExpiredManagedEvents(nowMillis)
    }
}
