package com.example.sportys.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class NewsApiResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<ArticleDTO>
)

@Serializable
data class ArticleDTO(
    val title: String,
    val description: String? = null,
    val content: String? = null,
    val image: String? = null,
    val publishedAt: String,
    val source: NewsSourceDTO
)

@Serializable
data class NewsSourceDTO(
    val name: String?
)