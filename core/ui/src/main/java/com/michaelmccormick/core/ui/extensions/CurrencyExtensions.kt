package com.michaelmccormick.core.ui.extensions

import com.michaelmccormick.core.models.Currency
import java.text.NumberFormat

fun Currency.format(): String {
    val format = NumberFormat.getCurrencyInstance()
    format.currency = java.util.Currency.getInstance(this.iso)
    format.maximumFractionDigits = 2
    val value: Double = this.minorUnits / 100.0
    return format.format(value)
}
