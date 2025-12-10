package com.example.sportys.model

import kotlinx.serialization.Serializable

@Serializable
data class TeamShort(
    val id: String,
    val name: String,
    val logoUrl: String?
)
