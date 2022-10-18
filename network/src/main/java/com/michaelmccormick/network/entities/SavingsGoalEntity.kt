package com.michaelmccormick.network.entities

import androidx.annotation.RestrictTo
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SavingsGoalEntity @RestrictTo(RestrictTo.Scope.TESTS) constructor(
    val savingsGoalUid: String?,
    val name: String?,
)
