package com.example.sportys.model

import kotlinx.serialization.Serializable

@Serializable
data class MatchScore(
    val home: Int,
    val away: Int
)

enum class MatchStatus {
    SCHEDULED,
    LIVE,
    FINISHED,
    POSTPONED,
    CANCELED
}