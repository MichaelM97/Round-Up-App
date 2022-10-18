package com.michaelmccormick.network.entities

import androidx.annotation.RestrictTo
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeedItemEntity @RestrictTo(RestrictTo.Scope.TESTS) constructor(
    val feedItemUid: String?,
    val amount: CurrencyEntity?,
    val direction: String?,
    val transactionTime: String?,
    val counterPartyName: String?,
)
