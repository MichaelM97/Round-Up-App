package com.michaelmccormick.network.entities

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TopUpRequestEntity(
    val amount: CurrencyEntity,
)
