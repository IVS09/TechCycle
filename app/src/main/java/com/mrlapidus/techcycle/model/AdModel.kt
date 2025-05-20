package com.mrlapidus.techcycle.model

data class AdModel(
    val id: String = "",
    val userId: String = "",
    val brand: String = "",
    val category: String = "",
    val condition: String = "",
    val location: String = "",
    val price: Double = 0.0,
    val title: String = "",
    val description: String = "",
    val status: String = "Disponible",
    val timestamp: Long = 0,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    var isFavorite: Boolean = false,
    val viewCount: Int = 0,
    val imageUrls: List<String> = emptyList()
)

