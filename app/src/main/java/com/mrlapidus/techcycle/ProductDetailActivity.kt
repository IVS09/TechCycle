package com.mrlapidus.techcycle

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.mrlapidus.techcycle.adapter.ImageSliderAdapter
import com.mrlapidus.techcycle.databinding.ActivityProductDetailBinding

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var firebaseAuth: FirebaseAuth

    // 🔥 NUEVO – referencia directa al anuncio para escuchar cambios
    private lateinit var adRef: DatabaseReference

    private var adId: String = ""
    private var ownerId: String = ""           // 🔥 NUEVO
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        adId    = intent.getStringExtra("adId") ?: ""
        ownerId = intent.getStringExtra("ownerId") ?: ""

        // 🔥 NUEVO – guardamos la referencia una sola vez
        adRef = FirebaseDatabase.getInstance().getReference("Anuncios").child(adId)

        // ---------------------------  Datos recibidos por Intent  ---------------------------
        val title      = intent.getStringExtra("title") ?: ""
        val price      = intent.getStringExtra("price") ?: "0.0"
        val condition  = intent.getStringExtra("condition") ?: ""
        val category   = intent.getStringExtra("category") ?: ""
        val brand      = intent.getStringExtra("brand") ?: ""
        val location   = intent.getStringExtra("location") ?: ""
        val description= intent.getStringExtra("description") ?: ""
        val images     = intent.getStringArrayListExtra("images") ?: arrayListOf()

        binding.productTitle.text       = title
        binding.productPrice.text       = getString(R.string.product_price_format, price)
        binding.productCondition.text   = condition
        binding.productCategory.text    = category
        binding.productBrand.text       = getString(R.string.product_brand_format, brand)
        binding.productLocation.text    = getString(R.string.product_location_format, location)
        binding.productDescription.text = description

        binding.imageCarousel.adapter = ImageSliderAdapter(images)
        binding.imageCounter.text     = getString(R.string.image_counter_format, 1, images.size)

        // ------------------------------------------------------------------------------------
        val isOwner = firebaseAuth.currentUser?.uid == ownerId
        binding.btnReserve.visibility  = if (isOwner) View.GONE else View.VISIBLE
        binding.btnEditAd.visibility   = if (isOwner) View.VISIBLE else View.GONE
        binding.btnDeleteAd.visibility = if (isOwner) View.VISIBLE else View.GONE

        loadSellerData(ownerId)
        checkFavoriteStatus()
        verificarEstadoReserva()      // estado de la reserva para este usuario

        // 🔥 NUEVO – escuchamos el estado del anuncio en tiempo-real
        listenStatusRealtime()

        // -----------  Listeners de botones  -----------
        binding.btnFavorite.setOnClickListener {
            if (isFavorite) removeFromFavorites() else addToFavorites()
        }
        binding.btnDeleteAd.setOnClickListener { confirmAdDeletion() }
        binding.btnReserve  .setOnClickListener { enviarSolicitudReserva() }
    }

    // ╔══════════════════════════════════════════════════════════════════╗
    // ║                        RESERVAS                                 ║
    // ╚══════════════════════════════════════════════════════════════════╝
    // 🔥 NUEVO – se dispara cada vez que el nodo status cambia
    private fun listenStatusRealtime() {
        adRef.child("status").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val estado = snapshot.getValue(String::class.java) ?: "Disponible"
                refreshReserveButton(estado)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // 🔥 NUEVO – actualiza texto y disponibilidad del botón
    private fun refreshReserveButton(adStatus: String) {
        binding.productStatus.text = "Estado: $adStatus"

        // compradores diferentes al dueño
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null || currentUser.uid == ownerId) return

        when (adStatus) {
            "Reservado" -> {
                binding.btnReserve.apply {
                    isEnabled = false
                    text = "Reservado"
                }
            }
            else -> {
                // si no está reservado, vemos si este user ya tiene pendiente/aceptada
                verificarEstadoReserva()
            }
        }
    }

    private fun enviarSolicitudReserva() {
        val buyerId = firebaseAuth.currentUser?.uid ?: return
        val reservaRef = FirebaseDatabase.getInstance()
            .getReference("Reservas")
            .child(adId)
            .child(buyerId)

        val reservaData = mapOf("fecha" to System.currentTimeMillis(), "estado" to "pendiente")

        reservaRef.setValue(reservaData)
            .addOnSuccessListener {
                Toast.makeText(this, "Reserva enviada correctamente", Toast.LENGTH_SHORT).show()
                binding.btnReserve.apply { isEnabled = false; text = "Reserva pendiente" }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al enviar reserva", Toast.LENGTH_SHORT).show()
            }
    }

    /** Comprueba si el usuario YA había interactuado con la reserva  */
    private fun verificarEstadoReserva() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        FirebaseDatabase.getInstance().getReference("Reservas")
            .child(adId).child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) return
                    when (snapshot.child("estado").getValue(String::class.java)) {
                        "pendiente" -> binding.btnReserve.apply {
                            isEnabled = false; text = "Reserva pendiente"
                        }
                        "aceptado"  -> binding.btnReserve.apply {
                            isEnabled = false; text = "Reservado"
                        }
                        "rechazado" -> binding.btnReserve.apply {
                            isEnabled = false; text = "Reserva rechazada"
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ╔══════════════════════════════════════════════════════════════════╗
    // ║               FAVORITOS  (sin cambios funcionales)              ║
    // ╚══════════════════════════════════════════════════════════════════╝
    private fun checkFavoriteStatus() { /* igual que tu versión */ }
    private fun updateFavoriteIcon() { val iconRes = if (isFavorite) R.drawable.ad_favorite_icon else R.drawable.ad_no_favorite_icon
        binding.btnFavorite.setImageResource(iconRes) }
    private fun addToFavorites() { /* igual que tu versión */ }
    private fun removeFromFavorites() { /* igual que tu versión */ }

    // ╔══════════════════════════════════════════════════════════════════╗
    // ║           ELIMINAR / EDITAR (sin cambios)                       ║
    // ╚══════════════════════════════════════════════════════════════════╝
    private fun confirmAdDeletion() {   // (idéntico a tu código)
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar anuncio")
            .setMessage("¿Estás seguro de eliminar este anuncio?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ -> deleteAdFromDatabase() }
            .show()
    }
    private fun deleteAdFromDatabase() { /* tu implementación sin tocar */ }

    // ╔══════════════════════════════════════════════════════════════════╗
    // ║        CARGA DE DATOS DEL VENDEDOR (sin cambios)                ║
    // ╚══════════════════════════════════════════════════════════════════╝
    private fun loadSellerData(userId: String) { /* igual que tu versión */ }
}
