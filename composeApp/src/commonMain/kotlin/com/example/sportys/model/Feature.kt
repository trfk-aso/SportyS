package com.example.sportys.model

data class Feature(
    val id: String,
    val name: String,
    val isPaid: Boolean,
    val purchased: Boolean,
    val price: Double?
)