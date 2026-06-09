package com.rafaelaguerra.synctask.data.source

interface PremiumBillingDataSource {
    suspend fun refreshPremiumEntitlement(): Result<Boolean>
    suspend fun queryPremiumPriceLabel(): Result<String>
}
