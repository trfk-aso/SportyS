package com.example.sportys.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteItem(
    val id: Long,
    val type: FavoriteType,
    val itemId: String,
    val createdAt: Instant
)

enum class FavoriteType {
    ARTICLE,
    MATCH,
    TEAM,
    LEAGUE
}