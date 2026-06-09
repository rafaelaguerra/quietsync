package com.rafaelaguerra.synctask.domain.usecase

import com.rafaelaguerra.synctask.domain.repository.PremiumRepository

class RefreshPremiumStatusUseCase(
    private val repository: PremiumRepository
) {
    suspend operator fun invoke(): Result<Boolean> {
        return repository.refreshPremiumStatus()
    }
}
