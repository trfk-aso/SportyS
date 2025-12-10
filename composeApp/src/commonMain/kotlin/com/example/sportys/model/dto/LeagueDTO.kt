package com.example.sportys.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class LeagueResponse(
    val response: List<LeagueDataDTO>
)

@Serializable
data class LeagueDataDTO(
    val league: LeagueDTO,
    val country: CountryDTO? = null
)

@Serializable
data class CountryDTO(
    val name: String? = null,
    val code: String? = null,
    val flag: String? = null
)
@Serializable
data class LeagueDTO(
    val id: Int,
    val name: String,
    val type: String? = null,
    val logo: String? = null
)