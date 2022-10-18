package com.michaelmccormick.network.entities

import androidx.annotation.RestrictTo
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SavingsGoalListEntity @RestrictTo(RestrictTo.Scope.TESTS) constructor(
    val savingsGoalList: List<SavingsGoalEntity>,
)
