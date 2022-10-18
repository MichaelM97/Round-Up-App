package com.michaelmccormick.data.repositories

import com.michaelmccormick.core.models.Account
import com.michaelmccormick.core.models.FeedItem
import java.util.Date

interface StarlingRepository {
    /**
     * Return the users primary account.
     */
    suspend fun getPrimaryAccount(): Result<Account>

    /**
     * Returns a list of transactions for the passed [account] from within [minDate] and [maxDate].
     */
    suspend fun getTransactions(account: Account, minDate: Date, maxDate: Date): Result<List<FeedItem>>

    /**
     * Updates the round up saving goal for the passed [account] using the passed [amount].
     */
    suspend fun updateRoundUpSavingsGoal(account: Account, currency: String, amount: Long): Result<Unit>
}
