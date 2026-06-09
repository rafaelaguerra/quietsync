@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.rafaelaguerra.synctask.data.billing

import com.rafaelaguerra.synctask.data.local.IosPremiumPreferencesStorage
import com.rafaelaguerra.synctask.data.source.PremiumBillingDataSource
import com.rafaelaguerra.synctask.domain.billing.DEFAULT_PREMIUM_PRICE_LABEL
import com.rafaelaguerra.synctask.domain.billing.PREMIUM_PRODUCT_ID
import com.rafaelaguerra.synctask.domain.error.AppError
import com.rafaelaguerra.synctask.domain.error.asException
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.Foundation.NSError
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.StoreKit.SKPaymentQueue
import platform.StoreKit.SKPaymentTransaction
import platform.StoreKit.SKPaymentTransactionObserverProtocol
import platform.StoreKit.SKPaymentTransactionState
import platform.StoreKit.SKProduct
import platform.StoreKit.SKProductsRequest
import platform.StoreKit.SKProductsRequestDelegateProtocol
import platform.StoreKit.SKProductsResponse
import platform.StoreKit.SKRequest
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class IosPremiumBillingDataSource(
    private val premiumPreferences: IosPremiumPreferencesStorage
) : PremiumBillingDataSource {

    private val mutex = Mutex()
    private var cachedPriceLabel: String = DEFAULT_PREMIUM_PRICE_LABEL
    private var cachedProduct: SKProduct? = null
    private var activeProductsDelegate: ProductsRequestDelegate? = null
    private var pendingPurchaseCallback: ((Result<Boolean>) -> Unit)? = null
    private var pendingRestoreCallback: ((Result<Boolean>) -> Unit)? = null
    private val transactionObserver = PaymentQueueObserver(this)

    init {
        SKPaymentQueue.defaultQueue().addTransactionObserver(transactionObserver)
    }

    fun dispose() {
        SKPaymentQueue.defaultQueue().removeTransactionObserver(transactionObserver)
    }

    override suspend fun queryPremiumPriceLabel(): Result<String> = runCatching {
        mutex.withLock {
            val product = fetchProductLocked()
            cachedPriceLabel = product?.formattedPrice() ?: DEFAULT_PREMIUM_PRICE_LABEL
            cachedPriceLabel
        }
    }

    override suspend fun refreshPremiumEntitlement(): Result<Boolean> = Result.success(
        premiumPreferences.currentIsPremium()
    )

    suspend fun purchasePremium(): Result<Boolean> = runCatching {
        mutex.withLock {
            suspendCancellableCoroutine { continuation ->
                val product = cachedProduct
                if (product == null) {
                    continuation.resumeWithException(AppError.PremiumProductNotFound.asException())
                    return@suspendCancellableCoroutine
                }
                if (pendingPurchaseCallback != null) {
                    continuation.resumeWithException(AppError.BillingOperationFailed.asException())
                    return@suspendCancellableCoroutine
                }
                pendingPurchaseCallback = { result ->
                    if (continuation.isActive) {
                        if (result.isSuccess) {
                            continuation.resume(result.getOrThrow())
                        } else {
                            continuation.resumeWithException(
                                result.exceptionOrNull() ?: AppError.BillingOperationFailed.asException()
                            )
                        }
                    }
                }
                val payment = platform.StoreKit.SKPayment.paymentWithProduct(product)
                SKPaymentQueue.defaultQueue().addPayment(payment)
                continuation.invokeOnCancellation { pendingPurchaseCallback = null }
            }
        }
    }

    suspend fun restorePurchases(): Result<Boolean> = runCatching {
        mutex.withLock {
            suspendCancellableCoroutine { continuation ->
                if (pendingRestoreCallback != null) {
                    continuation.resumeWithException(AppError.BillingOperationFailed.asException())
                    return@suspendCancellableCoroutine
                }
                pendingRestoreCallback = { result ->
                    if (continuation.isActive) {
                        if (result.isSuccess) {
                            continuation.resume(result.getOrThrow())
                        } else {
                            continuation.resumeWithException(
                                result.exceptionOrNull() ?: AppError.BillingOperationFailed.asException()
                            )
                        }
                    }
                }
                SKPaymentQueue.defaultQueue().restoreCompletedTransactions()
                continuation.invokeOnCancellation { pendingRestoreCallback = null }
            }
        }
    }

    internal fun handleUpdatedTransactions(transactions: List<*>) {
        transactions.forEach { raw ->
            val transaction = raw as? SKPaymentTransaction ?: return@forEach
            when (transaction.transactionState) {
                SKPaymentTransactionState.SKPaymentTransactionStatePurchased,
                SKPaymentTransactionState.SKPaymentTransactionStateRestored -> {
                    if (transaction.payment?.productIdentifier == PREMIUM_PRODUCT_ID) {
                        premiumPreferences.setIsPremium(true)
                        completePendingPurchase(Result.success(true))
                        completePendingRestore(Result.success(true))
                    }
                    SKPaymentQueue.defaultQueue().finishTransaction(transaction)
                }
                SKPaymentTransactionState.SKPaymentTransactionStateFailed -> {
                    val error = transaction.error
                    val failure: Result<Boolean> = if (error?.code?.toInt() == 2) {
                        Result.failure(AppError.PurchaseCancelled.asException())
                    } else {
                        Result.failure(AppError.BillingOperationFailed.asException())
                    }
                    completePendingPurchase(failure)
                    completePendingRestore(failure)
                    SKPaymentQueue.defaultQueue().finishTransaction(transaction)
                }
                else -> Unit
            }
        }
    }

    internal fun handleRestoreCompleted() {
        if (!premiumPreferences.currentIsPremium()) {
            completePendingRestore(Result.success(false))
        }
    }

    internal fun handleRestoreFailed() {
        completePendingRestore(Result.failure(AppError.BillingOperationFailed.asException()))
    }

    private fun completePendingPurchase(result: Result<Boolean>) {
        pendingPurchaseCallback?.invoke(result)
        pendingPurchaseCallback = null
    }

    private fun completePendingRestore(result: Result<Boolean>) {
        pendingRestoreCallback?.invoke(result)
        pendingRestoreCallback = null
    }

    private suspend fun fetchProductLocked(): SKProduct? {
        cachedProduct?.let { return it }
        return suspendCancellableCoroutine { continuation ->
            val delegate = ProductsRequestDelegate { product ->
                activeProductsDelegate = null
                cachedProduct = product
                if (continuation.isActive) continuation.resume(product)
            }
            activeProductsDelegate = delegate
            val request = SKProductsRequest(productIdentifiers = setOf(PREMIUM_PRODUCT_ID))
            request.delegate = delegate
            request.start()
            continuation.invokeOnCancellation {
                request.cancel()
                activeProductsDelegate = null
            }
        }
    }

    private class ProductsRequestDelegate(
        private val onProduct: (SKProduct?) -> Unit
    ) : NSObject(), SKProductsRequestDelegateProtocol {
        override fun productsRequest(request: SKProductsRequest, didReceiveResponse: SKProductsResponse) {
            val product = didReceiveResponse.products
                .firstOrNull { (it as? SKProduct)?.productIdentifier == PREMIUM_PRODUCT_ID } as? SKProduct
            onProduct(product)
        }

        override fun request(request: SKRequest, didFailWithError: NSError) {
            onProduct(null)
        }
    }
}

private class PaymentQueueObserver(
    private val billing: IosPremiumBillingDataSource
) : NSObject(), SKPaymentTransactionObserverProtocol {

    override fun paymentQueue(queue: SKPaymentQueue, updatedTransactions: List<*>) {
        billing.handleUpdatedTransactions(updatedTransactions)
    }

    override fun paymentQueueRestoreCompletedTransactionsFinished(queue: SKPaymentQueue) {
        billing.handleRestoreCompleted()
    }

    override fun paymentQueue(
        queue: SKPaymentQueue,
        restoreCompletedTransactionsFailedWithError: NSError
    ) {
        billing.handleRestoreFailed()
    }
}

private fun SKProduct.formattedPrice(): String {
    val formatter = NSNumberFormatter()
    formatter.numberStyle = NSNumberFormatterCurrencyStyle
    formatter.locale = priceLocale
    return formatter.stringFromNumber(price) ?: DEFAULT_PREMIUM_PRICE_LABEL
}
