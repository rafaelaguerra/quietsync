package com.rafaelaguerra.synctask.presentation.common

import com.rafaelaguerra.synctask.R
import com.rafaelaguerra.synctask.domain.error.AppError
import com.rafaelaguerra.synctask.domain.error.AppException

fun AppError.toUiText(): UiText = when (this) {
    AppError.CalendarCreateFailed -> UiText.Resource(R.string.error_calendar_create_failed)
    AppError.CalendarEventIdUnavailable -> UiText.Resource(R.string.error_calendar_event_id)
    AppError.CalendarNoWritableCalendar -> UiText.Resource(R.string.error_calendar_no_writable)
    AppError.EventNotFound -> UiText.Resource(R.string.error_event_not_found)
    AppError.DoNotDisturbAccessDenied -> UiText.Resource(R.string.msg_dnd_access_required)
    AppError.AirplaneModeNotAutomatable -> UiText.Resource(R.string.msg_airplane_mode_manual)
    AppError.PremiumProductNotFound -> UiText.Resource(R.string.error_premium_product_not_found)
    AppError.PurchaseCancelled -> UiText.Resource(R.string.error_purchase_cancelled)
    AppError.BillingConnectionFailed -> UiText.Resource(R.string.error_billing_connection_failed)
    AppError.BillingServiceUnavailable -> UiText.Resource(R.string.error_billing_service_unavailable)
    AppError.BillingUnavailable -> UiText.Resource(R.string.error_billing_unavailable)
    AppError.BillingNetworkError -> UiText.Resource(R.string.error_billing_network)
    AppError.BillingItemUnavailable -> UiText.Resource(R.string.error_billing_item_unavailable)
    AppError.BillingOperationFailed -> UiText.Resource(R.string.msg_purchase_failed_default)
}

fun Throwable.toUiText(): UiText = when (this) {
    is AppException -> appError.toUiText()
    else -> UiText.Resource(R.string.msg_unexpected_error)
}
