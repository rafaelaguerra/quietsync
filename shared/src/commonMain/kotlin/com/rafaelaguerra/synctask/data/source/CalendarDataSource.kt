package com.rafaelaguerra.synctask.data.source

import com.rafaelaguerra.synctask.domain.model.CalendarEvent

interface CalendarDataSource {
    suspend fun createEvent(event: CalendarEvent): Long
    suspend fun deleteEvent(eventId: Long)
}
