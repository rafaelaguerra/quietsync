package com.rafaelaguerra.synctask.domain.error

/**
 * User-facing error types for the app. Resolved to localized strings in the presentation layer.
 */
sealed class AppError {
    // Calendar
    data object CalendarCreateFailed : AppError()
    data object CalendarEventIdUnavailable : AppError()
    data object CalendarNoWritableCalendar : AppError()

    // Events
    data object EventNotFound : AppError()

    // Phone state
    data object DoNotDisturbAccessDenied : AppError()
    data object AirplaneModeNotAutomatable : AppError()

    // Billing
    data object PremiumProductNotFound : AppError()
    data object PurchaseCancelled : AppError()
    data object BillingConnectionFailed : AppError()
    data object BillingServiceUnavailable : AppError()
    data object BillingUnavailable : AppError()
    data object BillingNetworkError : AppError()
    data object BillingItemUnavailable : AppError()
    data object BillingOperationFailed : AppError()
}

class AppException(val appError: AppError) : Exception()

fun AppError.asException(): AppException = AppException(this)
