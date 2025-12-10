package com.example.sportys.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class LSResponse(
    val matches: List<LSMatch>
)

@Serializable
data class LSMatch(
    val id: Int,
    val date: String,
    val time: String,
    val league: String,
    val home: String,
    val away: String,
    val homeLogo: String? = null,
    val awayLogo: String? = null
)