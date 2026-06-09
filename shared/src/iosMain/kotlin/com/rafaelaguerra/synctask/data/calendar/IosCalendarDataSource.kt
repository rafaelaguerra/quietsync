@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.rafaelaguerra.synctask.data.calendar

import com.rafaelaguerra.synctask.data.source.CalendarDataSource
import com.rafaelaguerra.synctask.domain.error.AppError
import com.rafaelaguerra.synctask.domain.error.asException
import com.rafaelaguerra.synctask.domain.model.CalendarEvent
import com.rafaelaguerra.synctask.domain.model.PhoneState
import com.rafaelaguerra.synctask.platform.IosCalendarAccess
import kotlinx.datetime.Clock
import platform.EventKit.EKEntityType
import platform.EventKit.EKEvent
import platform.EventKit.EKEventStore
import platform.EventKit.EKSpan
import platform.Foundation.NSDate
import platform.Foundation.NSUserDefaults
import platform.Foundation.dateWithTimeIntervalSince1970

class IosCalendarDataSource : CalendarDataSource {
    private val eventStore = EKEventStore()
    private val defaults = NSUserDefaults.standardUserDefaults

    override suspend fun createEvent(event: CalendarEvent): Long {
        ensureCalendarAccess()

        val userDescription = event.description.trim()
        val description = if (userDescription.isEmpty()) {
            QUIETSYNC_DESCRIPTION_PREFIX
        } else {
            "$QUIETSYNC_DESCRIPTION_PREFIX\n\n$userDescription"
        }

        val decoratedTitle = event.phoneState.titleEmoji()
            ?.let { "$it ${event.title}" }
            ?: event.title

        val ekEvent = EKEvent.eventWithEventStore(eventStore)
        ekEvent.title = decoratedTitle
        ekEvent.notes = description
        ekEvent.location = event.location
        ekEvent.startDate = NSDate.dateWithTimeIntervalSince1970(event.startDateTimeMillis / 1000.0)
        ekEvent.endDate = NSDate.dateWithTimeIntervalSince1970(event.endDateTimeMillis / 1000.0)
        ekEvent.calendar = eventStore.defaultCalendarForNewEvents

        val saved = eventStore.saveEvent(ekEvent, EKSpan.EKSpanThisEvent, commit = true, error = null)
        if (!saved) {
            throw AppError.CalendarCreateFailed.asException()
        }

        val identifier = ekEvent.eventIdentifier
            ?: throw AppError.CalendarEventIdUnavailable.asException()

        val eventId = Clock.System.now().toEpochMilliseconds()
        persistIdentifier(eventId, identifier)
        return eventId
    }

    override suspend fun deleteEvent(eventId: Long) {
        val identifier = loadIdentifier(eventId) ?: return
        val ekEvent = eventStore.eventWithIdentifier(identifier) ?: return
        eventStore.removeEvent(ekEvent, EKSpan.EKSpanThisEvent, commit = true, error = null)
        removeIdentifier(eventId)
    }

    private suspend fun ensureCalendarAccess() {
        if (!IosCalendarAccess.ensureGranted()) {
            throw AppError.CalendarNoWritableCalendar.asException()
        }
    }

    private fun persistIdentifier(eventId: Long, identifier: String) {
        defaults.setObject(identifier, keyFor(eventId))
    }

    private fun loadIdentifier(eventId: Long): String? {
        return defaults.stringForKey(keyFor(eventId))
    }

    private fun removeIdentifier(eventId: Long) {
        defaults.removeObjectForKey(keyFor(eventId))
    }

    private fun keyFor(eventId: Long): String = "$KEY_PREFIX$eventId"

    private companion object {
        const val QUIETSYNC_DESCRIPTION_PREFIX = "creado por QuietSync"
        const val KEY_PREFIX = "ios_calendar_event_id_"
    }
}

private fun PhoneState.titleEmoji(): String? = when (this) {
    PhoneState.NORMAL -> null
    PhoneState.VIBRATE -> "📳"
    PhoneState.SILENT -> "🔕"
    PhoneState.DO_NOT_DISTURB -> "🌙"
    PhoneState.AIRPLANE_MODE -> "✈️"
}
