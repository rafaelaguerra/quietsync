package com.rafaelaguerra.synctask.domain.error

/**
 * Platform-agnostic sink for non-fatal errors and diagnostic breadcrumbs.
 *
 * Fatal/uncaught crashes are captured automatically by the platform crash reporter;
 * this interface is for the failures the app handles gracefully (swallowed [Result]
 * failures, callback failures, etc.) that would otherwise be invisible in production.
 */
interface ErrorTracker {

    /** Reports a handled (non-fatal) [throwable], optionally with a contextual [message]. */
    fun recordError(throwable: Throwable, message: String? = null)

    /** Adds a breadcrumb that will be attached to the next crash report. */
    fun log(message: String)

    /** Attaches a custom key/value pair to subsequent crash reports. */
    fun setCustomKey(key: String, value: String)

    /** No-op implementation, used as a safe default when no reporter is wired in. */
    object NoOp : ErrorTracker {
        override fun recordError(throwable: Throwable, message: String?) {}
        override fun log(message: String) {}
        override fun setCustomKey(key: String, value: String) {}
    }
}
