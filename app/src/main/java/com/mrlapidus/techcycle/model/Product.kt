package com.mrlapidus.techcycle.model

data class Product(
    val id: String = "",
    val title: String = "",
    val price: Double = 0.0,
    val condition: String = "",
    val category: String = "",
    val brand: String = "",
    val location: String = "",
    val description: String = "",
    val sellerId: String = "",
    val imageUrls: List<String> = emptyList()
)
