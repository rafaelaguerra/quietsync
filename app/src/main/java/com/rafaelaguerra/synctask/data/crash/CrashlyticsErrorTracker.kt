package com.rafaelaguerra.synctask.data.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.rafaelaguerra.synctask.domain.error.ErrorTracker

/**
 * [ErrorTracker] backed by Firebase Crashlytics. Fatal/uncaught crashes (including
 * native crashes via the NDK module) are captured automatically once Crashlytics is
 * initialized; this class forwards the handled, non-fatal errors.
 */
class CrashlyticsErrorTracker(
    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
) : ErrorTracker {

    override fun recordError(throwable: Throwable, message: String?) {
        if (message != null) {
            crashlytics.log(message)
        }
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }
}
