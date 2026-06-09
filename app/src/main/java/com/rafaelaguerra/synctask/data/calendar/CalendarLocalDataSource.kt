package com.rafaelaguerra.synctask.data.calendar

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.provider.CalendarContract
import com.rafaelaguerra.synctask.data.source.CalendarDataSource
import com.rafaelaguerra.synctask.domain.error.AppError
import com.rafaelaguerra.synctask.domain.error.asException
import com.rafaelaguerra.synctask.domain.model.CalendarEvent
import com.rafaelaguerra.synctask.domain.model.PhoneState
import java.util.TimeZone

class CalendarLocalDataSource(
    private val contentResolver: ContentResolver
) : CalendarDataSource {
    private companion object {
        const val QUIETSYNC_DESCRIPTION_PREFIX = "creado por QuietSync"
    }

    override suspend fun createEvent(event: CalendarEvent): Long = createEventInternal(event)

    override suspend fun deleteEvent(eventId: Long) = deleteEventInternal(eventId)

    private fun createEventInternal(event: CalendarEvent): Long {
        val calendarId = getWritableCalendarId()
        val userDescription = event.description.trim()
        val description = if (userDescription.isEmpty()) {
            QUIETSYNC_DESCRIPTION_PREFIX
        } else {
            "$QUIETSYNC_DESCRIPTION_PREFIX\n\n$userDescription"
        }

        // Prefix the calendar title with an emoji that reflects the device mode so
        // the user can recognise QuietSync events at a glance in Google Calendar.
        // No prefix for NORMAL — there is nothing automated to highlight.
        val decoratedTitle = event.phoneState.titleEmoji()
            ?.let { "$it ${event.title}" }
            ?: event.title

        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, decoratedTitle)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.EVENT_LOCATION, event.location)
            put(CalendarContract.Events.DTSTART, event.startDateTimeMillis)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)

            val recurrence = event.recurrenceConfig
            if (recurrence != null) {
                // Recurring events use RRULE + DURATION instead of DTEND
                val durationMillis = event.endDateTimeMillis - event.startDateTimeMillis
                val durationMinutes = durationMillis / 60_000
                put(CalendarContract.Events.DURATION, "PT${durationMinutes}M")
                put(CalendarContract.Events.RRULE, recurrence.toRRule())
            } else {
                put(CalendarContract.Events.DTEND, event.endDateTimeMillis)
            }
        }

        val eventUri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            ?: throw AppError.CalendarCreateFailed.asException()

        return eventUri.lastPathSegment?.toLongOrNull()
            ?: throw AppError.CalendarEventIdUnavailable.asException()
    }

    private fun deleteEventInternal(eventId: Long) {
        val eventUri = Uri.withAppendedPath(
            CalendarContract.Events.CONTENT_URI,
            eventId.toString()
        )
        contentResolver.delete(eventUri, null, null)
    }

    private fun getWritableCalendarId(): Long {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.IS_PRIMARY
        )
        val selection =
            "${CalendarContract.Calendars.VISIBLE} = 1 AND " +
                "${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ?"
        val selectionArgs = arrayOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR.toString())
        val sortOrder = "${CalendarContract.Calendars.IS_PRIMARY} DESC"

        contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
                return cursor.getLong(idIndex)
            }
        }

        throw AppError.CalendarNoWritableCalendar.asException()
    }
}

private fun PhoneState.titleEmoji(): String? = when (this) {
    PhoneState.NORMAL -> null
    PhoneState.VIBRATE -> "📳"
    PhoneState.SILENT -> "🔕"
    PhoneState.DO_NOT_DISTURB -> "🌙"
    PhoneState.AIRPLANE_MODE -> "✈️"
}
