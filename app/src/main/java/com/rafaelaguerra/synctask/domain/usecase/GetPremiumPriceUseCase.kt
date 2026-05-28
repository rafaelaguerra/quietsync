package com.rafaelaguerra.synctask.domain.usecase

import com.rafaelaguerra.synctask.domain.repository.PremiumRepository

class GetPremiumPriceUseCase(
    private val repository: PremiumRepository
) {
    suspend operator fun invoke(): Result<String> = repository.getPremiumPriceLabel()
}
