package com.michaelmccormick.data.mappers

import com.michaelmccormick.core.models.Account
import com.michaelmccormick.core.models.AccountType
import com.michaelmccormick.network.entities.AccountEntity
import javax.inject.Inject
import timber.log.Timber

internal class AccountEntityMapper @Inject constructor() {
    fun toAccount(entity: AccountEntity): Account? {
        return Account(
            uid = entity.accountUid ?: return null,
            categoryUid = entity.defaultCategory ?: return null,
            accountType = entity.accountType?.toAccountType() ?: return null,
        )
    }

    private fun String.toAccountType(): AccountType? {
        return try {
            AccountType.valueOf(this)
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
            null
        }
    }
}
