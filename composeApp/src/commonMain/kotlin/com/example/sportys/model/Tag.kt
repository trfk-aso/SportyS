package com.example.sportys.model

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