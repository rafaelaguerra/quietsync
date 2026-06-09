package com.rafaelaguerra.synctask.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.rafaelaguerra.synctask.data.local.PremiumPreferencesLocalDataSource
import com.rafaelaguerra.synctask.data.source.PremiumBillingDataSource
import com.rafaelaguerra.synctask.domain.error.AppError
import com.rafaelaguerra.synctask.domain.error.asException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PlayBillingDataSource(
    context: Context,
    private val premiumPreferences: PremiumPreferencesLocalDataSource
) : PurchasesUpdatedListener, PremiumBillingDataSource {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pendingPurchaseCallback: ((Result<Boolean>) -> Unit)? = null
    private var cachedPremiumProductDetails: ProductDetails? = null

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    override suspend fun queryPremiumPriceLabel(): Result<String> {
        return runCatching {
            val details = queryPremiumProductDetails()
            val formattedPrice = details?.oneTimePurchaseOfferDetails?.formattedPrice
            formattedPrice ?: DEFAULT_PREMIUM_PRICE_LABEL
        }
    }

    override suspend fun refreshPremiumEntitlement(): Result<Boolean> {
        return runCatching {
            ensureConnected()
            val purchases = queryPurchases()
            val isPremium = processPremiumPurchases(purchases)
            premiumPreferences.setIsPremium(isPremium)
            isPremium
        }
    }

    fun launchPremiumPurchase(
        activity: Activity,
        onResult: (Result<Boolean>) -> Unit
    ) {
        scope.launch {
            val detailsResult = runCatching { queryPremiumProductDetails() }
            val details = detailsResult.getOrNull()

            if (details == null) {
                withContext(Dispatchers.Main.immediate) {
                    onResult(Result.failure(AppError.PremiumProductNotFound.asException()))
                }
                return@launch
            }

            withContext(Dispatchers.Main.immediate) {
                pendingPurchaseCallback = onResult
                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(details)
                                .build()
                        )
                    )
                    .build()

                val billingResult = billingClient.launchBillingFlow(activity, flowParams)
                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    pendingPurchaseCallback = null
                    onResult(Result.failure(appErrorFor(billingResult).asException()))
                }
            }
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        val callback = pendingPurchaseCallback
        pendingPurchaseCallback = null

        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                scope.launch {
                    val unlocked = processPremiumPurchases(purchases.orEmpty())
                    premiumPreferences.setIsPremium(unlocked)
                    withContext(Dispatchers.Main.immediate) {
                        callback?.invoke(Result.success(unlocked))
                    }
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                callback?.invoke(Result.failure(AppError.PurchaseCancelled.asException()))
            }

            else -> {
                callback?.invoke(Result.failure(appErrorFor(billingResult).asException()))
            }
        }
    }

    fun destroy() {
        if (billingClient.isReady) billingClient.endConnection()
        scope.cancel()
    }

    private suspend fun queryPremiumProductDetails(): ProductDetails? {
        cachedPremiumProductDetails?.let { return it }
        ensureConnected()

        val details = suspendCancellableCoroutine<List<ProductDetails>> { continuation ->
            val queryParams = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PREMIUM_PRODUCT_ID)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    )
                )
                .build()

            billingClient.queryProductDetailsAsync(queryParams) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(productDetailsList)
                } else {
                    continuation.resumeWithException(
                        appErrorFor(billingResult).asException()
                    )
                }
            }
        }

        return details.firstOrNull { it.productId == PREMIUM_PRODUCT_ID }
            ?.also { cachedPremiumProductDetails = it }
    }

    private suspend fun queryPurchases(): List<Purchase> {
        return suspendCancellableCoroutine { continuation ->
            val queryPurchasesParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

            billingClient.queryPurchasesAsync(queryPurchasesParams) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(purchases)
                } else {
                    continuation.resumeWithException(
                        appErrorFor(billingResult).asException()
                    )
                }
            }
        }
    }

    private suspend fun processPremiumPurchases(purchases: List<Purchase>): Boolean {
        var hasPremium = false
        purchases.forEach { purchase ->
            val includesPremiumProduct = purchase.products.contains(PREMIUM_PRODUCT_ID)
            if (includesPremiumProduct && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                hasPremium = true
                if (!purchase.isAcknowledged) {
                    acknowledgePurchase(purchase.purchaseToken)
                }
            }
        }
        return hasPremium
    }

    private suspend fun acknowledgePurchase(purchaseToken: String) {
        suspendCancellableCoroutine<Unit> { continuation ->
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()

            billingClient.acknowledgePurchase(params) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(Unit)
                } else {
                    continuation.resumeWithException(
                        appErrorFor(billingResult).asException()
                    )
                }
            }
        }
    }

    private suspend fun ensureConnected() {
        if (billingClient.isReady) return
        suspendCancellableCoroutine<Unit> { continuation ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    if (continuation.isActive) {
                        continuation.resumeWithException(
                            AppError.BillingConnectionFailed.asException()
                        )
                    }
                }

                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (!continuation.isActive) return
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        continuation.resume(Unit)
                    } else {
                        continuation.resumeWithException(
                            appErrorFor(billingResult).asException()
                        )
                    }
                }
            })
        }
    }

    companion object {
        const val PREMIUM_PRODUCT_ID = "aurasync_premium_upgrade"
        private const val DEFAULT_PREMIUM_PRICE_LABEL = "$0.99"
    }
}
