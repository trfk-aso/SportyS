package com.example.sportys.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class MatchResponse(
    val response: List<MatchDTO>
)

@Serializable
data class MatchDTO(
    val fixture: FixtureDTO,
    val league: LeagueShortDTO,
    val teams: MatchTeamsDTO,
    val goals: GoalsDTO
)

@Serializable
data class FixtureDTO(
    val id: Int,
    val date: String,
    val status: StatusDTO
)

@Serializable
data class StatusDTO(
    val short: String
)

@Serializable
data class MatchTeamsDTO(
    val home: TeamShortDTO,
    val away: TeamShortDTO
)

@Serializable
data class TeamShortDTO(
    val id: Int,
    val name: String,
    val logo: String? = null
)

@Serializable
data class GoalsDTO(
    val home: Int? = null,
    val away: Int? = null
)

@Serializable
data class LeagueShortDTO(
    val id: Int,
    val name: String,
    val country: String? = null,
    val logo: String? = null
)