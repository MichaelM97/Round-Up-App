package com.michaelmccormick.network.entities

import androidx.annotation.RestrictTo
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateSavingsGoalRequestEntity @RestrictTo(RestrictTo.Scope.TESTS) constructor(
    val name: String,
    val currency: String,
)
