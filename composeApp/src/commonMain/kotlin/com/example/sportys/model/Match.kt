package com.example.sportys.model

import kotlinx.datetime.Instant

data class Match(
    val id: String,
    val homeTeam: TeamShort,
    val awayTeam: TeamShort,
    val league: LeagueShort,
    val status: MatchStatus,
    val startTime: Instant,
    val score: MatchScore?,
    // события добавить
)