package com.rafaelaguerra.synctask.data.device

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.rafaelaguerra.synctask.data.receiver.PhoneStateReceiver
import com.rafaelaguerra.synctask.domain.model.PhoneState

class PhoneStateAlarmScheduler(
    context: Context
) {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)

    fun schedulePhoneStateChange(
        eventId: Long,
        startDateTimeMillis: Long,
        endDateTimeMillis: Long,
        phoneState: PhoneState
    ) {
        require(endDateTimeMillis > startDateTimeMillis) {
            "La fecha de fin debe ser posterior a la de inicio."
        }

        val applyStateIntent = buildApplyStateIntent(eventId, phoneState)
        val restoreStateIntent = buildRestoreStateIntent(eventId)

        scheduleAlarm(startDateTimeMillis, applyStateIntent)
        scheduleAlarm(endDateTimeMillis, restoreStateIntent)
    }

    fun cancelPhoneStateChange(eventId: Long) {
        val applyStateIntent = buildApplyStateIntent(eventId, PhoneState.NORMAL)
        val restoreStateIntent = buildRestoreStateIntent(eventId)
        alarmManager?.cancel(applyStateIntent)
        alarmManager?.cancel(restoreStateIntent)
    }

    private fun scheduleAlarm(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (triggerAtMillis <= System.currentTimeMillis()) return
        alarmManager?.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    private fun buildApplyStateIntent(eventId: Long, phoneState: PhoneState): PendingIntent {
        val intent = Intent(appContext, PhoneStateReceiver::class.java).apply {
            action = PhoneStateReceiver.ACTION_APPLY_STATE
            putExtra(PhoneStateReceiver.EXTRA_EVENT_ID, eventId)
            putExtra(PhoneStateReceiver.EXTRA_PHONE_STATE, phoneState.name)
        }

        return PendingIntent.getBroadcast(
            appContext,
            requestCodeFor(eventId, isRestoreAction = false),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildRestoreStateIntent(eventId: Long): PendingIntent {
        val intent = Intent(appContext, PhoneStateReceiver::class.java).apply {
            action = PhoneStateReceiver.ACTION_RESTORE_STATE
            putExtra(PhoneStateReceiver.EXTRA_EVENT_ID, eventId)
        }

        return PendingIntent.getBroadcast(
            appContext,
            requestCodeFor(eventId, isRestoreAction = true),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun requestCodeFor(eventId: Long, isRestoreAction: Boolean): Int {
        val baseCode = (eventId % Int.MAX_VALUE).toInt()
        return if (isRestoreAction) baseCode + 1 else baseCode
    }
}
