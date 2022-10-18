package com.michaelmccormick.network.entities

import androidx.annotation.RestrictTo
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateSavingsGoalResponseEntity @RestrictTo(RestrictTo.Scope.TESTS) constructor(
    val savingsGoalUid: String?,
    val success: Boolean?,
)
