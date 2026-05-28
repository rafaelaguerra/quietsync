package com.rafaelaguerra.synctask.data.billing

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.rafaelaguerra.synctask.domain.error.AppError

internal fun appErrorFor(billingResult: BillingResult): AppError {
    return when (billingResult.responseCode) {
        BillingClient.BillingResponseCode.USER_CANCELED -> AppError.PurchaseCancelled
        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> AppError.BillingServiceUnavailable
        BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> AppError.BillingUnavailable
        BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> AppError.BillingItemUnavailable
        BillingClient.BillingResponseCode.NETWORK_ERROR -> AppError.BillingNetworkError
        else -> AppError.BillingOperationFailed
    }
}
