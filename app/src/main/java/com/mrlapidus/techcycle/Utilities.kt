package com.mrlapidus.techcycle

import android.content.Context
import android.text.format.DateFormat
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
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

    fun saveAdToFavorites(context: Context, adId: String) {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios").child(uid).child("Favoritos").child(adId)
        val favData = mapOf("adId" to adId, "addedAt" to System.currentTimeMillis())

        ref.setValue(favData).addOnSuccessListener {
            Toast.makeText(context, "Añadido a favoritos", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Error al guardar favorito", Toast.LENGTH_SHORT).show()
        }
    }

    fun removeAdFromFavorites(context: Context, adId: String) {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios").child(uid).child("Favoritos").child(adId)

        ref.removeValue().addOnSuccessListener {
            Toast.makeText(context, "Favorito eliminado", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Error al eliminar favorito", Toast.LENGTH_SHORT).show()
        }
    }

}