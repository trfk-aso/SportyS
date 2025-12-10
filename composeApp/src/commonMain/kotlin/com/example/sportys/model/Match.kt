package com.example.sportys.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Match(
    val id: String,
    val homeTeam: TeamShort,
    val awayTeam: TeamShort,
    val league: LeagueShort,
    val status: MatchStatus,
    val startTime: Instant,
    val score: MatchScore?,
    val events: List<Event> = emptyList(),
    val lineups: List<Lineup> = emptyList(),
    val statistics: List<TeamStatistics> = emptyList()
)
@Serializable
data class Event(
    val team: String?,
    val player: String?,
    val assist: String?,
    val type: String?,
    val detail: String?,
    val minute: Int
)
@Serializable
data class Lineup(
    val team: String,
    val coach: String?,
    val formation: String?,
    val startXI: List<PlayerShort>,
    val substitutes: List<PlayerShort>
)
@Serializable
data class PlayerShort(
    val name: String,
    val number: Int?,
    val position: String?,
    val photo: String? = null
)
@Serializable
data class TeamStatistics(
    val team: String,
    val stats: List<StatItem>
)
@Serializable
data class StatItem(
    val type: String,
    val value: String?
)