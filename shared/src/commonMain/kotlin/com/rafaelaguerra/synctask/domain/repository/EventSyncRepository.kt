package com.rafaelaguerra.synctask.domain.repository

import com.rafaelaguerra.synctask.domain.model.AppManagedEvent
import com.rafaelaguerra.synctask.domain.model.CalendarEvent
import com.rafaelaguerra.synctask.domain.model.PhoneState

interface EventSyncRepository {
    suspend fun createCalendarEvent(event: CalendarEvent): Result<Long>

    suspend fun schedulePhoneState(
        eventId: Long,
        startDateTimeMillis: Long,
        endDateTimeMillis: Long,
        phoneState: PhoneState
    ): Result<Unit>

    suspend fun saveAppManagedEvent(
        eventId: Long,
        title: String,
        phoneState: PhoneState,
        startDateTimeMillis: Long,
        endDateTimeMillis: Long,
        createdAtMillis: Long
    ): Result<Unit>

    suspend fun getAppManagedEvents(): Result<List<AppManagedEvent>>

    suspend fun cleanupExpiredManagedEvents(nowMillis: Long): Result<Unit>

    suspend fun removeAppManagedEvent(eventId: Long): Result<Unit>

    suspend fun updateAppManagedEventPhoneState(
        eventId: Long,
        phoneState: PhoneState
    ): Result<AppManagedEvent>
}
