package com.example.sportys.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class GNewsResponse(
    val articles: List<ArticleDTO>
)

@Serializable
data class SourceDTO(
    val name: String
)