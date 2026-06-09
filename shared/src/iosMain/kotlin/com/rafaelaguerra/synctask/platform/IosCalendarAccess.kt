@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.rafaelaguerra.synctask.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.EventKit.EKAuthorizationStatusAuthorized
import platform.EventKit.EKAuthorizationStatusFullAccess
import platform.EventKit.EKAuthorizationStatusWriteOnly
import platform.EventKit.EKEntityType
import platform.EventKit.EKEventStore
import kotlin.coroutines.resume

private val sharedEventStore = EKEventStore()

object IosCalendarAccess {

    fun hasCalendarAccess(): Boolean {
        val status = EKEventStore.authorizationStatusForEntityType(EKEntityType.EKEntityTypeEvent)
        return status == EKAuthorizationStatusAuthorized ||
            status == EKAuthorizationStatusFullAccess ||
            status == EKAuthorizationStatusWriteOnly
    }

    /** Requests calendar access when needed. Returns true if the user granted access. */
    suspend fun ensureGranted(): Boolean {
        if (hasCalendarAccess()) return true
        return requestAccess()
    }

    private suspend fun requestAccess(): Boolean = suspendCancellableCoroutine { continuation ->
        sharedEventStore.requestFullAccessToEventsWithCompletion { granted, _ ->
            if (continuation.isActive) {
                continuation.resume(granted)
            }
        }
    }
}
