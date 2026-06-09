package com.rafaelaguerra.synctask.domain.usecase

import com.rafaelaguerra.synctask.domain.model.CalendarEvent
import com.rafaelaguerra.synctask.domain.repository.EventSyncRepository

class CreateCalendarEventUseCase(
    private val repository: EventSyncRepository
) {
    suspend operator fun invoke(event: CalendarEvent): Result<Long> {
        return repository.createCalendarEvent(event)
    }
}
