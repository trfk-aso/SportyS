package com.example.sportys.model

import kotlinx.datetime.Instant

data class Article(
    val id: String,
    val title: String,
    val summary: String?,
    val content: String?,
    val imageUrl: String?,
    val publishedAt: Instant,
    val source: String?,
    val tags: List<Tag>
)