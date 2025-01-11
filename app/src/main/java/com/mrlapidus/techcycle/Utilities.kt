package com.mrlapidus.techcycle

import android.text.format.DateFormat
import java.util.*

object Utilities {
    const val STATUS_AVAILABLE = "Disponible"
    const val STATUS_SOLD = "Vendido"

    val CATEGORIES = listOf(
        "Todos",
        "Móviles",
        "Ordenadores",
        "Tabletas",
        "Cámaras",
        "Televisores",
        "Wearables",
        "Drones",
        "Audio",
        "Consolas",
        "Accesorios"
    )

    val CONDITIONS = listOf(
        "Nuevo",
        "Usado",
        "Reacondicionado"
    )
    // Devuelve el tiempo actual en formato timestamp
    @Suppress("unused")
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }

    // Convierte un timestamp en una fecha legible como cadena de texto
    fun formatTimestampToDate(timestamp: Long): String {
        if (timestamp == 0L) return "Información no disponible"
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp
        return DateFormat.format("dd/MM/yyyy", calendar).toString()
    }
}