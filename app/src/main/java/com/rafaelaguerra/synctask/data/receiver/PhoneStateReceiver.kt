package com.rafaelaguerra.synctask.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rafaelaguerra.synctask.data.local.AppManagedEventsLocalDataSource
import com.rafaelaguerra.synctask.data.device.PhoneStateController
import com.rafaelaguerra.synctask.domain.model.PhoneState

class PhoneStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1L)
        if (eventId == -1L) return

        val stateController = PhoneStateController(context)
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val previousStateKey = previousStateKey(eventId)

        when (intent.action) {
            ACTION_APPLY_STATE -> {
                val requestedStateName = intent.getStringExtra(EXTRA_PHONE_STATE) ?: return
                val requestedState = runCatching { PhoneState.valueOf(requestedStateName) }
                    .getOrNull() ?: return

                val previousState = stateController.getCurrentState()
                preferences.edit()
                    .putString(previousStateKey, previousState.name)
                    .apply()

                runCatching {
                    stateController.applyState(requestedState)
                }
            }

            ACTION_RESTORE_STATE -> {
                val previousState = preferences.getString(previousStateKey, null)
                    ?.let { value -> runCatching { PhoneState.valueOf(value) }.getOrNull() }
                    ?: PhoneState.NORMAL

                runCatching {
                    stateController.applyState(previousState)
                }
                preferences.edit().remove(previousStateKey).apply()
                runCatching {
                    AppManagedEventsLocalDataSource(context).remove(eventId)
                }
            }
        }
    }

    private fun previousStateKey(eventId: Long): String {
        return "previous_state_$eventId"
    }

    companion object {
        const val ACTION_APPLY_STATE =
            "com.rafaelaguerra.synctask.action.APPLY_PHONE_STATE"
        const val ACTION_RESTORE_STATE =
            "com.rafaelaguerra.synctask.action.RESTORE_PHONE_STATE"

        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_PHONE_STATE = "extra_phone_state"

        private const val PREFERENCES_NAME = "phone_state_scheduler"
    }
}
