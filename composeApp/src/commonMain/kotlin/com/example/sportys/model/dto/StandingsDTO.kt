package com.example.sportys.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class StandingsResponseDTO(
    val response: List<LeagueStandingsDTO>
)

@Serializable
data class LeagueStandingsDTO(
    val league: StandingsLeagueDTO
)

@Serializable
data class StandingsLeagueDTO(
    val id: Int,
    val name: String,
    val standings: List<List<StandingDTO>>
)

@Serializable
data class StandingDTO(
    val rank: Int,
    val team: StandingTeamDTO,
    val points: Int,
    val goalsDiff: Int,
    val all: StandingAllDTO,
    val group: String? = null,
    val form: String? = null,
    val status: String? = null,
    val description: String? = null
)

@Serializable
data class StandingAllDTO(
    val played: Int,
    val win: Int,
    val draw: Int,
    val lose: Int
)

@Serializable
data class StandingTeamDTO(
    val id: Int,
    val name: String,
    val logo: String? = null
)