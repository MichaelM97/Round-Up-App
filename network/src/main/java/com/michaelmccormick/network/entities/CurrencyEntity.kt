package com.michaelmccormick.network.entities

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CurrencyEntity(
    val currency: String?,
    val minorUnits: Long?,
)
