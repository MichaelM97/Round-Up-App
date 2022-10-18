package com.michaelmccormick.features.roundup.domain

import com.michaelmccormick.core.models.Account
import com.michaelmccormick.data.repositories.StarlingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
internal class UpdateRoundUpSavingsGoalUseCaseTest {
    private val mockStarlingRepository: StarlingRepository = mockk()
    private lateinit var updateRoundUpSavingsGoalUseCase: UpdateRoundUpSavingsGoalUseCase

    @BeforeEach
    fun before() {
        updateRoundUpSavingsGoalUseCase = UpdateRoundUpSavingsGoalUseCase(
            starlingRepository = mockStarlingRepository,
        )
    }

    @Test
    fun `Should update round up savings goal`() = runTest {
        // Given
        val currency = "GBP"
        val amount = 1500L
        val primaryAccount: Account = mockk()
        coEvery { mockStarlingRepository.getPrimaryAccount() } returns Result.success(primaryAccount)
        coEvery { mockStarlingRepository.updateRoundUpSavingsGoal(primaryAccount, currency, amount) } returns Result.success(Unit)

        // When
        val result = updateRoundUpSavingsGoalUseCase(currency, amount)

        // Then
        assertTrue(result.isSuccess)
        coVerifyOrder {
            mockStarlingRepository.getPrimaryAccount()
            mockStarlingRepository.updateRoundUpSavingsGoal(primaryAccount, currency, amount)
        }
    }

    @Test
    fun `Should return failure if getting primary account fails`() = runTest {
        // Given
        val currency = "GBP"
        val amount = 1500L
        val exception = Exception()
        coEvery { mockStarlingRepository.getPrimaryAccount() } returns Result.failure(exception)

        // When
        val result = updateRoundUpSavingsGoalUseCase(currency, amount)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { mockStarlingRepository.getPrimaryAccount() }
        coVerify(exactly = 0) { mockStarlingRepository.updateRoundUpSavingsGoal(any(), any(), any()) }
    }

    @Test
    fun `Should return failure if update round up savings goal fails`() = runTest {
        // Given
        val currency = "GBP"
        val amount = 1500L
        val primaryAccount: Account = mockk()
        coEvery { mockStarlingRepository.getPrimaryAccount() } returns Result.success(primaryAccount)
        val exception = Exception()
        coEvery { mockStarlingRepository.updateRoundUpSavingsGoal(primaryAccount, currency, amount) } returns Result.failure(exception)

        // When
        val result = updateRoundUpSavingsGoalUseCase(currency, amount)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerifyOrder {
            mockStarlingRepository.getPrimaryAccount()
            mockStarlingRepository.updateRoundUpSavingsGoal(primaryAccount, currency, amount)
        }
    }
}
