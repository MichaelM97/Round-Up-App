package com.michaelmccormick.features.roundup.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import com.michaelmccormick.core.models.Currency
import com.michaelmccormick.core.models.FeedItem
import com.michaelmccormick.core.models.FeedItemDirection
import com.michaelmccormick.core.ui.constants.Dimensions
import com.michaelmccormick.core.ui.extensions.format
import com.michaelmccormick.core.ui.extensions.getLocale
import com.michaelmccormick.features.roundup.ui.models.WeekOption
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun RoundUpScreen(viewModel: RoundUpScreenViewModel) {
    val scaffoldState = rememberScaffoldState()
    val state by viewModel.state.collectAsState()
    Scaffold(scaffoldState = scaffoldState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = it),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WeekSpinner(weeks = state.weekOptions, selectedWeek = state.selectedWeek, onWeekSelected = viewModel::onWeekSelected)
            RoundUpButton(roundUpAmount = state.roundUpAmount, onRoundUpSelected = viewModel::onRoundUpSelected)
            if (state.transactions.isNotEmpty()) {
                TransactionsList(transactions = state.transactions)
            } else if (!state.loading) {
                EmptyTransactionsList()
            }
        }
        LoadingOverlay(show = state.loading)
        SnackBar(viewState = state, scaffoldState = scaffoldState)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun WeekSpinner(weeks: List<WeekOption>, selectedWeek: WeekOption?, onWeekSelected: (WeekOption) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormatter = SimpleDateFormat("dd MMM yy", LocalContext.current.getLocale())
    ExposedDropdownMenuBox(
        modifier = Modifier.padding(top = Dimensions.NORMAL),
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        TextField(
            modifier = Modifier.testTag(RoundUpScreenTags.WEEK_SPINNER_TEXT_FIELD),
            value = selectedWeek?.let {
                stringResource(R.string.week_spinner_option, dateFormatter.format(it.minDate.time), dateFormatter.format(it.maxDate.time))
            } ?: stringResource(R.string.week_spinner_title),
            onValueChange = { },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            modifier = Modifier.testTag(RoundUpScreenTags.WEEK_SPINNER_MENU),
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            weeks.forEach {
                DropdownMenuItem(
                    onClick = {
                        onWeekSelected(it)
                        expanded = false
                    },
                ) {
                    Text(
                        text = stringResource(R.string.week_spinner_option, dateFormatter.format(it.minDate.time), dateFormatter.format(it.maxDate.time)),
                    )
                }
            }
        }
    }
}

@Composable
private fun RoundUpButton(
    roundUpAmount: Currency?,
    onRoundUpSelected: () -> Unit,
) {
    OutlinedButton(
        modifier = Modifier.padding(vertical = Dimensions.NORMAL),
        onClick = onRoundUpSelected,
        enabled = roundUpAmount != null,
    ) {
        Text(
            text = roundUpAmount?.let {
                stringResource(R.string.round_up_button_amount, roundUpAmount.format())
            } ?: stringResource(R.string.round_up_button),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TransactionsList(transactions: Map<Int, List<FeedItem>>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(RoundUpScreenTags.TRANSACTIONS_LIST),
        horizontalAlignment = Alignment.Start,
    ) {
        transactions.forEach {
            stickyHeader(
                key = it.key,
                contentType = Date::class,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colors.primaryVariant),
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = Dimensions.X_SMALL, horizontal = Dimensions.SMALL),
                        text = SimpleDateFormat("dd MMMM", LocalContext.current.getLocale())
                            .format(it.value.first().date),
                        style = MaterialTheme.typography.subtitle1,
                    )
                }
            }
            items(
                items = it.value,
                key = { transaction -> transaction.uid },
                contentType = { FeedItem::class },
            ) { transaction ->
                val amountText = if (transaction.direction == FeedItemDirection.IN) {
                    stringResource(R.string.inbound_transaction_amount, transaction.amount.format())
                } else {
                    stringResource(R.string.outbound_transaction_amount, transaction.amount.format())
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimensions.X_SMALL, horizontal = Dimensions.SMALL),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = transaction.counterPartyName,
                        style = MaterialTheme.typography.body1,
                    )
                    Text(
                        text = amountText,
                        style = MaterialTheme.typography.body1,
                    )
                }
                Divider()
            }
        }
    }
}

@Composable
private fun EmptyTransactionsList() {
    Text(text = stringResource(R.string.empty_transactions_list))
}

@Composable
private fun LoadingOverlay(show: Boolean) {
    if (show) {
        Dialog(onDismissRequest = { }) {
            CircularProgressIndicator(modifier = Modifier.testTag(RoundUpScreenTags.LOADING))
        }
    }
}

@Composable
private fun SnackBar(viewState: RoundUpScreenViewModel.ViewState, scaffoldState: ScaffoldState) {
    val message = stringResource(
        id = when (viewState.snackBarState) {
            RoundUpScreenViewModel.SnackBarState.GET_TRANSACTIONS_FAILURE -> R.string.get_transactions_error
            RoundUpScreenViewModel.SnackBarState.ROUND_UP_SUCCESS -> R.string.round_up_success
            RoundUpScreenViewModel.SnackBarState.ROUND_UP_FAILURE -> R.string.round_up_error
            else -> return
        },
    )
    LaunchedEffect(key1 = viewState.snackBarState) {
        scaffoldState.snackbarHostState.showSnackbar(message = message)
    }
}

object RoundUpScreenTags {
    const val LOADING = "LoadingTag"
    const val WEEK_SPINNER_TEXT_FIELD = "WeekSpinnerTextFieldTag"
    const val WEEK_SPINNER_MENU = "WeekSpinnerMenuTag"
    const val TRANSACTIONS_LIST = "TransactionsListTag"
}
