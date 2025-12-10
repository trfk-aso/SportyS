package com.example.sportys.model

import kotlinx.serialization.Serializable

@Serializable
data class LeagueShort(
    val id: String,
    val name: String,
    val country: String?,
    val logoUrl: String?
)