package com.michaelmccormick.network.entities

import androidx.annotation.RestrictTo
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CurrencyEntity @RestrictTo(RestrictTo.Scope.TESTS) constructor(
    val currency: String?,
    val minorUnits: Long?,
)
