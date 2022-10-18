package com.michaelmccormick.network.entities

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateSavingsGoalRequestEntity(
    val name: String,
    val currency: String,
)
