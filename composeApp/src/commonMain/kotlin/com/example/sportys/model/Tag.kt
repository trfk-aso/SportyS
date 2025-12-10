package com.example.sportys.model

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val type: TagType,
    val id: String,
    val name: String
)

enum class TagType {
    TEAM,
    LEAGUE,
    TOURNAMENT,
    PLAYER,
    OTHER
}