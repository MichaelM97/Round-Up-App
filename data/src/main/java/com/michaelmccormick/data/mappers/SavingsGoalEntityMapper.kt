package com.michaelmccormick.data.mappers

import com.michaelmccormick.core.models.SavingsGoal
import com.michaelmccormick.network.entities.SavingsGoalEntity
import javax.inject.Inject

internal class SavingsGoalEntityMapper @Inject constructor() {
    fun toSavingsGoal(entity: SavingsGoalEntity): SavingsGoal? {
        return SavingsGoal(
            uid = entity.savingsGoalUid ?: return null,
            name = entity.name ?: return null,
        )
    }
}
