package com.michaelmccormick.features.roundup.domain

import com.michaelmccormick.data.repositories.StarlingRepository
import javax.inject.Inject

class UpdateRoundUpSavingsGoalUseCase @Inject internal constructor(
    private val starlingRepository: StarlingRepository,
) {
    /**
     * Updates (or creates) the round up saving goal in the users primary account using the passed [amount].
     */
    suspend operator fun invoke(currency: String, amount: Long): Result<Unit> {
        val primaryAccount = starlingRepository.getPrimaryAccount()
        if (primaryAccount.isFailure) return Result.failure(primaryAccount.exceptionOrNull()!!)
        return starlingRepository.updateRoundUpSavingsGoal(
            account = primaryAccount.getOrThrow(),
            currency = currency,
            amount = amount,
        )
    }
}
