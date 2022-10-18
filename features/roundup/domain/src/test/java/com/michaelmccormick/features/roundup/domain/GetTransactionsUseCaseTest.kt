package com.michaelmccormick.features.roundup.domain

import com.michaelmccormick.core.models.Account
import com.michaelmccormick.core.models.Currency
import com.michaelmccormick.core.models.FeedItem
import com.michaelmccormick.core.models.FeedItemDirection
import com.michaelmccormick.data.repositories.StarlingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
internal class GetTransactionsUseCaseTest {
    private val mockStarlingRepository: StarlingRepository = mockk()
    private lateinit var getTransactionsUseCase: GetTransactionsUseCase

    @BeforeEach
    fun before() {
        getTransactionsUseCase = GetTransactionsUseCase(
            starlingRepository = mockStarlingRepository,
        )
    }

    @Test
    fun `Should return transactions`() = runTest {
        // Given
        val minDate: Date = mockk()
        val maxDate: Date = mockk()
        val primaryAccount: Account = mockk()
        coEvery { mockStarlingRepository.getPrimaryAccount() } returns Result.success(primaryAccount)
        val transactions = listOf(
            FeedItem(
                uid = "123",
                amount = Currency("GBP", 100),
                direction = FeedItemDirection.OUT,
                date = mockk(),
                counterPartyName = "Shop",
            ),
            FeedItem(
                uid = "456",
                amount = Currency("GBP", 50000),
                direction = FeedItemDirection.IN,
                date = mockk(),
                counterPartyName = "Work",
            ),
        )
        coEvery { mockStarlingRepository.getTransactions(primaryAccount, minDate, maxDate) } returns Result.success(transactions)

        // When
        val result = getTransactionsUseCase(minDate, maxDate)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(transactions, result.getOrNull())
        coVerifyOrder {
            mockStarlingRepository.getPrimaryAccount()
            mockStarlingRepository.getTransactions(primaryAccount, minDate, maxDate)
        }
    }

    @Test
    fun `Should return failure if getting primary account fails`() = runTest {
        // Given
        val minDate: Date = mockk()
        val maxDate: Date = mockk()
        val exception = Exception()
        coEvery { mockStarlingRepository.getPrimaryAccount() } returns Result.failure(exception)

        // When
        val result = getTransactionsUseCase(minDate, maxDate)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { mockStarlingRepository.getPrimaryAccount() }
        coVerify(exactly = 0) { mockStarlingRepository.getTransactions(any(), any(), any()) }
    }

    @Test
    fun `Should return failure if get transactions fails`() = runTest {
        // Given
        val minDate: Date = mockk()
        val maxDate: Date = mockk()
        val primaryAccount: Account = mockk()
        coEvery { mockStarlingRepository.getPrimaryAccount() } returns Result.success(primaryAccount)
        val exception = Exception()
        coEvery { mockStarlingRepository.getTransactions(primaryAccount, minDate, maxDate) } returns Result.failure(exception)

        // When
        val result = getTransactionsUseCase(minDate, maxDate)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerifyOrder {
            mockStarlingRepository.getPrimaryAccount()
            mockStarlingRepository.getTransactions(primaryAccount, minDate, maxDate)
        }
    }
}
