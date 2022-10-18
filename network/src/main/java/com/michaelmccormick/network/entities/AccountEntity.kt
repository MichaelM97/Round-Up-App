package com.michaelmccormick.network.entities

import androidx.annotation.RestrictTo
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AccountEntity @RestrictTo(RestrictTo.Scope.TESTS) constructor(
    val accountUid: String?,
    val defaultCategory: String?,
    val accountType: String?,
)
