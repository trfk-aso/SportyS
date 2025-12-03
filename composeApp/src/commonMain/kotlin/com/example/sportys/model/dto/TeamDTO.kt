package com.example.sportys.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class TeamResponse(
    val response: List<TeamDataDTO>
)

@Serializable
data class TeamDataDTO(
    val team: TeamDTO
)

@Serializable
data class TeamDTO(
    val id: Int,
    val name: String,
    val logo: String? = null,
    val leagueId: Int? = null,
    val leagueName: String? = null,
    val position: Int? = null
)