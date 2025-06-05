package com.mrlapidus.techcycle.model

data class ReservationRequest(
    val buyerId: String = "",
    val buyerName: String = "",
    val buyerAvatarUrl: String = "",
    val fecha: Long = 0L,
    val estado: String = ""
)
