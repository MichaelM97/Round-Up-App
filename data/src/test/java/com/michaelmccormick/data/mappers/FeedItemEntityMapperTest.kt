package com.michaelmccormick.data.mappers

import com.michaelmccormick.core.factories.SimpleDateFormatFactory
import com.michaelmccormick.core.models.Currency
import com.michaelmccormick.core.models.FeedItem
import com.michaelmccormick.core.models.FeedItemDirection
import com.michaelmccormick.network.entities.CurrencyEntity
import com.michaelmccormick.network.entities.FeedItemEntity
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import java.text.ParseException
import java.util.Date
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource

@ExtendWith(MockKExtension::class)
internal class FeedItemEntityMapperTest {
    private val mockCurrencyEntityMapper: CurrencyEntityMapper = mockk()
    private val mockSimpleDateFormatFactory: SimpleDateFormatFactory = mockk()
    private lateinit var feedItemEntityMapper: FeedItemEntityMapper

    @BeforeEach
    fun before() {
        feedItemEntityMapper = FeedItemEntityMapper(
            currencyEntityMapper = mockCurrencyEntityMapper,
            simpleDateFormatFactory = mockSimpleDateFormatFactory,
        )
    }

    @Test
    fun `Should build model from valid entity`() {
        // Given
        val currencyEntity = CurrencyEntity(currency = "GBP", minorUnits = 100)
        val currency = Currency(iso = "GBP", minorUnits = 100)
        every { mockCurrencyEntityMapper.toCurrency(currencyEntity) } returns currency
        val entity = FeedItemEntity(
            feedItemUid = "123",
            amount = currencyEntity,
            direction = "OUT",
            transactionTime = "2022-10-16T15:30:00.00Z",
            counterPartyName = "Shop",
        )
        val mockDate: Date = mockk()
        every { mockSimpleDateFormatFactory.iso8601().parse("2022-10-16T15:30:00.00Z") } returns mockDate

        // When
        val model = feedItemEntityMapper.toFeedItem(entity)

        // Then
        assertEquals(
            FeedItem(
                uid = "123",
                amount = currency,
                direction = FeedItemDirection.OUT,
                date = mockDate,
                counterPartyName = "Shop",
            ),
            model,
        )
    }

    @Test
    fun `Should return null when currency is invalid`() {
        // Given
        val currencyEntity = CurrencyEntity(currency = "GBP", minorUnits = null)
        every { mockCurrencyEntityMapper.toCurrency(currencyEntity) } returns null
        val entity = FeedItemEntity(
            feedItemUid = "123",
            amount = currencyEntity,
            direction = "OUT",
            transactionTime = "2022-10-16T15:30:00.00Z",
            counterPartyName = "Shop",
        )
        val mockDate: Date = mockk()
        every { mockSimpleDateFormatFactory.iso8601().parse("2022-10-16T15:30:00.00Z") } returns mockDate

        // When
        val model = feedItemEntityMapper.toFeedItem(entity)

        // Then
        assertNull(model)
    }

    @Test
    fun `Should return null when direction is invalid`() {
        // Given
        val currencyEntity = CurrencyEntity(currency = "GBP", minorUnits = 100)
        val currency = Currency(iso = "GBP", minorUnits = 100)
        every { mockCurrencyEntityMapper.toCurrency(currencyEntity) } returns currency
        val entity = FeedItemEntity(
            feedItemUid = "123",
            amount = currencyEntity,
            direction = "INVALID",
            transactionTime = "2022-10-16T15:30:00.00Z",
            counterPartyName = "Shop",
        )
        val mockDate: Date = mockk()
        every { mockSimpleDateFormatFactory.iso8601().parse("2022-10-16T15:30:00.00Z") } returns mockDate

        // When
        val model = feedItemEntityMapper.toFeedItem(entity)

        // Then
        assertNull(model)
    }

    @Test
    fun `Should return null when date is invalid`() {
        // Given
        val currencyEntity = CurrencyEntity(currency = "GBP", minorUnits = 100)
        val currency = Currency(iso = "GBP", minorUnits = 100)
        every { mockCurrencyEntityMapper.toCurrency(currencyEntity) } returns currency
        val entity = FeedItemEntity(
            feedItemUid = "123",
            amount = currencyEntity,
            direction = "OUT",
            transactionTime = "2022-10-16T15:30:00.00Z",
            counterPartyName = "Shop",
        )
        every { mockSimpleDateFormatFactory.iso8601().parse("2022-10-16T15:30:00.00Z") } throws ParseException("", 0)

        // When
        val model = feedItemEntityMapper.toFeedItem(entity)

        // Then
        assertNull(model)
    }

    @ParameterizedTest
    @ArgumentsSource(NullValueTestArgumentsProvider::class)
    fun `Should return null when mandatory field is null`(entity: FeedItemEntity) {
        // Given
        every { mockCurrencyEntityMapper.toCurrency(CurrencyEntity(currency = "GBP", minorUnits = 100)) } returns mockk()
        every { mockSimpleDateFormatFactory.iso8601().parse("2022-10-16T15:30:00.00Z") } returns mockk()

        // When
        val model = feedItemEntityMapper.toFeedItem(entity)

        // Then
        assertNull(model)
    }

    private class NullValueTestArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
            return Stream.of(
                Arguments.of(
                    FeedItemEntity(
                        feedItemUid = null,
                        amount = CurrencyEntity(currency = "GBP", minorUnits = 100),
                        direction = "OUT",
                        transactionTime = "2022-10-16T15:30:00.00Z",
                        counterPartyName = "Shop",
                    ),
                ),
                Arguments.of(
                    FeedItemEntity(
                        feedItemUid = "123",
                        amount = null,
                        direction = "OUT",
                        transactionTime = "2022-10-16T15:30:00.00Z",
                        counterPartyName = "Shop",
                    ),
                ),
                Arguments.of(
                    FeedItemEntity(
                        feedItemUid = "123",
                        amount = CurrencyEntity(currency = "GBP", minorUnits = 100),
                        direction = null,
                        transactionTime = "2022-10-16T15:30:00.00Z",
                        counterPartyName = "Shop",
                    ),
                ),
                Arguments.of(
                    FeedItemEntity(
                        feedItemUid = "123",
                        amount = CurrencyEntity(currency = "GBP", minorUnits = 100),
                        direction = "OUT",
                        transactionTime = null,
                        counterPartyName = "Shop",
                    ),
                ),
                Arguments.of(
                    FeedItemEntity(
                        feedItemUid = "123",
                        amount = CurrencyEntity(currency = "GBP", minorUnits = 100),
                        direction = "OUT",
                        transactionTime = "2022-10-16T15:30:00.00Z",
                        counterPartyName = null,
                    ),
                ),
            )
        }
    }
}
