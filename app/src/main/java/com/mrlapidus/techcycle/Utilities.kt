package com.mrlapidus.techcycle

import android.text.format.DateFormat
import java.util.*

object Utilities {
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