package com.rafaelaguerra.synctask.data.repository

import com.rafaelaguerra.synctask.data.source.AppManagedEventsStorage
import com.rafaelaguerra.synctask.data.source.CalendarDataSource
import com.rafaelaguerra.synctask.data.source.PhoneStateScheduler
import com.rafaelaguerra.synctask.domain.error.AppError
import com.rafaelaguerra.synctask.domain.error.asException
import com.rafaelaguerra.synctask.domain.model.AppManagedEvent
import com.rafaelaguerra.synctask.domain.model.CalendarEvent
import com.rafaelaguerra.synctask.domain.model.PhoneState
import com.rafaelaguerra.synctask.domain.repository.EventSyncRepository

class EventSyncRepositoryImpl(
    private val calendarDataSource: CalendarDataSource,
    private val phoneStateScheduler: PhoneStateScheduler,
    private val appManagedEventsStorage: AppManagedEventsStorage
) : EventSyncRepository {

    override suspend fun createCalendarEvent(event: CalendarEvent): Result<Long> {
        return runCatching {
            calendarDataSource.createEvent(event)
        }
    }

    override suspend fun schedulePhoneState(
        eventId: Long,
        startDateTimeMillis: Long,
        endDateTimeMillis: Long,
        phoneState: PhoneState
    ): Result<Unit> {
        return runCatching {
            phoneStateScheduler.schedulePhoneStateChange(
                eventId = eventId,
                startDateTimeMillis = startDateTimeMillis,
                endDateTimeMillis = endDateTimeMillis,
                phoneState = phoneState
            )
        }
    }

    override suspend fun saveAppManagedEvent(
        eventId: Long,
        title: String,
        phoneState: PhoneState,
        startDateTimeMillis: Long,
        endDateTimeMillis: Long,
        createdAtMillis: Long
    ): Result<Unit> {
        return runCatching {
            appManagedEventsStorage.save(
                AppManagedEvent(
                    eventId = eventId,
                    title = title,
                    phoneState = phoneState,
                    startDateTimeMillis = startDateTimeMillis,
                    endDateTimeMillis = endDateTimeMillis,
                    createdAtMillis = createdAtMillis
                )
            )
        }
    }

    override suspend fun getAppManagedEvents(): Result<List<AppManagedEvent>> {
        return runCatching {
            appManagedEventsStorage.getAll()
        }
    }

    override suspend fun cleanupExpiredManagedEvents(nowMillis: Long): Result<Unit> {
        return runCatching {
            val expiredEvents = appManagedEventsStorage.getAll()
                .filter { event ->
                    event.endDateTimeMillis > 0L && event.endDateTimeMillis <= nowMillis
                }

            expiredEvents.forEach { event ->
                phoneStateScheduler.cancelPhoneStateChange(event.eventId)
                appManagedEventsStorage.remove(event.eventId)
            }
        }
    }

    override suspend fun removeAppManagedEvent(eventId: Long): Result<Unit> {
        return runCatching {
            phoneStateScheduler.cancelPhoneStateChange(eventId)
            calendarDataSource.deleteEvent(eventId)
            appManagedEventsStorage.remove(eventId)
        }
    }

    override suspend fun updateAppManagedEventPhoneState(
        eventId: Long,
        phoneState: PhoneState
    ): Result<AppManagedEvent> {
        return runCatching {
            val existingEvent = appManagedEventsStorage.getAll()
                .firstOrNull { it.eventId == eventId }
                ?: throw AppError.EventNotFound.asException()

            phoneStateScheduler.cancelPhoneStateChange(eventId)
            phoneStateScheduler.schedulePhoneStateChange(
                eventId = existingEvent.eventId,
                startDateTimeMillis = existingEvent.startDateTimeMillis,
                endDateTimeMillis = existingEvent.endDateTimeMillis,
                phoneState = phoneState
            )

            val updatedEvent = existingEvent.copy(phoneState = phoneState)
            appManagedEventsStorage.save(updatedEvent)
            updatedEvent
        }
    }
}
