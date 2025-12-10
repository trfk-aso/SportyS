package com.example.sportys.model.dto

import com.example.sportys.model.StatValueSerializer
import kotlinx.serialization.Serializable

@Serializable
data class StatisticsResponse(
    val response: List<StatisticsDTO>
)

@Serializable
data class StatisticsDTO(
    val team: StatsTeamDTO,
    val statistics: List<StatItemDTO>
)

@Serializable
data class StatsTeamDTO(
    val id: Int,
    val name: String,
    val logo: String? = null
)

@Serializable
data class StatItemDTO(
    val type: String,

    @Serializable(with = StatValueSerializer::class)
    val value: String?
)