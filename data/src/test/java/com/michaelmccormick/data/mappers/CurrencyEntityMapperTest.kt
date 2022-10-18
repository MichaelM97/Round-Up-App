package com.michaelmccormick.data.mappers

import com.michaelmccormick.core.models.Currency
import com.michaelmccormick.network.entities.CurrencyEntity
import java.util.stream.Stream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource

internal class CurrencyEntityMapperTest {
    private lateinit var currencyEntityMapper: CurrencyEntityMapper

    @BeforeEach
    fun before() {
        currencyEntityMapper = CurrencyEntityMapper()
    }

    @ParameterizedTest
    @ArgumentsSource(TestArgumentsProvider::class)
    fun `Should build expected model`(entity: CurrencyEntity, expectedModel: Currency?) {
        assertEquals(expectedModel, currencyEntityMapper.toCurrency(entity))
    }

    private class TestArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
            return Stream.of(
                Arguments.of(
                    CurrencyEntity(currency = null, minorUnits = 100),
                    null,
                ),
                Arguments.of(
                    CurrencyEntity(currency = "GBP", minorUnits = null),
                    null,
                ),
                Arguments.of(
                    CurrencyEntity(currency = "GBP", minorUnits = 199),
                    Currency(iso = "GBP", minorUnits = 199),
                ),
            )
        }
    }
}
