package com.michaelmccormick.data.mappers

import com.michaelmccormick.core.models.Account
import com.michaelmccormick.core.models.AccountType
import com.michaelmccormick.network.entities.AccountEntity
import java.util.stream.Stream
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource

internal class AccountEntityMapperTest {
    private lateinit var accountEntityMapper: AccountEntityMapper

    @BeforeEach
    fun before() {
        accountEntityMapper = AccountEntityMapper()
    }

    @ParameterizedTest
    @ArgumentsSource(TestArgumentsProvider::class)
    fun `Should build expected model`(entity: AccountEntity, expectedModel: Account?) {
        assertEquals(expectedModel, accountEntityMapper.toAccount(entity))
    }

    private class TestArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
            return Stream.of(
                Arguments.of(
                    AccountEntity(accountUid = null, defaultCategory = "cat1", accountType = "PRIMARY"),
                    null,
                ),
                Arguments.of(
                    AccountEntity(accountUid = "123", defaultCategory = null, accountType = "PRIMARY"),
                    null,
                ),
                Arguments.of(
                    AccountEntity(accountUid = "123", defaultCategory = "cat1", accountType = null),
                    null,
                ),
                Arguments.of(
                    AccountEntity(accountUid = "123", defaultCategory = "cat1", accountType = "INVALID"),
                    null,
                ),
                Arguments.of(
                    AccountEntity(accountUid = "123", defaultCategory = "cat1", accountType = "PRIMARY"),
                    Account(uid = "123", categoryUid = "cat1", accountType = AccountType.PRIMARY),
                ),
            )
        }
    }
}
