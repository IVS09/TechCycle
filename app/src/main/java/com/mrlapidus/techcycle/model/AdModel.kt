package com.mrlapidus.techcycle.model

/**
 * Modelo de datos para representar el anuncio en la aplicación.
 */
data class AdModel(
    val id: String = "",
    val userId: String = "",
    val brand: String = "",
    val category: String = "",
    val condition: String = "",
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val price: Double = 0.0,
    val title: String = "",
    val description: String = "",
    val status: String = "Disponible",
    val timestamp: Long = 0L,
    val isFavorite: Boolean = false,
    val viewCount: Int = 0,
    val imageUrl: String = ""
)
