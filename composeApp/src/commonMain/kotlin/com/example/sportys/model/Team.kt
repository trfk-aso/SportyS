package com.example.sportys.model

data class Team(
    val id: String,
    val name: String,
    val logoUrl: String?,
    val leagueId: String?,
    val leagueName: String?,
    val position: Int?
)