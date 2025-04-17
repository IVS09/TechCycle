package com.mrlapidus.techcycle.model

data class AdModel(
    var id: String = "",
    var userId: String = "",
    var brand: String = "",
    var category: String = "",
    var condition: String = "",
    var location: String = "",
    var price: String = "",
    var title: String = "",
    var description: String = "",
    var status: String = "Disponible",
    var timestamp: Long = 0,
    var latitud: Double = 0.0,
    var longitud: Double = 0.0,
    var isFavorite: Boolean = false,
    var viewCount: Int = 0,
    var imageUrls: List<String> = listOf()
)