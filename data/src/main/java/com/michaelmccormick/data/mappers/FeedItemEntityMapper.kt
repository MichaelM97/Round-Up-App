package com.michaelmccormick.data.mappers

import com.michaelmccormick.core.factories.SimpleDateFormatFactory
import com.michaelmccormick.core.models.FeedItem
import com.michaelmccormick.core.models.FeedItemDirection
import com.michaelmccormick.network.entities.FeedItemEntity
import java.text.ParseException
import java.util.Date
import javax.inject.Inject
import timber.log.Timber

internal class FeedItemEntityMapper @Inject constructor(
    private val currencyEntityMapper: CurrencyEntityMapper,
    private val simpleDateFormatFactory: SimpleDateFormatFactory,
) {
    fun toFeedItem(entity: FeedItemEntity): FeedItem? {
        return FeedItem(
            uid = entity.feedItemUid ?: return null,
            amount = entity.amount?.let { currencyEntityMapper.toCurrency(it) } ?: return null,
            direction = entity.direction?.toFeedItemDirection() ?: return null,
            date = entity.transactionTime?.toDate() ?: return null,
            counterPartyName = entity.counterPartyName ?: return null,
        )
    }

    private fun String.toFeedItemDirection(): FeedItemDirection? {
        return try {
            FeedItemDirection.valueOf(this)
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
            null
        }
    }

    private fun String.toDate(): Date? {
        return try {
            simpleDateFormatFactory.iso8601().parse(this)
        } catch (e: ParseException) {
            Timber.e(e)
            null
        }
    }
}
