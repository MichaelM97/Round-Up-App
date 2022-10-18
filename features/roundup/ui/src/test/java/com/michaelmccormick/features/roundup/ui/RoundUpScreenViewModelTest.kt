package com.michaelmccormick.features.roundup.ui

import app.cash.turbine.test
import com.michaelmccormick.core.factories.CalendarFactory
import com.michaelmccormick.core.models.Currency
import com.michaelmccormick.core.models.FeedItem
import com.michaelmccormick.core.models.FeedItemDirection
import com.michaelmccormick.core.test.DispatchersExtension
import com.michaelmccormick.features.roundup.domain.GetTransactionsUseCase
import com.michaelmccormick.features.roundup.domain.UpdateRoundUpSavingsGoalUseCase
import com.michaelmccormick.features.roundup.ui.models.WeekOption
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import java.util.Calendar
import java.util.Date
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class, DispatchersExtension::class)
internal class RoundUpScreenViewModelTest {
    private val mockCalendarFactory: CalendarFactory = mockk()
    private val mockGetTransactionsUseCase: GetTransactionsUseCase = mockk()
    private val mockUpdateRoundUpSavingsGoalUseCase: UpdateRoundUpSavingsGoalUseCase = mockk()
    private lateinit var roundUpScreenViewModel: RoundUpScreenViewModel

    @Nested
    inner class Init {
        @Test
        fun `Should set initial state and get transactions for current week`() {
            // Given
            val mockedWeekGeneration = mockWeekGeneration()
            val transaction1Date: Date = mockk<Date>().also { every { it.day } returns 1 }
            val transaction2Date: Date = mockk<Date>().also { every { it.day } returns 2 }
            val transaction3Date: Date = mockk<Date>().also { every { it.day } returns 1 }
            val transaction1 = FeedItem("t1", Currency("GBP", 1257), FeedItemDirection.OUT, transaction1Date, "Shop")
            val transaction2 = FeedItem("t2", Currency("GBP", 2550), FeedItemDirection.IN, transaction2Date, "Work")
            val transaction3 = FeedItem("t3", Currency("GBP", 9999), FeedItemDirection.OUT, transaction3Date, "Groceries")
            coEvery {
                mockGetTransactionsUseCase(
                    minDate = mockedWeekGeneration.weekOptions.first().minDate,
                    maxDate = mockedWeekGeneration.weekOptions.first().maxDate,
                )
            } returns Result.success(
                listOf(transaction1, transaction2, transaction3),
            )

            // When
            buildViewModel()

            // Then
            coVerifyOrder {
                mockCalendarFactory.now()
                mockedWeekGeneration.mockCalendar.add(Calendar.DAY_OF_WEEK, -3)
                for (i in 0 until 21) {
                    mockedWeekGeneration.mockCalendar.add(Calendar.DAY_OF_WEEK, 6)
                    mockedWeekGeneration.mockCalendar.add(Calendar.DAY_OF_WEEK, -13)
                }
                mockGetTransactionsUseCase(
                    minDate = mockedWeekGeneration.weekOptions.first().minDate,
                    maxDate = mockedWeekGeneration.weekOptions.first().maxDate,
                )
            }
            assertEquals(
                RoundUpScreenViewModel.ViewState(
                    loading = false,
                    weekOptions = mockedWeekGeneration.weekOptions,
                    selectedWeek = mockedWeekGeneration.weekOptions.first(),
                    transactions = mapOf(
                        1 to listOf(transaction1, transaction3),
                        2 to listOf(transaction2),
                    ),
                    roundUpAmount = Currency(iso = "GBP", minorUnits = 44),
                ),
                roundUpScreenViewModel.state.value,
            )
        }

        @Test
        fun `Should set initial state and error when get transactions fails`() {
            // Given
            val mockedWeekGeneration = mockWeekGeneration()
            coEvery {
                mockGetTransactionsUseCase(
                    minDate = mockedWeekGeneration.weekOptions.first().minDate,
                    maxDate = mockedWeekGeneration.weekOptions.first().maxDate,
                )
            } returns Result.failure(Exception())

            // When
            buildViewModel()

            // Then
            coVerifyOrder {
                mockCalendarFactory.now()
                mockedWeekGeneration.mockCalendar.add(Calendar.DAY_OF_WEEK, -3)
                for (i in 0 until 21) {
                    mockedWeekGeneration.mockCalendar.add(Calendar.DAY_OF_WEEK, 6)
                    mockedWeekGeneration.mockCalendar.add(Calendar.DAY_OF_WEEK, -13)
                }
                mockGetTransactionsUseCase(
                    minDate = mockedWeekGeneration.weekOptions.first().minDate,
                    maxDate = mockedWeekGeneration.weekOptions.first().maxDate,
                )
            }
            assertEquals(
                RoundUpScreenViewModel.ViewState(
                    loading = false,
                    weekOptions = mockedWeekGeneration.weekOptions,
                    selectedWeek = mockedWeekGeneration.weekOptions.first(),
                    snackBarState = RoundUpScreenViewModel.SnackBarState.GET_TRANSACTIONS_FAILURE,
                ),
                roundUpScreenViewModel.state.value,
            )
        }
    }

    @Nested
    inner class OnWeekSelected {
        private lateinit var mockedWeekGeneration: MockedWeekGeneration

        @BeforeEach
        fun before() {
            mockedWeekGeneration = mockWeekGeneration()
            coEvery {
                mockGetTransactionsUseCase(
                    minDate = mockedWeekGeneration.weekOptions.first().minDate,
                    maxDate = mockedWeekGeneration.weekOptions.first().maxDate,
                )
            } returns Result.success(emptyList())
            buildViewModel()
        }

        @Test
        fun `Should emit expected state when getting transactions using selected week`() = runTest {
            // Given
            val transaction1Date: Date = mockk<Date>().also { every { it.day } returns 9 }
            val transaction2Date: Date = mockk<Date>().also { every { it.day } returns 15 }
            val transaction3Date: Date = mockk<Date>().also { every { it.day } returns 23 }
            val transaction4Date: Date = mockk<Date>().also { every { it.day } returns 9 }
            val transaction1 = FeedItem("t1", Currency("GBP", 14020), FeedItemDirection.OUT, transaction1Date, "Shop")
            val transaction2 = FeedItem("t2", Currency("GBP", 6780), FeedItemDirection.OUT, transaction2Date, "Work")
            val transaction3 = FeedItem("t3", Currency("GBP", 7723), FeedItemDirection.OUT, transaction3Date, "Groceries")
            val transaction4 = FeedItem("t4", Currency("GBP", 100), FeedItemDirection.OUT, transaction4Date, "Shop")
            val selectedWeek = mockedWeekGeneration.weekOptions[1]
            coEvery {
                mockGetTransactionsUseCase(
                    minDate = selectedWeek.minDate,
                    maxDate = selectedWeek.maxDate,
                )
            } returns Result.success(
                listOf(transaction1, transaction2, transaction3, transaction4),
            )

            roundUpScreenViewModel.state.test {
                // When
                roundUpScreenViewModel.onWeekSelected(selectedWeek)

                // Then
                assertEquals(
                    RoundUpScreenViewModel.ViewState(
                        loading = false,
                        weekOptions = mockedWeekGeneration.weekOptions,
                        selectedWeek = mockedWeekGeneration.weekOptions.first(),
                    ),
                    awaitItem(),
                )
                assertEquals(
                    RoundUpScreenViewModel.ViewState(
                        loading = true,
                        weekOptions = mockedWeekGeneration.weekOptions,
                        selectedWeek = selectedWeek,
                    ),
                    awaitItem(),
                )
                assertEquals(
                    RoundUpScreenViewModel.ViewState(
                        loading = false,
                        weekOptions = mockedWeekGeneration.weekOptions,
                        selectedWeek = selectedWeek,
                        transactions = mapOf(
                            9 to listOf(transaction1, transaction4),
                            15 to listOf(transaction2),
                            23 to listOf(transaction3),
                        ),
                        roundUpAmount = Currency(iso = "GBP", minorUnits = 177),
                    ),
                    awaitItem(),
                )
                coVerify(exactly = 1) {
                    mockGetTransactionsUseCase(minDate = selectedWeek.minDate, maxDate = selectedWeek.maxDate)
                }
            }
        }

        @Test
        fun `Should emit expected state when getting transactions fails`() = runTest {
            // Given
            val selectedWeek = mockedWeekGeneration.weekOptions[1]
            coEvery {
                mockGetTransactionsUseCase(
                    minDate = selectedWeek.minDate,
                    maxDate = selectedWeek.maxDate,
                )
            } returns Result.failure(Exception())

            roundUpScreenViewModel.state.test {
                // When
                roundUpScreenViewModel.onWeekSelected(selectedWeek)

                // Then
                assertEquals(
                    RoundUpScreenViewModel.ViewState(
                        loading = false,
                        weekOptions = mockedWeekGeneration.weekOptions,
                        selectedWeek = mockedWeekGeneration.weekOptions.first(),
                    ),
                    awaitItem(),
                )
                assertEquals(
                    RoundUpScreenViewModel.ViewState(
                        loading = true,
                        weekOptions = mockedWeekGeneration.weekOptions,
                        selectedWeek = selectedWeek,
                    ),
                    awaitItem(),
                )
                assertEquals(
                    RoundUpScreenViewModel.ViewState(
                        loading = false,
                        weekOptions = mockedWeekGeneration.weekOptions,
                        selectedWeek = selectedWeek,
                        snackBarState = RoundUpScreenViewModel.SnackBarState.GET_TRANSACTIONS_FAILURE,
                    ),
                    awaitItem(),
                )
                coVerify(exactly = 1) {
                    mockGetTransactionsUseCase(minDate = selectedWeek.minDate, maxDate = selectedWeek.maxDate)
                }
            }
        }

        @Test
        fun `Should clear snack bar state when week selected after get transaction failure`() = runTest {
            // Given
            val selectedWeek = mockedWeekGeneration.weekOptions[1]
            coEvery {
                mockGetTransactionsUseCase(
                    minDate = selectedWeek.minDate,
                    maxDate = selectedWeek.maxDate,
                )
            } returns Result.failure(Exception())

            roundUpScreenViewModel.state.test {
                // When
                roundUpScreenViewModel.onWeekSelected(selectedWeek)
                roundUpScreenViewModel.onWeekSelected(selectedWeek)

                // Then
                assertEquals(
                    RoundUpScreenViewModel.ViewState(
                        loading = false,
                        weekOptions = mockedWeekGeneration.weekOptions,
                        selectedWeek = mockedWeekGeneration.weekOptions.first(),
                    ),
                    awaitItem(),
                )
                assertEquals(
                    RoundUpScreenViewModel.ViewState(
                        loading = true,
                        weekOptions = mockedWeekGeneration.weekOptions,
                        selectedWeek = selectedWeek,
                    ),
                    awaitItem(),
                )
                assertEquals(
                    RoundUpScreenViewModel.ViewState(
                        loading = false,
                        weekOptions = mockedWeekGeneration.weekOptions,
                        selectedWeek = selectedWeek,
                        snackBarState = RoundUpScreenViewModel.SnackBarState.GET_TRANSACTIONS_FAILURE,
                    ),
                    awaitItem(),
                )
                assertEquals(
                    RoundUpScreenViewModel.ViewState(
                        loading = true,
                        weekOptions = mockedWeekGeneration.weekOptions,
                        selectedWeek = selectedWeek,
                        snackBarState = null,
                    ),
                    awaitItem(),
                )
                coVerify(exactly = 2) {
                    mockGetTransactionsUseCase(minDate = selectedWeek.minDate, maxDate = selectedWeek.maxDate)
                }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class OnRoundUpSelected {
        private lateinit var mockedWeekGeneration: MockedWeekGeneration
        private lateinit var transaction1: FeedItem
        private lateinit var transaction2: FeedItem
        private lateinit var transaction3: FeedItem

        @BeforeEach
        fun before() {
            mockedWeekGeneration = mockWeekGeneration()
            val transaction1Date: Date = mockk<Date>().also { every { it.day } returns 1 }
            val transaction2Date: Date = mockk<Date>().also { every { it.day } returns 2 }
            val transaction3Date: Date = mockk<Date>().also { every { it.day } returns 1 }
            transaction1 = FeedItem("t1", Currency("GBP", 1257), FeedItemDirection.OUT, transaction1Date, "Shop")
            transaction2 = FeedItem("t2", Currency("GBP", 2550), FeedItemDirection.IN, transaction2Date, "Work")
            transaction3 = FeedItem("t3", Currency("GBP", 9999), FeedItemDirection.OUT, transaction3Date, "Groceries")
            coEvery {
                mockGetTransactionsUseCase(
                    minDate = mockedWeekGeneration.weekOptions.first().minDate,
                    maxDate = mockedWeekGeneration.weekOptions.first().maxDate,
                )
            } returns Result.success(listOf(transaction1, transaction2, transaction3))
            buildViewModel()
        }

        @Test
        fun `Should emit expected state when round up selected`() = runTest {
            // Given
            coEvery { mockUpdateRoundUpSavingsGoalUseCase(currency = "GBP", amount = 44) } returns Result.success(Unit)

            roundUpScreenViewModel.state.test {
                // When
                roundUpScreenViewModel.onRoundUpSelected()

                // Then
                assertEquals(
                    RoundUpScreenViewModel.ViewState(
                        loading = false,
                        weekOptions = mockedWeekGeneration.weekOptions,
                        selectedWeek = mockedWeekGeneration.weekOptions.first(),
                        transactions = mapOf(
                            1 to listOf(transaction1, transaction3),
                            2 to listOf(transaction2),
                        ),
                        roundUpAmount = Currency(iso = "GBP", minorUnits = 44),
                    ),
                    awaitItem(),
                )
                assertEquals(
                    RoundUpScreenViewModel.ViewState(
                        loading = true,
                        weekOptions = mockedWeekGeneration.weekOptions,
                        selectedWeek = mockedWeekGeneration.weekOptions.first(),
                        transactions = mapOf(
                            1 to listOf(transaction1, transaction3),
                            2 to listOf(transaction2),
                        ),
                        roundUpAmount = Currency(iso = "GBP", minorUnits = 44),
                    ),
                    awaitItem(),
                )
                assertEquals(
                    RoundUpScreenViewModel.ViewState(
                        loading = false,
                        weekOptions = mockedWeekGeneration.weekOptions,
                        selectedWeek = mockedWeekGeneration.weekOptions.first(),
                        transactions = mapOf(
                            1 to listOf(transaction1, transaction3),
                            2 to listOf(transaction2),
                        ),
                        roundUpAmount = Currency(iso = "GBP", minorUnits = 44),
                        snackBarState = RoundUpScreenViewModel.SnackBarState.ROUND_UP_SUCCESS,
                    ),
                    awaitItem(),
                )
                coVerify(exactly = 1) {
                    mockUpdateRoundUpSavingsGoalUseCase(currency = "GBP", amount = 44)
                }
            }
        }

        @Test
        fun `Should emit expected state when getting transactions fails`() = runTest {
            // Given
            coEvery { mockUpdateRoundUpSavingsGoalUseCase(currency = "GBP", amount = 44) } returns Result.failure(Exception())

            roundUpScreenViewModel.state.test {
                // When
                roundUpScreenViewModel.onRoundUpSelected()

                // Then
                assertEquals(
                    RoundUpScreenViewModel.ViewState(
                        loading = false,
                        weekOptions = mockedWeekGeneration.weekOptions,
                        selectedWeek = mockedWeekGeneration.weekOptions.first(),
                        transactions = mapOf(
                            1 to listOf(transaction1, transaction3),
                            2 to listOf(transaction2),
                        ),
                        roundUpAmount = Currency(iso = "GBP", minorUnits = 44),
                    ),
                    awaitItem(),
                )
                assertEquals(
                    RoundUpScreenViewModel.ViewState(
                        loading = true,
                        weekOptions = mockedWeekGeneration.weekOptions,
                        selectedWeek = mockedWeekGeneration.weekOptions.first(),
                        transactions = mapOf(
                            1 to listOf(transaction1, transaction3),
                            2 to listOf(transaction2),
                        ),
                        roundUpAmount = Currency(iso = "GBP", minorUnits = 44),
                    ),
                    awaitItem(),
                )
                assertEquals(
                    RoundUpScreenViewModel.ViewState(
                        loading = false,
                        weekOptions = mockedWeekGeneration.weekOptions,
                        selectedWeek = mockedWeekGeneration.weekOptions.first(),
                        transactions = mapOf(
                            1 to listOf(transaction1, transaction3),
                            2 to listOf(transaction2),
                        ),
                        roundUpAmount = Currency(iso = "GBP", minorUnits = 44),
                        snackBarState = RoundUpScreenViewModel.SnackBarState.ROUND_UP_FAILURE,
                    ),
                    awaitItem(),
                )
                coVerify(exactly = 1) {
                    mockUpdateRoundUpSavingsGoalUseCase(currency = "GBP", amount = 44)
                }
            }
        }

        @Test
        fun `Should clear snack bar state when round up selected again`() = runTest {
            // Given
            coEvery { mockUpdateRoundUpSavingsGoalUseCase(currency = "GBP", amount = 44) } returns Result.failure(Exception())

            roundUpScreenViewModel.state.test {
                // When
                roundUpScreenViewModel.onRoundUpSelected()
                roundUpScreenViewModel.onRoundUpSelected()

                // Then
                assertEquals(
                    RoundUpScreenViewModel.ViewState(
                        loading = false,
                        weekOptions = mockedWeekGeneration.weekOptions,
                        selectedWeek = mockedWeekGeneration.weekOptions.first(),
                        transactions = mapOf(
                            1 to listOf(transaction1, transaction3),
                            2 to listOf(transaction2),
                        ),
                        roundUpAmount = Currency(iso = "GBP", minorUnits = 44),
                    ),
                    awaitItem(),
                )
                assertEquals(
                    RoundUpScreenViewModel.ViewState(
                        loading = true,
                        weekOptions = mockedWeekGeneration.weekOptions,
                        selectedWeek = mockedWeekGeneration.weekOptions.first(),
                        transactions = mapOf(
                            1 to listOf(transaction1, transaction3),
                            2 to listOf(transaction2),
                        ),
                        roundUpAmount = Currency(iso = "GBP", minorUnits = 44),
                    ),
                    awaitItem(),
                )
                assertEquals(
                    RoundUpScreenViewModel.ViewState(
                        loading = false,
                        weekOptions = mockedWeekGeneration.weekOptions,
                        selectedWeek = mockedWeekGeneration.weekOptions.first(),
                        transactions = mapOf(
                            1 to listOf(transaction1, transaction3),
                            2 to listOf(transaction2),
                        ),
                        roundUpAmount = Currency(iso = "GBP", minorUnits = 44),
                        snackBarState = RoundUpScreenViewModel.SnackBarState.ROUND_UP_FAILURE,
                    ),
                    awaitItem(),
                )
                assertEquals(
                    RoundUpScreenViewModel.ViewState(
                        loading = true,
                        weekOptions = mockedWeekGeneration.weekOptions,
                        selectedWeek = mockedWeekGeneration.weekOptions.first(),
                        transactions = mapOf(
                            1 to listOf(transaction1, transaction3),
                            2 to listOf(transaction2),
                        ),
                        roundUpAmount = Currency(iso = "GBP", minorUnits = 44),
                        snackBarState = null,
                    ),
                    awaitItem(),
                )
                coVerify(exactly = 2) {
                    mockUpdateRoundUpSavingsGoalUseCase(currency = "GBP", amount = 44)
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `Should do nothing when round up amount is null`() = runTest {
            // Given
            coEvery {
                mockGetTransactionsUseCase(
                    minDate = mockedWeekGeneration.weekOptions[1].minDate,
                    maxDate = mockedWeekGeneration.weekOptions[1].maxDate,
                )
            } returns Result.success(emptyList())
            roundUpScreenViewModel.onWeekSelected(mockedWeekGeneration.weekOptions[1])

            roundUpScreenViewModel.state.test {
                // When
                roundUpScreenViewModel.onRoundUpSelected()

                // Then
                assertEquals(
                    RoundUpScreenViewModel.ViewState(
                        loading = false,
                        weekOptions = mockedWeekGeneration.weekOptions,
                        selectedWeek = mockedWeekGeneration.weekOptions[1],
                    ),
                    awaitItem(),
                )
                coVerify(exactly = 0) {
                    mockUpdateRoundUpSavingsGoalUseCase(currency = "GBP", amount = 44)
                }
            }
        }
    }

    private fun buildViewModel() {
        roundUpScreenViewModel = RoundUpScreenViewModel(
            calendarFactory = mockCalendarFactory,
            getTransactionsUseCase = mockGetTransactionsUseCase,
            updateRoundUpSavingsGoalUseCase = mockUpdateRoundUpSavingsGoalUseCase,
        )
    }

    private data class MockedWeekGeneration(
        val mockCalendar: Calendar,
        val weekOptions: List<WeekOption>,
    )

    private fun mockWeekGeneration(): MockedWeekGeneration {
        val mockCalendar: Calendar = mockk()
        every { mockCalendarFactory.now() } returns mockCalendar
        every { mockCalendar.get(Calendar.DAY_OF_WEEK) } returns 5
        every { mockCalendar.add(Calendar.DAY_OF_WEEK, -3) } just Runs
        val dateMocks: List<Date> = List(52) { mockk() }
        every { mockCalendar.time } returnsMany dateMocks
        every { mockCalendar.add(Calendar.DAY_OF_WEEK, 6) } just Runs
        every { mockCalendar.add(Calendar.DAY_OF_WEEK, -13) } just Runs
        val weekOptions = dateMocks.chunked(2).map { WeekOption(minDate = it.first(), maxDate = it[1]) }
        return MockedWeekGeneration(
            mockCalendar = mockCalendar,
            weekOptions = weekOptions,
        )
    }
}
