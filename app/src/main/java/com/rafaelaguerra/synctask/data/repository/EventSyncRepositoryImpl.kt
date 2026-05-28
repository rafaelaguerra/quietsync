package com.rafaelaguerra.synctask.data.repository

import com.rafaelaguerra.synctask.data.calendar.CalendarLocalDataSource
import com.rafaelaguerra.synctask.data.device.PhoneStateAlarmScheduler
import com.rafaelaguerra.synctask.data.local.AppManagedEventsLocalDataSource
import com.rafaelaguerra.synctask.domain.error.AppError
import com.rafaelaguerra.synctask.domain.error.asException
import com.rafaelaguerra.synctask.domain.model.AppManagedEvent
import com.rafaelaguerra.synctask.domain.model.CalendarEvent
import com.rafaelaguerra.synctask.domain.model.PhoneState
import com.rafaelaguerra.synctask.domain.repository.EventSyncRepository

class EventSyncRepositoryImpl(
    private val calendarLocalDataSource: CalendarLocalDataSource,
    private val phoneStateAlarmScheduler: PhoneStateAlarmScheduler,
    private val appManagedEventsLocalDataSource: AppManagedEventsLocalDataSource
) : EventSyncRepository {

    override suspend fun createCalendarEvent(event: CalendarEvent): Result<Long> {
        return runCatching {
            calendarLocalDataSource.createEvent(event)
        }
    }

    override suspend fun schedulePhoneState(
        eventId: Long,
        startDateTimeMillis: Long,
        endDateTimeMillis: Long,
        phoneState: PhoneState
    ): Result<Unit> {
        return runCatching {
            phoneStateAlarmScheduler.schedulePhoneStateChange(
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
            appManagedEventsLocalDataSource.save(
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
            appManagedEventsLocalDataSource.getAll()
        }
    }

    override suspend fun cleanupExpiredManagedEvents(nowMillis: Long): Result<Unit> {
        return runCatching {
            val expiredEvents = appManagedEventsLocalDataSource.getAll()
                .filter { event ->
                    event.endDateTimeMillis > 0L && event.endDateTimeMillis <= nowMillis
                }

            expiredEvents.forEach { event ->
                phoneStateAlarmScheduler.cancelPhoneStateChange(event.eventId)
                appManagedEventsLocalDataSource.remove(event.eventId)
            }
        }
    }

    override suspend fun removeAppManagedEvent(eventId: Long): Result<Unit> {
        return runCatching {
            phoneStateAlarmScheduler.cancelPhoneStateChange(eventId)
            calendarLocalDataSource.deleteEvent(eventId)
            appManagedEventsLocalDataSource.remove(eventId)
        }
    }

    override suspend fun updateAppManagedEventPhoneState(
        eventId: Long,
        phoneState: PhoneState
    ): Result<AppManagedEvent> {
        return runCatching {
            val existingEvent = appManagedEventsLocalDataSource.getAll()
                .firstOrNull { it.eventId == eventId }
                ?: throw AppError.EventNotFound.asException()

            phoneStateAlarmScheduler.cancelPhoneStateChange(eventId)
            phoneStateAlarmScheduler.schedulePhoneStateChange(
                eventId = existingEvent.eventId,
                startDateTimeMillis = existingEvent.startDateTimeMillis,
                endDateTimeMillis = existingEvent.endDateTimeMillis,
                phoneState = phoneState
            )

            val updatedEvent = existingEvent.copy(phoneState = phoneState)
            appManagedEventsLocalDataSource.save(updatedEvent)
            updatedEvent
        }
    }
}
