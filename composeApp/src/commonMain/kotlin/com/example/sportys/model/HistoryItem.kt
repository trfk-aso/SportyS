package com.example.sportys.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

enum class HistoryType { ARTICLE, MATCH}

@Serializable
data class HistoryItem(
    val id: Long,
    val type: HistoryType,
    val itemId: String,
    val openedAt: Instant,
    val article: Article? = null,
    val match: Match? = null
)
