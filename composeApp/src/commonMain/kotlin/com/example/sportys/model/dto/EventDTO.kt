package com.example.sportys.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class EventsResponse(
    val response: List<EventDTO>
)

@Serializable
data class EventDTO(
    val time: EventTimeDTO,
    val team: EventTeamDTO,
    val player: EventPlayerDTO? = null,
    val assist: EventPlayerDTO? = null,
    val type: String,
    val detail: String,
    val comments: String? = null
)

@Serializable
data class EventTimeDTO(
    val elapsed: Int,
    val extra: Int? = null
)

@Serializable
data class EventTeamDTO(
    val id: Int,
    val name: String,
    val logo: String? = null
)

@Serializable
data class EventPlayerDTO(
    val id: Int? = null,
    val name: String? = null
)