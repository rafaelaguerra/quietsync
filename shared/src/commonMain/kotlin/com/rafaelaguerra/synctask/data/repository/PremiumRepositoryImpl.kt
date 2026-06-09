package com.rafaelaguerra.synctask.data.repository

import com.rafaelaguerra.synctask.data.source.PremiumBillingDataSource
import com.rafaelaguerra.synctask.data.source.PremiumPreferencesStorage
import com.rafaelaguerra.synctask.domain.repository.PremiumRepository
import kotlinx.coroutines.flow.Flow

class PremiumRepositoryImpl(
    private val premiumBillingDataSource: PremiumBillingDataSource,
    private val premiumPreferencesStorage: PremiumPreferencesStorage
) : PremiumRepository {

    override fun observeIsPremium(): Flow<Boolean> {
        return premiumPreferencesStorage.observeIsPremium()
    }

    override suspend fun refreshPremiumStatus(): Result<Boolean> {
        return premiumBillingDataSource.refreshPremiumEntitlement()
            .onSuccess { premiumPreferencesStorage.setIsPremium(it) }
    }

    override suspend fun getPremiumPriceLabel(): Result<String> {
        return premiumBillingDataSource.queryPremiumPriceLabel()
    }
}
