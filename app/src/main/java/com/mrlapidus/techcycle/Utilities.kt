package com.mrlapidus.techcycle

import android.text.format.DateFormat
import java.util.*

object Utilities {
    const val STATUS_AVAILABLE = "Disponible"
    const val STATUS_SOLD = "Vendido"

    val CATEGORIES = arrayOf(
        "Todos",
        "Móviles",
        "Ordenadores",
        "Tabletas",
        "Cámaras",
        "Televisores",
        "Wearables",
        "Audio",
        "Consolas",
        "Accesorios"
    )

    val CATEGORY_ICONS = arrayOf(
        R.drawable.category_all_icon,
        R.drawable.category_mobile_icon,
        R.drawable.category_computer_icon,
        R.drawable.category_tablet_icon,
        R.drawable.category_camera_icon,
        R.drawable.category_tv_icon,
        R.drawable.category_wereable_icon,
        R.drawable.category_gadget_icon,
        R.drawable.category_audio_icon,
        R.drawable.category_game_icon,
        R.drawable.category_gadget_icon
    )


    val CONDITIONS = arrayOf(
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