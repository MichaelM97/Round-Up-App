package com.michaelmccormick.features.roundup.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaelmccormick.core.factories.CalendarFactory
import com.michaelmccormick.core.models.Currency
import com.michaelmccormick.core.models.FeedItem
import com.michaelmccormick.core.models.FeedItemDirection
import com.michaelmccormick.features.roundup.domain.GetTransactionsUseCase
import com.michaelmccormick.features.roundup.domain.UpdateRoundUpSavingsGoalUseCase
import com.michaelmccormick.features.roundup.ui.models.WeekOption
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class RoundUpScreenViewModel @Inject internal constructor(
    private val calendarFactory: CalendarFactory,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val updateRoundUpSavingsGoalUseCase: UpdateRoundUpSavingsGoalUseCase,
) : ViewModel() {
    private val _state: MutableStateFlow<ViewState> = MutableStateFlow(ViewState(weekOptions = generateWeekOptions()))
    val state: StateFlow<ViewState> = _state.asStateFlow()

    init {
        onWeekSelected(state.value.weekOptions.first())
    }

    /**
     * Gets transactions from within the selected [week].
     */
    fun onWeekSelected(week: WeekOption) {
        viewModelScope.launch {
            _state.emit(state.value.copy(loading = true, selectedWeek = week, transactions = emptyMap(), roundUpAmount = null, snackBarState = null))
            getTransactionsUseCase(minDate = week.minDate, maxDate = week.maxDate)
                .onSuccess {
                    val groupedTransactions = it.groupBy { transaction -> transaction.date.day }
                    _state.emit(state.value.copy(loading = false, transactions = groupedTransactions, roundUpAmount = it.calculateRoundUpAmount()))
                }
                .onFailure {
                    _state.emit(state.value.copy(loading = false, snackBarState = SnackBarState.GET_TRANSACTIONS_FAILURE))
                }
        }
    }

    /**
     * Adds the current [roundUpAmount] to the Round Up savings goal.
     */
    fun onRoundUpSelected() {
        state.value.roundUpAmount?.let {
            viewModelScope.launch {
                _state.emit(state.value.copy(loading = true, snackBarState = null))
                updateRoundUpSavingsGoalUseCase(currency = it.iso, amount = it.minorUnits)
                    .onSuccess {
                        _state.emit(state.value.copy(loading = false, snackBarState = SnackBarState.ROUND_UP_SUCCESS))
                    }
                    .onFailure {
                        _state.emit(state.value.copy(loading = false, snackBarState = SnackBarState.ROUND_UP_FAILURE))
                    }
            }
        }
    }

    private fun generateWeekOptions(): List<WeekOption> {
        // Start at today's date
        val calendar = calendarFactory.now()
        // Change date to start of current week
        calendar.add(Calendar.DAY_OF_WEEK, -(calendar.get(Calendar.DAY_OF_WEEK) - 2))
        // Build a list of week options, count = WEEK_COUNT
        val weeks = mutableListOf<WeekOption>()
        for (i in 0 until WEEK_COUNT) {
            val firstDayOfWeek = calendar.time
            calendar.add(Calendar.DAY_OF_WEEK, 6)
            val lastDayOfWeek = calendar.time
            weeks.add(WeekOption(minDate = firstDayOfWeek, maxDate = lastDayOfWeek))
            // Change date to start of previous week
            calendar.add(Calendar.DAY_OF_WEEK, -13)
        }
        return weeks
    }

    private fun List<FeedItem>.calculateRoundUpAmount(): Currency? {
        if (this.isEmpty()) return null
        var total: Long = 0
        // Round up each outbound transaction to nearest pound
        this.forEach {
            if (it.direction == FeedItemDirection.OUT && (it.amount.minorUnits % 100) != 0L) {
                total += 100 - (it.amount.minorUnits % 100)
            }
        }
        return Currency(iso = this.first().amount.iso, minorUnits = total)
    }

    data class ViewState(
        val loading: Boolean = true,
        val weekOptions: List<WeekOption> = emptyList(),
        val selectedWeek: WeekOption? = null,
        val transactions: Map<Int, List<FeedItem>> = emptyMap(),
        val roundUpAmount: Currency? = null,
        val snackBarState: SnackBarState? = null,
    )

    enum class SnackBarState { GET_TRANSACTIONS_FAILURE, ROUND_UP_SUCCESS, ROUND_UP_FAILURE }

    private companion object {
        const val WEEK_COUNT = 26
    }
}
