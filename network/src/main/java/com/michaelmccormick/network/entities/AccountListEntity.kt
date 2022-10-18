package com.michaelmccormick.network.entities

import androidx.annotation.RestrictTo
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AccountListEntity @RestrictTo(RestrictTo.Scope.TESTS) constructor(
    val accounts: List<AccountEntity>,
)
