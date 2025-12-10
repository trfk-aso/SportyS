package com.example.sportys.model

data class Team(
    val id: String,
    val name: String,
    val logoUrl: String?,
    val leagueId: String?,
    val leagueName: String?,
    val position: Int?
)

data class TeamLeagueStats(
    val teamId: String,
    val name: String,
    val logoUrl: String?,
    val position: Int,
    val played: Int,
    val points: Int,

    val periodPlayed: Int = played,
    val periodPoints: Int = points,
    val periodGoalsFor: Int = 0,
    val periodGoalsAgainst: Int = 0
)