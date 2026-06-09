package com.rafaelaguerra.synctask.presentation.common

import com.rafaelaguerra.synctask.domain.error.AppError
import com.rafaelaguerra.synctask.domain.error.AppException
import com.rafaelaguerra.synctask.resources.Res
import com.rafaelaguerra.synctask.resources.error_billing_connection_failed
import com.rafaelaguerra.synctask.resources.error_billing_item_unavailable
import com.rafaelaguerra.synctask.resources.error_billing_network
import com.rafaelaguerra.synctask.resources.error_billing_service_unavailable
import com.rafaelaguerra.synctask.resources.error_billing_unavailable
import com.rafaelaguerra.synctask.resources.error_calendar_create_failed
import com.rafaelaguerra.synctask.resources.error_calendar_event_id
import com.rafaelaguerra.synctask.resources.error_calendar_no_writable
import com.rafaelaguerra.synctask.resources.error_event_not_found
import com.rafaelaguerra.synctask.resources.error_premium_product_not_found
import com.rafaelaguerra.synctask.resources.error_purchase_cancelled
import com.rafaelaguerra.synctask.resources.msg_airplane_mode_manual
import com.rafaelaguerra.synctask.resources.msg_dnd_access_required
import com.rafaelaguerra.synctask.resources.msg_purchase_failed_default
import com.rafaelaguerra.synctask.resources.msg_unexpected_error

fun AppError.toUiText(): UiText = when (this) {
    AppError.CalendarCreateFailed -> UiText.Resource(Res.string.error_calendar_create_failed)
    AppError.CalendarEventIdUnavailable -> UiText.Resource(Res.string.error_calendar_event_id)
    AppError.CalendarNoWritableCalendar -> UiText.Resource(Res.string.error_calendar_no_writable)
    AppError.EventNotFound -> UiText.Resource(Res.string.error_event_not_found)
    AppError.DoNotDisturbAccessDenied -> UiText.Resource(Res.string.msg_dnd_access_required)
    AppError.AirplaneModeNotAutomatable -> UiText.Resource(Res.string.msg_airplane_mode_manual)
    AppError.PremiumProductNotFound -> UiText.Resource(Res.string.error_premium_product_not_found)
    AppError.PurchaseCancelled -> UiText.Resource(Res.string.error_purchase_cancelled)
    AppError.BillingConnectionFailed -> UiText.Resource(Res.string.error_billing_connection_failed)
    AppError.BillingServiceUnavailable -> UiText.Resource(Res.string.error_billing_service_unavailable)
    AppError.BillingUnavailable -> UiText.Resource(Res.string.error_billing_unavailable)
    AppError.BillingNetworkError -> UiText.Resource(Res.string.error_billing_network)
    AppError.BillingItemUnavailable -> UiText.Resource(Res.string.error_billing_item_unavailable)
    AppError.BillingOperationFailed -> UiText.Resource(Res.string.msg_purchase_failed_default)
}

fun Throwable.toUiText(): UiText = when (this) {
    is AppException -> appError.toUiText()
    else -> UiText.Resource(Res.string.msg_unexpected_error)
}
