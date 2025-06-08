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
    private var adId: String = ""
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        adId = intent.getStringExtra("adId") ?: ""

        val title = intent.getStringExtra("title") ?: ""
        val price = intent.getStringExtra("price") ?: "0.0"
        val condition = intent.getStringExtra("condition") ?: ""
        val category = intent.getStringExtra("category") ?: ""
        val brand = intent.getStringExtra("brand") ?: ""
        val location = intent.getStringExtra("location") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val images = intent.getStringArrayListExtra("images") ?: arrayListOf()
        val ownerId = intent.getStringExtra("ownerId") ?: ""

        loadSellerData(ownerId)
        checkAdStatus()
        checkFavoriteStatus()
        verificarEstadoReserva()


        binding.productTitle.text = title
        binding.productPrice.text = getString(R.string.product_price_format, price)
        binding.productCondition.text = condition
        binding.productCategory.text = category
        binding.productBrand.text = getString(R.string.product_brand_format, brand)
        binding.productLocation.text = getString(R.string.product_location_format, location)
        binding.productDescription.text = description

        binding.imageCarousel.adapter = ImageSliderAdapter(images)
        binding.imageCounter.text = getString(R.string.image_counter_format, 1, images.size)

        val currentUserId = firebaseAuth.currentUser?.uid
        val isOwner = currentUserId == ownerId

        binding.btnReserve.visibility = if (isOwner) View.GONE else View.VISIBLE
        binding.btnEditAd.visibility = if (isOwner) View.VISIBLE else View.GONE
        binding.btnDeleteAd.visibility = if (isOwner) View.VISIBLE else View.GONE

        binding.btnFavorite.setOnClickListener {
            if (isFavorite) {
                removeFromFavorites()
            } else {
                addToFavorites()
            }
        }

        binding.btnDeleteAd.setOnClickListener {
            confirmAdDeletion()
        }

        binding.btnReserve.setOnClickListener {
            enviarSolicitudReserva()
        }

        if (!isOwner) {
            checkMyReservationStatus()
        }


    }

    private fun confirmAdDeletion() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar anuncio")
            .setMessage("¿Estás seguro de eliminar este anuncio?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                deleteAdFromDatabase()
            }
            .show()
    }

    private fun deleteAdFromDatabase() {
        val adRef = FirebaseDatabase.getInstance().getReference("Anuncios").child(adId)
        val imageFolderRef = FirebaseStorage.getInstance().getReference("AdImages").child(adId)

        // 1. Elimina anuncio de Realtime Database
        adRef.removeValue().addOnSuccessListener {
            // 2. Elimina carpeta de imágenes en Storage
            imageFolderRef.listAll()
                .addOnSuccessListener { result ->
                    val deleteTasks = result.items.map { it.delete() }
                    val allDeleted = deleteTasks.fold(true) { acc, task -> acc && true }
                    Toast.makeText(this, "Anuncio e imágenes eliminadas", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Anuncio eliminado, pero error al eliminar imágenes", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al eliminar el anuncio", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkFavoriteStatus() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
            .child(uid)
            .child("Favoritos")
            .child(adId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isFavorite = snapshot.exists()
                updateFavoriteIcon()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateFavoriteIcon() {
        val iconRes = if (isFavorite) R.drawable.ad_favorite_icon else R.drawable.ad_no_favorite_icon
        binding.btnFavorite.setImageResource(iconRes)
    }

    private fun addToFavorites() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
            .child(uid)
            .child("Favoritos")
            .child(adId)

        val data = mapOf(
            "adId" to adId,
            "addedAt" to System.currentTimeMillis()
        )

        ref.setValue(data)
            .addOnSuccessListener {
                isFavorite = true
                updateFavoriteIcon()
                Toast.makeText(this, "Añadido a favoritos", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al añadir a favoritos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeFromFavorites() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
            .child(uid)
            .child("Favoritos")
            .child(adId)

        ref.removeValue()
            .addOnSuccessListener {
                isFavorite = false
                updateFavoriteIcon()
                Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar favorito", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadSellerData(userId: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            val nombre = snapshot.child("nombreCompleto").getValue(String::class.java) ?: "Usuario"
            val avatarUrl = snapshot.child("urlAvatar").getValue(String::class.java) ?: ""
            val fechaRegistro = snapshot.child("fechaDeRegistro").getValue(Long::class.java) ?: 0L

            binding.sellerName.text = nombre
            binding.sellerSince.text = getString(
                R.string.member_since,
                android.text.format.DateFormat.format("dd/MM/yyyy", fechaRegistro)
            )

            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_profile)
                .into(binding.sellerAvatar)
        }
    }

    private fun checkAdStatus() {
        val adRef = FirebaseDatabase.getInstance().getReference("Anuncios").child(adId)
        adRef.child("status").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java) ?: "Disponible"
                binding.productStatus.text = "Estado: $status"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun enviarSolicitudReserva() {
        val buyerId = firebaseAuth.currentUser?.uid ?: return
        val reservaRef = FirebaseDatabase.getInstance()
            .getReference("Reservas")
            .child(adId)
            .child(buyerId)

        val reservaData = mapOf(
            "fecha" to System.currentTimeMillis(),
            "estado" to "pendiente"
        )

        reservaRef.setValue(reservaData)
            .addOnSuccessListener {
                Toast.makeText(this, "Reserva enviada correctamente", Toast.LENGTH_SHORT).show()
                binding.btnReserve.isEnabled = false
                binding.btnReserve.text = "Reserva pendiente"
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al enviar reserva", Toast.LENGTH_SHORT).show()
            }
    }

    private fun verificarEstadoReserva() {
        val currentUser = firebaseAuth.currentUser ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Reservas")
            .child(adId)
            .child(currentUser.uid)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val estado = snapshot.child("estado").getValue(String::class.java) ?: "pendiente"
                    when (estado) {
                        "pendiente" -> {
                            Toast.makeText(this@ProductDetailActivity, "Ya has solicitado esta reserva. Esperando respuesta.", Toast.LENGTH_LONG).show()
                            binding.btnReserve.isEnabled = false
                            binding.btnReserve.text = "Reserva pendiente"
                        }
                        "aceptado" -> {
                            Toast.makeText(this@ProductDetailActivity, "Tu reserva fue aceptada.", Toast.LENGTH_LONG).show()
                            binding.btnReserve.isEnabled = false
                            binding.btnReserve.text = "Reservado"
                        }
                        "rechazado" -> {
                            Toast.makeText(this@ProductDetailActivity, "Tu reserva fue rechazada.", Toast.LENGTH_LONG).show()
                            binding.btnReserve.isEnabled = false
                            binding.btnReserve.text = "Reserva rechazada"
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun checkMyReservationStatus() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Reservas")
            .child(adId)
            .child(uid)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val estado = snapshot.child("estado").getValue(String::class.java) ?: ""

                when (estado) {
                    "pendiente" -> {
                        binding.btnReserve.text = "Reserva pendiente"
                        binding.btnReserve.isEnabled = false
                    }
                    "aceptado" -> {
                        binding.btnReserve.text = "Reservado"
                        binding.btnReserve.isEnabled = false
                    }
                    "rechazado" -> {
                        binding.btnReserve.text = "Reserva rechazada"
                        binding.btnReserve.isEnabled = true // si quieres permitir volver a reservar
                    }
                    else -> {
                        binding.btnReserve.text = getString(R.string.reserve_ad)
                        binding.btnReserve.isEnabled = true
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }



}






