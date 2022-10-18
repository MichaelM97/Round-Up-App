package com.michaelmccormick.core.models

data class Account(
    val uid: String,
    val categoryUid: String,
    val accountType: AccountType,
)

enum class AccountType { PRIMARY, ADDITIONAL, LOAN }
