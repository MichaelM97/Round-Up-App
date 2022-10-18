package com.michaelmccormick.features.roundup.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.michaelmccormick.core.models.Currency
import com.michaelmccormick.core.models.FeedItem
import com.michaelmccormick.core.models.FeedItemDirection
import com.michaelmccormick.features.roundup.ui.models.WeekOption
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

internal class RoundUpScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockRoundUpScreenViewModel: RoundUpScreenViewModel = mockk(relaxed = true)

    @Test
    fun shouldShowLoading() {
        // Given
        mockViewState(RoundUpScreenViewModel.ViewState(loading = true))

        // When
        renderComposable()

        // Then
        composeTestRule.onNodeWithTag(RoundUpScreenTags.LOADING)
            .assertIsDisplayed()
    }

    @Test
    fun shouldShowSelectedWeekOption() {
        // Given
        mockViewState(
            RoundUpScreenViewModel.ViewState(
                loading = false,
                selectedWeek = WeekOption(minDate = Date(2022, 9, 10), maxDate = Date(2022, 9, 16)),
            ),
        )

        // When
        renderComposable()

        // Then
        composeTestRule.onNodeWithTag(RoundUpScreenTags.WEEK_SPINNER_TEXT_FIELD)
            .assertIsDisplayed()
            .assertTextEquals("10 Oct 22 - 16 Oct 22")
    }

    @Test
    fun shouldCallViewModelWhenWeekOptionClicked() {
        // Given
        val weekOption1 = WeekOption(minDate = Date(2022, 9, 17), maxDate = Date(2022, 9, 23))
        val weekOption2 = WeekOption(minDate = Date(2022, 9, 10), maxDate = Date(2022, 9, 16))
        val weekOption3 = WeekOption(minDate = Date(2022, 9, 3), maxDate = Date(2022, 9, 9))
        mockViewState(
            RoundUpScreenViewModel.ViewState(
                loading = false,
                weekOptions = listOf(weekOption1, weekOption2, weekOption3),
            ),
        )
        renderComposable()

        // When
        composeTestRule.onNodeWithTag(RoundUpScreenTags.WEEK_SPINNER_TEXT_FIELD)
            .performClick()
        with(composeTestRule.onNodeWithTag(RoundUpScreenTags.WEEK_SPINNER_MENU)) {
            onChildAt(0).assertTextEquals("17 Oct 22 - 23 Oct 22")
            onChildAt(1).assertTextEquals("10 Oct 22 - 16 Oct 22")
            onChildAt(2)
                .assertTextEquals("03 Oct 22 - 09 Oct 22")
                .performClick()
        }

        // Then
        verify(exactly = 1) { mockRoundUpScreenViewModel.onWeekSelected(weekOption3) }
    }

    @Test
    fun shouldShowEmptyTransactions() {
        // Given
        mockViewState(
            RoundUpScreenViewModel.ViewState(
                loading = false,
                transactions = emptyMap(),
            ),
        )

        // When
        renderComposable()

        // Then
        composeTestRule.onNodeWithText("There are no transactions for the selected week")
            .assertIsDisplayed()
    }

    @Test
    fun shouldShowTransactions() {
        // Given
        val transactions = mapOf(
            1 to listOf(
                FeedItem("t1", Currency("GBP", 5357), FeedItemDirection.OUT, Date(2022, 10, 16), "Groceries"),
                FeedItem("t2", Currency("GBP", 9999), FeedItemDirection.IN, Date(2022, 10, 16), "Transfer"),
            ),
            2 to listOf(
                FeedItem("t3", Currency("GBP", 102050), FeedItemDirection.IN, Date(2022, 10, 17), "Work"),
                FeedItem("t4", Currency("GBP", 200), FeedItemDirection.OUT, Date(2022, 10, 17), "Shop"),
            ),
        )
        mockViewState(
            RoundUpScreenViewModel.ViewState(
                loading = false,
                transactions = transactions,
            ),
        )

        // When
        renderComposable()

        // Then
        with(composeTestRule.onNodeWithTag(RoundUpScreenTags.TRANSACTIONS_LIST)) {
            assertIsDisplayed()
            onChildAt(0).assertTextContains("Groceries")
            onChildAt(1).assertTextContains("£53.57")
            onChildAt(2).assertTextContains("Transfer")
            onChildAt(3).assertTextContains("+£99.99")
            onChildAt(5).assertTextContains("Work")
            onChildAt(6).assertTextContains("+£1,020.50")
            onChildAt(7).assertTextContains("Shop")
            onChildAt(8).assertTextContains("£2.00")
        }
    }

    @Test
    fun shouldCallViewModelWhenRoundUpClicked() {
        // Given
        mockViewState(
            RoundUpScreenViewModel.ViewState(
                loading = false,
                roundUpAmount = Currency("GBP", minorUnits = 2523),
            ),
        )
        renderComposable()

        // When
        composeTestRule.onNodeWithText("Round Up (£25.23)")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        // Then
        verify(exactly = 1) { mockRoundUpScreenViewModel.onRoundUpSelected() }
    }

    @Test
    fun shouldDisableRoundUpButtonWhenAmountNull() {
        // Given
        mockViewState(
            RoundUpScreenViewModel.ViewState(
                loading = false,
                roundUpAmount = null,
            ),
        )

        // When
        renderComposable()

        // Then
        composeTestRule.onNodeWithText("Round Up")
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun shouldShowSnackBar() {
        // Given
        mockViewState(
            RoundUpScreenViewModel.ViewState(
                loading = false,
                snackBarState = RoundUpScreenViewModel.SnackBarState.ROUND_UP_SUCCESS,
            ),
        )

        // When
        renderComposable()

        // Then
        composeTestRule.onNodeWithText("Funds successfully added to Round Up savings goal")
            .assertIsDisplayed()
    }

    private fun mockViewState(state: RoundUpScreenViewModel.ViewState) {
        every { mockRoundUpScreenViewModel.state } returns MutableStateFlow(state)
    }

    private fun renderComposable() {
        composeTestRule.setContent {
            RoundUpScreen(viewModel = mockRoundUpScreenViewModel)
        }
    }
}
