package com.michaelmccormick.data.mappers

import com.michaelmccormick.core.models.SavingsGoal
import com.michaelmccormick.network.entities.SavingsGoalEntity
import java.util.stream.Stream
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource

internal class SavingsGoalEntityMapperTest {
    private lateinit var savingsGoalEntityMapper: SavingsGoalEntityMapper

    @BeforeEach
    fun before() {
        savingsGoalEntityMapper = SavingsGoalEntityMapper()
    }

    @ParameterizedTest
    @ArgumentsSource(TestArgumentsProvider::class)
    fun `Should build expected model`(entity: SavingsGoalEntity, expectedModel: SavingsGoal?) {
        assertEquals(expectedModel, savingsGoalEntityMapper.toSavingsGoal(entity))
    }

    private class TestArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
            return Stream.of(
                Arguments.of(
                    SavingsGoalEntity(savingsGoalUid = null, name = "Round Up"),
                    null,
                ),
                Arguments.of(
                    SavingsGoalEntity(savingsGoalUid = "123", name = null),
                    null,
                ),
                Arguments.of(
                    SavingsGoalEntity(savingsGoalUid = "123", name = "Round Up"),
                    SavingsGoal(uid = "123", name = "Round Up"),
                ),
            )
        }
    }
}
