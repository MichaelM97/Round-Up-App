package com.michaelmccormick.data.repositories

import com.michaelmccormick.core.factories.SimpleDateFormatFactory
import com.michaelmccormick.core.factories.UUIDFactory
import com.michaelmccormick.core.models.Account
import com.michaelmccormick.core.models.AccountType
import com.michaelmccormick.core.models.FeedItem
import com.michaelmccormick.core.models.SavingsGoal
import com.michaelmccormick.data.mappers.AccountEntityMapper
import com.michaelmccormick.data.mappers.FeedItemEntityMapper
import com.michaelmccormick.data.mappers.SavingsGoalEntityMapper
import com.michaelmccormick.network.entities.CreateSavingsGoalRequestEntity
import com.michaelmccormick.network.entities.CurrencyEntity
import com.michaelmccormick.network.entities.TopUpRequestEntity
import com.michaelmccormick.network.services.StarlingAPIService
import java.util.Date
import javax.inject.Inject
import timber.log.Timber

internal class StarlingRepositoryImpl @Inject constructor(
    private val starlingAPIService: StarlingAPIService,
    private val accountEntityMapper: AccountEntityMapper,
    private val feedItemEntityMapper: FeedItemEntityMapper,
    private val savingsGoalEntityMapper: SavingsGoalEntityMapper,
    private val simpleDateFormatFactory: SimpleDateFormatFactory,
    private val uuidFactory: UUIDFactory,
) : StarlingRepository {
    private var primaryAccountCache: Account? = null
    private var roundUpSavingsGoalCache: SavingsGoal? = null

    override suspend fun getPrimaryAccount(): Result<Account> {
        primaryAccountCache?.let { return Result.success(it) }
        return try {
            primaryAccountCache = starlingAPIService.getAccounts()
                .accounts
                .mapNotNull { accountEntityMapper.toAccount(it) }
                .firstOrNull { it.accountType == AccountType.PRIMARY }
            Result.success(primaryAccountCache ?: throw IllegalStateException("No primary account found"))
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure(e)
        }
    }

    override suspend fun getTransactions(account: Account, minDate: Date, maxDate: Date): Result<List<FeedItem>> {
        return try {
            val transactions = starlingAPIService.getAccountFeed(
                accountUid = account.uid,
                categoryUid = account.categoryUid,
                minTransactionTimestamp = simpleDateFormatFactory.minIso8601().format(minDate),
                maxTransactionTimestamp = simpleDateFormatFactory.maxIso8601().format(maxDate),
            )
                .feedItems
                .mapNotNull { feedItemEntityMapper.toFeedItem(it) }
            Result.success(transactions)
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure(e)
        }
    }

    override suspend fun updateRoundUpSavingsGoal(account: Account, currency: String, amount: Long): Result<Unit> {
        return try {
            // Fetch round up savings goal if cache is null
            if (roundUpSavingsGoalCache == null) {
                roundUpSavingsGoalCache = starlingAPIService.getSavingsGoals(accountUid = account.uid)
                    .savingsGoalList
                    .mapNotNull { savingsGoalEntityMapper.toSavingsGoal(it) }
                    .firstOrNull { it.name == ROUND_UP_SAVINGS_GOAL }
            }
            // If cache is still null user doesn't have the round up savings goal yet, create one
            val roundUpSavingsGoalUid: String = if (roundUpSavingsGoalCache == null) {
                val response = starlingAPIService.createSavingsGoal(
                    accountUid = account.uid,
                    createSavingsGoalRequestEntity = CreateSavingsGoalRequestEntity(
                        name = ROUND_UP_SAVINGS_GOAL,
                        currency = currency,
                    ),
                )
                if (response.success == false || response.savingsGoalUid == null) {
                    throw IllegalStateException("Failed to create round up savings goal")
                } else {
                    response.savingsGoalUid!!
                }
            } else roundUpSavingsGoalCache!!.uid
            // Add amount to savings goal
            val result = starlingAPIService.addMoneyToSavingsGoal(
                accountUid = account.uid,
                savingsGoalUid = roundUpSavingsGoalUid,
                transferUid = uuidFactory.random(),
                topUpRequest = TopUpRequestEntity(
                    amount = CurrencyEntity(currency = currency, minorUnits = amount),
                ),
            )
            if (result.success == false) throw IllegalStateException("Savings goal failed to update")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure(e)
        }
    }

    private companion object {
        const val ROUND_UP_SAVINGS_GOAL = "Round Up Savings"
    }
}
