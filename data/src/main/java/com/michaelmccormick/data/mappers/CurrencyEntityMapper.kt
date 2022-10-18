package com.michaelmccormick.data.mappers

import com.michaelmccormick.core.models.Currency
import com.michaelmccormick.network.entities.CurrencyEntity
import javax.inject.Inject

internal class CurrencyEntityMapper @Inject constructor() {
    fun toCurrency(entity: CurrencyEntity): Currency? {
        return Currency(
            iso = entity.currency ?: return null,
            minorUnits = entity.minorUnits ?: return null,
        )
    }
}
