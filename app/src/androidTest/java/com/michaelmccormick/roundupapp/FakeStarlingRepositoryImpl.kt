package com.michaelmccormick.roundupapp

import com.michaelmccormick.core.models.Account
import com.michaelmccormick.core.models.AccountType
import com.michaelmccormick.core.models.FeedItem
import com.michaelmccormick.data.repositories.StarlingRepository
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FakeStarlingRepositoryImpl @Inject constructor() : StarlingRepository {
    var getTransactionsResult: Result<List<FeedItem>> = Result.success(emptyList())
    var updateRoundUpSavingsGoalResult: Result<Unit> = Result.success(Unit)

    override suspend fun getPrimaryAccount(): Result<Account> {
        return Result.success(Account(uid = "primary", categoryUid = "cat1", accountType = AccountType.PRIMARY))
    }

    override suspend fun getTransactions(account: Account, minDate: Date, maxDate: Date): Result<List<FeedItem>> {
        return getTransactionsResult
    }

    override suspend fun updateRoundUpSavingsGoal(account: Account, currency: String, amount: Long): Result<Unit> {
        return updateRoundUpSavingsGoalResult
    }
}
