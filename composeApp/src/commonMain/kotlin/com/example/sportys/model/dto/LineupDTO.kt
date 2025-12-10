package com.example.sportys.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class LineupsResponse(
    val response: List<LineupDTO>
)

@Serializable
data class LineupDTO(
    val team: LineupTeamDTO,
    val coach: CoachDTO? = null,
    val formation: String? = null,
    val startXI: List<LineupPlayerWrapperDTO> = emptyList(),
    val substitutes: List<LineupPlayerWrapperDTO> = emptyList()
)

@Serializable
data class LineupTeamDTO(
    val id: Int,
    val name: String,
    val logo: String? = null
)

@Serializable
data class CoachDTO(
    val id: Int? = null,
    val name: String? = null
)

@Serializable
data class LineupPlayerWrapperDTO(
    val player: LineupPlayerDTO
)

@Serializable
data class LineupPlayerDTO(
    val id: Int? = null,
    val name: String? = null,
    val number: Int? = null,
    val pos: String? = null,
    val grid: String? = null
)