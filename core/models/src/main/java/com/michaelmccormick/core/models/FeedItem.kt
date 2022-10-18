package com.michaelmccormick.core.models

import java.util.Date

data class FeedItem(
    val uid: String,
    val amount: Currency,
    val direction: FeedItemDirection,
    val date: Date,
    val counterPartyName: String,
)

enum class FeedItemDirection { IN, OUT }
