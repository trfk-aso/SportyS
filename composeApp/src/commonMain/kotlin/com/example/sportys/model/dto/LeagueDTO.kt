package com.example.sportys.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class LeagueResponse(
    val response: List<LeagueDataDTO>
)

@Serializable
data class LeagueDataDTO(
    val league: LeagueDTO
)

@Serializable
data class LeagueDTO(
    val id: Int,
    val name: String,
    val country: String?,
    val logo: String?
)