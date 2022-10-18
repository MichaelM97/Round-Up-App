package com.michaelmccormick.roundupapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.michaelmccormick.core.models.Currency
import com.michaelmccormick.core.models.FeedItem
import com.michaelmccormick.core.models.FeedItemDirection
import com.michaelmccormick.features.roundup.ui.RoundUpScreenTags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
@HiltAndroidTest
internal class MainActivityTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var fakeStarlingRepositoryImpl: FakeStarlingRepositoryImpl

    @Before
    fun before() {
        hiltRule.inject()
    }

    @Test
    fun shouldShowEmptyTransactionsWhenUnpopulatedWeekSelected() = runTest {
        // Given
        fakeStarlingRepositoryImpl.getTransactionsResult = Result.success(emptyList())

        // When
        composeTestRule.awaitIdle()

        // Then
        composeTestRule.onNodeWithText("There are no transactions for the selected week")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Round Up")
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun shouldShowTransactionsWhenPopulatedWeekSelected() = runTest {
        // Given
        val transactions = listOf(
            FeedItem("t1", Currency("GBP", 5357), FeedItemDirection.OUT, Date(2022, 10, 16), "Groceries"),
            FeedItem("t2", Currency("GBP", 9999), FeedItemDirection.IN, Date(2022, 10, 16), "Transfer"),
            FeedItem("t3", Currency("GBP", 102050), FeedItemDirection.IN, Date(2022, 10, 17), "Work"),
            FeedItem("t4", Currency("GBP", 200), FeedItemDirection.OUT, Date(2022, 10, 17), "Shop"),
        )
        fakeStarlingRepositoryImpl.getTransactionsResult = Result.success(transactions)

        // When
        composeTestRule.awaitIdle()

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
    fun shouldShowErrorWhenGetTransactionsFails() = runTest {
        // Given
        fakeStarlingRepositoryImpl.getTransactionsResult = Result.failure(Exception())

        // When
        composeTestRule.awaitIdle()

        // Then
        composeTestRule.onNodeWithText("Error when fetching transactions")
            .assertIsDisplayed()
    }

    @Test
    fun shouldShowSuccessWhenRoundUpSucceeds() = runTest {
        // Given
        val transactions = listOf(
            FeedItem("t1", Currency("GBP", 5357), FeedItemDirection.OUT, Date(2022, 10, 16), "Groceries"),
        )
        fakeStarlingRepositoryImpl.getTransactionsResult = Result.success(transactions)
        fakeStarlingRepositoryImpl.updateRoundUpSavingsGoalResult = Result.success(Unit)
        composeTestRule.awaitIdle()

        // When
        composeTestRule.onNodeWithText("Round Up (£0.43)")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        // Then
        composeTestRule.onNodeWithText("Funds successfully added to Round Up savings goal")
            .assertIsDisplayed()
    }

    @Test
    fun shouldShowErrorWhenRoundUpFails() = runTest {
        // Given
        val transactions = listOf(
            FeedItem("t1", Currency("GBP", 5357), FeedItemDirection.OUT, Date(2022, 10, 16), "Groceries"),
        )
        fakeStarlingRepositoryImpl.getTransactionsResult = Result.success(transactions)
        fakeStarlingRepositoryImpl.updateRoundUpSavingsGoalResult = Result.failure(Exception())
        composeTestRule.awaitIdle()

        // When
        composeTestRule.onNodeWithText("Round Up (£0.43)")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        // Then
        composeTestRule.onNodeWithText("Error adding funds to Round Up savings goal")
            .assertIsDisplayed()
    }
}
