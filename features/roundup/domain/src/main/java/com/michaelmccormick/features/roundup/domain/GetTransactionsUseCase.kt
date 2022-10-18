package com.michaelmccormick.features.roundup.domain

import com.michaelmccormick.core.models.FeedItem
import com.michaelmccormick.data.repositories.StarlingRepository
import java.util.Date
import javax.inject.Inject

class GetTransactionsUseCase @Inject internal constructor(
    private val starlingRepository: StarlingRepository,
) {
    /**
     * Gets transactions for the users primary account from between [minDate] and [maxDate].
     */
    suspend operator fun invoke(minDate: Date, maxDate: Date): Result<List<FeedItem>> {
        val primaryAccount = starlingRepository.getPrimaryAccount()
        if (primaryAccount.isFailure) return Result.failure(primaryAccount.exceptionOrNull()!!)
        return starlingRepository.getTransactions(
            account = primaryAccount.getOrThrow(),
            minDate = minDate,
            maxDate = maxDate,
        )
    }
}
