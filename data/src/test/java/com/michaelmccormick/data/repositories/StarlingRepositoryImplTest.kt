package com.michaelmccormick.data.repositories

import com.michaelmccormick.core.factories.SimpleDateFormatFactory
import com.michaelmccormick.core.factories.UUIDFactory
import com.michaelmccormick.core.models.Account
import com.michaelmccormick.core.models.AccountType
import com.michaelmccormick.core.models.Currency
import com.michaelmccormick.core.models.FeedItem
import com.michaelmccormick.core.models.FeedItemDirection
import com.michaelmccormick.core.models.SavingsGoal
import com.michaelmccormick.data.mappers.AccountEntityMapper
import com.michaelmccormick.data.mappers.FeedItemEntityMapper
import com.michaelmccormick.data.mappers.SavingsGoalEntityMapper
import com.michaelmccormick.network.entities.AccountEntity
import com.michaelmccormick.network.entities.AccountListEntity
import com.michaelmccormick.network.entities.CreateSavingsGoalRequestEntity
import com.michaelmccormick.network.entities.CreateSavingsGoalResponseEntity
import com.michaelmccormick.network.entities.CurrencyEntity
import com.michaelmccormick.network.entities.FeedItemEntity
import com.michaelmccormick.network.entities.FeedListEntity
import com.michaelmccormick.network.entities.SavingsGoalEntity
import com.michaelmccormick.network.entities.SavingsGoalListEntity
import com.michaelmccormick.network.entities.SavingsGoalTransferEntity
import com.michaelmccormick.network.entities.TopUpRequestEntity
import com.michaelmccormick.network.services.StarlingAPIService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
internal class StarlingRepositoryImplTest {
    private val mockStarlingAPIService: StarlingAPIService = mockk()
    private val mockAccountEntityMapper: AccountEntityMapper = mockk()
    private val mockFeedItemEntityMapper: FeedItemEntityMapper = mockk()
    private val mockSavingsGoalEntityMapper: SavingsGoalEntityMapper = mockk()
    private val mockSimpleDateFormatFactory: SimpleDateFormatFactory = mockk()
    private val mockUuidFactory: UUIDFactory = mockk()
    private lateinit var starlingRepositoryImpl: StarlingRepositoryImpl

    @BeforeEach
    fun before() {
        starlingRepositoryImpl = StarlingRepositoryImpl(
            starlingAPIService = mockStarlingAPIService,
            accountEntityMapper = mockAccountEntityMapper,
            feedItemEntityMapper = mockFeedItemEntityMapper,
            savingsGoalEntityMapper = mockSavingsGoalEntityMapper,
            simpleDateFormatFactory = mockSimpleDateFormatFactory,
            uuidFactory = mockUuidFactory,
        )
    }

    @Nested
    inner class GetPrimaryAccount {
        @Test
        fun `Should return primary account`() = runTest {
            // Given
            val accountEntity1 = AccountEntity(accountUid = "123", defaultCategory = "cat1", accountType = "ADDITIONAL")
            val accountEntity2 = AccountEntity(accountUid = "456", defaultCategory = "cat2", accountType = "PRIMARY")
            coEvery { mockStarlingAPIService.getAccounts() } returns AccountListEntity(accounts = listOf(accountEntity1, accountEntity2))
            val primaryAccount = Account(uid = "456", categoryUid = "cat2", accountType = AccountType.PRIMARY)
            every { mockAccountEntityMapper.toAccount(accountEntity1) } returns Account(uid = "123", categoryUid = "cat1", accountType = AccountType.ADDITIONAL)
            every { mockAccountEntityMapper.toAccount(accountEntity2) } returns primaryAccount

            // When
            val result = starlingRepositoryImpl.getPrimaryAccount()

            // Then
            assertTrue(result.isSuccess)
            assertEquals(primaryAccount, result.getOrNull())
            coVerify(exactly = 1) { mockStarlingAPIService.getAccounts() }
        }

        @Test
        fun `Should return primary account from cache when previously fetched`() = runTest {
            // Given
            val accountEntity = AccountEntity(accountUid = "123", defaultCategory = "cat1", accountType = "PRIMARY")
            coEvery { mockStarlingAPIService.getAccounts() } returns AccountListEntity(accounts = listOf(accountEntity))
            val primaryAccount = Account(uid = "123", categoryUid = "cat1", accountType = AccountType.PRIMARY)
            every { mockAccountEntityMapper.toAccount(accountEntity) } returns primaryAccount

            // When
            val result1 = starlingRepositoryImpl.getPrimaryAccount()
            val result2 = starlingRepositoryImpl.getPrimaryAccount()

            // Then
            assertTrue(result1.isSuccess)
            assertTrue(result2.isSuccess)
            assertEquals(primaryAccount, result1.getOrNull())
            assertEquals(primaryAccount, result2.getOrNull())
            coVerify(exactly = 1) { mockStarlingAPIService.getAccounts() }
        }

        @Test
        fun `Should return failure when service throws`() = runTest {
            // Given
            coEvery { mockStarlingAPIService.getAccounts() } throws Exception()

            // When
            val result = starlingRepositoryImpl.getPrimaryAccount()

            // Then
            assertTrue(result.isFailure)
            coVerify(exactly = 1) { mockStarlingAPIService.getAccounts() }
        }

        @Test
        fun `Should return failure when no primary account returned`() = runTest {
            // Given
            val accountEntity = AccountEntity(accountUid = "123", defaultCategory = "cat1", accountType = "ADDITIONAL")
            coEvery { mockStarlingAPIService.getAccounts() } returns AccountListEntity(accounts = listOf(accountEntity))
            every { mockAccountEntityMapper.toAccount(accountEntity) } returns Account(uid = "123", categoryUid = "cat1", accountType = AccountType.ADDITIONAL)

            // When
            val result = starlingRepositoryImpl.getPrimaryAccount()

            // Then
            assertTrue(result.isFailure)
            coVerify(exactly = 1) { mockStarlingAPIService.getAccounts() }
        }
    }

    @Nested
    inner class GetTransactions {
        @Test
        fun `Should return transactions`() = runTest {
            // Given
            val account = Account(uid = "account1", categoryUid = "cat1", accountType = AccountType.PRIMARY)
            val minDate: Date = mockk()
            val maxDate: Date = mockk()
            every { mockSimpleDateFormatFactory.minIso8601().format(minDate) } returns "10/10/22"
            every { mockSimpleDateFormatFactory.maxIso8601().format(maxDate) } returns "16/10/22"
            val transactionEntity1 = FeedItemEntity(feedItemUid = "123", null, null, null, null)
            val transactionEntity2 = FeedItemEntity(feedItemUid = "456", null, null, null, null)
            val transactionEntity3 = FeedItemEntity(feedItemUid = "789", null, null, null, null)
            coEvery {
                mockStarlingAPIService.getAccountFeed(
                    accountUid = "account1",
                    categoryUid = "cat1",
                    minTransactionTimestamp = "10/10/22",
                    maxTransactionTimestamp = "16/10/22",
                )
            } returns FeedListEntity(feedItems = listOf(transactionEntity1, transactionEntity2, transactionEntity3))
            val transaction1 = FeedItem(
                uid = "123",
                amount = Currency("GBP", 100),
                direction = FeedItemDirection.OUT,
                date = mockk(),
                counterPartyName = "Shop",
            )
            val transaction2 = FeedItem(
                uid = "789",
                amount = Currency("GBP", 25000),
                direction = FeedItemDirection.IN,
                date = mockk(),
                counterPartyName = "Work",
            )
            every { mockFeedItemEntityMapper.toFeedItem(transactionEntity1) } returns transaction1
            every { mockFeedItemEntityMapper.toFeedItem(transactionEntity2) } returns null
            every { mockFeedItemEntityMapper.toFeedItem(transactionEntity3) } returns transaction2

            // When
            val result = starlingRepositoryImpl.getTransactions(account, minDate, maxDate)

            // Then
            assertTrue(result.isSuccess)
            assertEquals(listOf(transaction1, transaction2), result.getOrNull())
            coVerify(exactly = 1) {
                mockStarlingAPIService.getAccountFeed(
                    accountUid = "account1",
                    categoryUid = "cat1",
                    minTransactionTimestamp = "10/10/22",
                    maxTransactionTimestamp = "16/10/22",
                )
            }
        }

        @Test
        fun `Should return success when no transactions returned`() = runTest {
            // Given
            val account = Account(uid = "account1", categoryUid = "cat1", accountType = AccountType.PRIMARY)
            val minDate: Date = mockk()
            val maxDate: Date = mockk()
            every { mockSimpleDateFormatFactory.minIso8601().format(minDate) } returns "10/10/22"
            every { mockSimpleDateFormatFactory.maxIso8601().format(maxDate) } returns "16/10/22"
            coEvery {
                mockStarlingAPIService.getAccountFeed(
                    accountUid = "account1",
                    categoryUid = "cat1",
                    minTransactionTimestamp = "10/10/22",
                    maxTransactionTimestamp = "16/10/22",
                )
            } returns FeedListEntity(feedItems = emptyList())

            // When
            val result = starlingRepositoryImpl.getTransactions(account, minDate, maxDate)

            // Then
            assertTrue(result.isSuccess)
            assertEquals(emptyList(), result.getOrNull())
            coVerify(exactly = 1) {
                mockStarlingAPIService.getAccountFeed(
                    accountUid = "account1",
                    categoryUid = "cat1",
                    minTransactionTimestamp = "10/10/22",
                    maxTransactionTimestamp = "16/10/22",
                )
            }
        }

        @Test
        fun `Should return failure when service throws`() = runTest {
            // Given
            val account = Account(uid = "account1", categoryUid = "cat1", accountType = AccountType.PRIMARY)
            val minDate: Date = mockk()
            val maxDate: Date = mockk()
            every { mockSimpleDateFormatFactory.minIso8601().format(minDate) } returns "10/10/22"
            every { mockSimpleDateFormatFactory.maxIso8601().format(maxDate) } returns "16/10/22"
            coEvery {
                mockStarlingAPIService.getAccountFeed(
                    accountUid = "account1",
                    categoryUid = "cat1",
                    minTransactionTimestamp = "10/10/22",
                    maxTransactionTimestamp = "16/10/22",
                )
            } throws Exception()

            // When
            val result = starlingRepositoryImpl.getTransactions(account, minDate, maxDate)

            // Then
            assertTrue(result.isFailure)
            coVerify(exactly = 1) {
                mockStarlingAPIService.getAccountFeed(
                    accountUid = "account1",
                    categoryUid = "cat1",
                    minTransactionTimestamp = "10/10/22",
                    maxTransactionTimestamp = "16/10/22",
                )
            }
        }
    }

    @Nested
    inner class UpdateRoundUpSavingsGoal {
        @Test
        fun `Should add money to round up savings goal after creating goal`() = runTest {
            // Given
            val account = Account(uid = "account1", categoryUid = "cat1", accountType = AccountType.PRIMARY)
            val currency = "GBP"
            val amount = 2500L
            coEvery { mockStarlingAPIService.getSavingsGoals(accountUid = account.uid) } returns SavingsGoalListEntity(savingsGoalList = emptyList())
            coEvery {
                mockStarlingAPIService.createSavingsGoal(
                    accountUid = "account1",
                    createSavingsGoalRequestEntity = CreateSavingsGoalRequestEntity(
                        name = "Round Up Savings",
                        currency = currency,
                    ),
                )
            } returns CreateSavingsGoalResponseEntity(savingsGoalUid = "savings1", success = true)
            every { mockUuidFactory.random() } returns "abc-123"
            coEvery {
                mockStarlingAPIService.addMoneyToSavingsGoal(
                    accountUid = account.uid,
                    savingsGoalUid = "savings1",
                    transferUid = "abc-123",
                    topUpRequest = TopUpRequestEntity(amount = CurrencyEntity(currency = currency, minorUnits = amount)),
                )
            } returns SavingsGoalTransferEntity(success = true)

            // When
            val result = starlingRepositoryImpl.updateRoundUpSavingsGoal(account = account, currency = currency, amount = amount)

            // Then
            assertTrue(result.isSuccess)
            coVerifyOrder {
                mockStarlingAPIService.getSavingsGoals(accountUid = account.uid)
                mockStarlingAPIService.createSavingsGoal(
                    accountUid = "account1",
                    createSavingsGoalRequestEntity = CreateSavingsGoalRequestEntity(
                        name = "Round Up Savings",
                        currency = currency,
                    ),
                )
                mockStarlingAPIService.addMoneyToSavingsGoal(
                    accountUid = account.uid,
                    savingsGoalUid = "savings1",
                    transferUid = "abc-123",
                    topUpRequest = TopUpRequestEntity(amount = CurrencyEntity(currency = currency, minorUnits = amount)),
                )
            }
        }

        @Test
        fun `Should add money to round up savings goal using existing goal`() = runTest {
            // Given
            val account = Account(uid = "account1", categoryUid = "cat1", accountType = AccountType.PRIMARY)
            val currency = "GBP"
            val amount = 300L
            val savingsGoalEntity1 = SavingsGoalEntity(savingsGoalUid = "savings1", name = "Rent")
            val savingsGoalEntity2 = SavingsGoalEntity(savingsGoalUid = "savings2", name = "Round Up Savings")
            every { mockSavingsGoalEntityMapper.toSavingsGoal(savingsGoalEntity1) } returns SavingsGoal(uid = "savings1", name = "Rent")
            every { mockSavingsGoalEntityMapper.toSavingsGoal(savingsGoalEntity2) } returns SavingsGoal(uid = "savings2", name = "Round Up Savings")
            coEvery { mockStarlingAPIService.getSavingsGoals(accountUid = account.uid) } returns SavingsGoalListEntity(
                savingsGoalList = listOf(savingsGoalEntity1, savingsGoalEntity2),
            )
            every { mockUuidFactory.random() } returns "abc-123"
            coEvery {
                mockStarlingAPIService.addMoneyToSavingsGoal(
                    accountUid = account.uid,
                    savingsGoalUid = "savings2",
                    transferUid = "abc-123",
                    topUpRequest = TopUpRequestEntity(amount = CurrencyEntity(currency = currency, minorUnits = amount)),
                )
            } returns SavingsGoalTransferEntity(success = true)

            // When
            val result = starlingRepositoryImpl.updateRoundUpSavingsGoal(account = account, currency = currency, amount = amount)

            // Then
            assertTrue(result.isSuccess)
            coVerifyOrder {
                mockStarlingAPIService.getSavingsGoals(accountUid = account.uid)
                mockStarlingAPIService.addMoneyToSavingsGoal(
                    accountUid = account.uid,
                    savingsGoalUid = "savings2",
                    transferUid = "abc-123",
                    topUpRequest = TopUpRequestEntity(amount = CurrencyEntity(currency = currency, minorUnits = amount)),
                )
            }
            coVerify(exactly = 0) { mockStarlingAPIService.createSavingsGoal(any(), any()) }
        }

        @Test
        fun `Should add money to round up savings goal using cache when previously fetched`() = runTest {
            // Given
            val account = Account(uid = "account1", categoryUid = "cat1", accountType = AccountType.PRIMARY)
            val currency = "GBP"
            val amount = 300L
            val savingsGoalEntity1 = SavingsGoalEntity(savingsGoalUid = "savings1", name = "Rent")
            val savingsGoalEntity2 = SavingsGoalEntity(savingsGoalUid = "savings2", name = "Round Up Savings")
            every { mockSavingsGoalEntityMapper.toSavingsGoal(savingsGoalEntity1) } returns SavingsGoal(uid = "savings1", name = "Rent")
            every { mockSavingsGoalEntityMapper.toSavingsGoal(savingsGoalEntity2) } returns SavingsGoal(uid = "savings2", name = "Round Up Savings")
            coEvery { mockStarlingAPIService.getSavingsGoals(accountUid = account.uid) } returns SavingsGoalListEntity(
                savingsGoalList = listOf(savingsGoalEntity1, savingsGoalEntity2),
            )
            every { mockUuidFactory.random() } returns "abc-123"
            coEvery {
                mockStarlingAPIService.addMoneyToSavingsGoal(
                    accountUid = account.uid,
                    savingsGoalUid = "savings2",
                    transferUid = "abc-123",
                    topUpRequest = TopUpRequestEntity(amount = CurrencyEntity(currency = currency, minorUnits = amount)),
                )
            } returns SavingsGoalTransferEntity(success = true)

            // When
            val result1 = starlingRepositoryImpl.updateRoundUpSavingsGoal(account = account, currency = currency, amount = amount)
            val result2 = starlingRepositoryImpl.updateRoundUpSavingsGoal(account = account, currency = currency, amount = amount)

            // Then
            assertTrue(result1.isSuccess)
            assertTrue(result2.isSuccess)
            coVerifyOrder {
                mockStarlingAPIService.getSavingsGoals(accountUid = account.uid)
                mockStarlingAPIService.addMoneyToSavingsGoal(
                    accountUid = account.uid,
                    savingsGoalUid = "savings2",
                    transferUid = "abc-123",
                    topUpRequest = TopUpRequestEntity(amount = CurrencyEntity(currency = currency, minorUnits = amount)),
                )
                mockStarlingAPIService.addMoneyToSavingsGoal(
                    accountUid = account.uid,
                    savingsGoalUid = "savings2",
                    transferUid = "abc-123",
                    topUpRequest = TopUpRequestEntity(amount = CurrencyEntity(currency = currency, minorUnits = amount)),
                )
            }
            coVerify(exactly = 0) { mockStarlingAPIService.createSavingsGoal(any(), any()) }
        }

        @Test
        fun `Should return failure if get savings goal throws`() = runTest {
            // Given
            val account = Account(uid = "account1", categoryUid = "cat1", accountType = AccountType.PRIMARY)
            val currency = "GBP"
            val amount = 300L
            coEvery { mockStarlingAPIService.getSavingsGoals(accountUid = account.uid) } throws Exception()

            // When
            val result = starlingRepositoryImpl.updateRoundUpSavingsGoal(account = account, currency = currency, amount = amount)

            // Then
            assertTrue(result.isFailure)
            coVerify(exactly = 1) { mockStarlingAPIService.getSavingsGoals(accountUid = account.uid) }
            coVerify(exactly = 0) {
                mockStarlingAPIService.createSavingsGoal(any(), any())
                mockStarlingAPIService.addMoneyToSavingsGoal(any(), any(), any(), any())
            }
        }

        @Test
        fun `Should return failure if create savings goal throws`() = runTest {
            // Given
            val account = Account(uid = "account1", categoryUid = "cat1", accountType = AccountType.PRIMARY)
            val currency = "GBP"
            val amount = 2500L
            coEvery { mockStarlingAPIService.getSavingsGoals(accountUid = account.uid) } returns SavingsGoalListEntity(savingsGoalList = emptyList())
            coEvery {
                mockStarlingAPIService.createSavingsGoal(
                    accountUid = "account1",
                    createSavingsGoalRequestEntity = CreateSavingsGoalRequestEntity(
                        name = "Round Up Savings",
                        currency = currency,
                    ),
                )
            } throws Exception()

            // When
            val result = starlingRepositoryImpl.updateRoundUpSavingsGoal(account = account, currency = currency, amount = amount)

            // Then
            assertTrue(result.isFailure)
            coVerifyOrder {
                mockStarlingAPIService.getSavingsGoals(accountUid = account.uid)
                mockStarlingAPIService.createSavingsGoal(
                    accountUid = "account1",
                    createSavingsGoalRequestEntity = CreateSavingsGoalRequestEntity(
                        name = "Round Up Savings",
                        currency = currency,
                    ),
                )
            }
            coVerify(exactly = 0) { mockStarlingAPIService.addMoneyToSavingsGoal(any(), any(), any(), any()) }
        }

        @Test
        fun `Should return failure if create savings goal was not successful`() = runTest {
            // Given
            val account = Account(uid = "account1", categoryUid = "cat1", accountType = AccountType.PRIMARY)
            val currency = "GBP"
            val amount = 2500L
            coEvery { mockStarlingAPIService.getSavingsGoals(accountUid = account.uid) } returns SavingsGoalListEntity(savingsGoalList = emptyList())
            coEvery {
                mockStarlingAPIService.createSavingsGoal(
                    accountUid = "account1",
                    createSavingsGoalRequestEntity = CreateSavingsGoalRequestEntity(
                        name = "Round Up Savings",
                        currency = currency,
                    ),
                )
            } returns CreateSavingsGoalResponseEntity(savingsGoalUid = null, success = false)

            // When
            val result = starlingRepositoryImpl.updateRoundUpSavingsGoal(account = account, currency = currency, amount = amount)

            // Then
            assertTrue(result.isFailure)
            coVerifyOrder {
                mockStarlingAPIService.getSavingsGoals(accountUid = account.uid)
                mockStarlingAPIService.createSavingsGoal(
                    accountUid = "account1",
                    createSavingsGoalRequestEntity = CreateSavingsGoalRequestEntity(
                        name = "Round Up Savings",
                        currency = currency,
                    ),
                )
            }
            coVerify(exactly = 0) { mockStarlingAPIService.addMoneyToSavingsGoal(any(), any(), any(), any()) }
        }

        @Test
        fun `Should return failure if add money to savings goal throws`() = runTest {
            // Given
            val account = Account(uid = "account1", categoryUid = "cat1", accountType = AccountType.PRIMARY)
            val currency = "GBP"
            val amount = 300L
            val savingsGoalEntity1 = SavingsGoalEntity(savingsGoalUid = "savings1", name = "Round Up Savings")
            every { mockSavingsGoalEntityMapper.toSavingsGoal(savingsGoalEntity1) } returns SavingsGoal(uid = "savings1", name = "Round Up Savings")
            coEvery { mockStarlingAPIService.getSavingsGoals(accountUid = account.uid) } returns SavingsGoalListEntity(
                savingsGoalList = listOf(savingsGoalEntity1),
            )
            every { mockUuidFactory.random() } returns "abc-123"
            coEvery {
                mockStarlingAPIService.addMoneyToSavingsGoal(
                    accountUid = account.uid,
                    savingsGoalUid = "savings1",
                    transferUid = "abc-123",
                    topUpRequest = TopUpRequestEntity(amount = CurrencyEntity(currency = currency, minorUnits = amount)),
                )
            } throws Exception()

            // When
            val result = starlingRepositoryImpl.updateRoundUpSavingsGoal(account = account, currency = currency, amount = amount)

            // Then
            assertTrue(result.isFailure)
            coVerifyOrder {
                mockStarlingAPIService.getSavingsGoals(accountUid = account.uid)
                mockStarlingAPIService.addMoneyToSavingsGoal(
                    accountUid = account.uid,
                    savingsGoalUid = "savings1",
                    transferUid = "abc-123",
                    topUpRequest = TopUpRequestEntity(amount = CurrencyEntity(currency = currency, minorUnits = amount)),
                )
            }
            coVerify(exactly = 0) { mockStarlingAPIService.createSavingsGoal(any(), any()) }
        }

        @Test
        fun `Should return failure if add money to savings goal was not successful`() = runTest {
            // Given
            val account = Account(uid = "account1", categoryUid = "cat1", accountType = AccountType.PRIMARY)
            val currency = "GBP"
            val amount = 300L
            val savingsGoalEntity1 = SavingsGoalEntity(savingsGoalUid = "savings1", name = "Round Up Savings")
            every { mockSavingsGoalEntityMapper.toSavingsGoal(savingsGoalEntity1) } returns SavingsGoal(uid = "savings1", name = "Round Up Savings")
            coEvery { mockStarlingAPIService.getSavingsGoals(accountUid = account.uid) } returns SavingsGoalListEntity(
                savingsGoalList = listOf(savingsGoalEntity1),
            )
            every { mockUuidFactory.random() } returns "abc-123"
            coEvery {
                mockStarlingAPIService.addMoneyToSavingsGoal(
                    accountUid = account.uid,
                    savingsGoalUid = "savings1",
                    transferUid = "abc-123",
                    topUpRequest = TopUpRequestEntity(amount = CurrencyEntity(currency = currency, minorUnits = amount)),
                )
            } returns SavingsGoalTransferEntity(success = false)

            // When
            val result = starlingRepositoryImpl.updateRoundUpSavingsGoal(account = account, currency = currency, amount = amount)

            // Then
            assertTrue(result.isFailure)
            coVerifyOrder {
                mockStarlingAPIService.getSavingsGoals(accountUid = account.uid)
                mockStarlingAPIService.addMoneyToSavingsGoal(
                    accountUid = account.uid,
                    savingsGoalUid = "savings1",
                    transferUid = "abc-123",
                    topUpRequest = TopUpRequestEntity(amount = CurrencyEntity(currency = currency, minorUnits = amount)),
                )
            }
            coVerify(exactly = 0) { mockStarlingAPIService.createSavingsGoal(any(), any()) }
        }
    }
}
