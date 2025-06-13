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

    // Referencia al anuncio en tiempo-real
    private lateinit var adRef: DatabaseReference

    private var adId: String   = ""
    private var ownerId: String = ""
    private var isFavorite     = false
    private var adStatus       = "Disponible"

    // ──────────────────────────────────────────────────────────────────────────────
    // 1) métodos para el lifecycle de la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        adId    = intent.getStringExtra("adId")    ?: ""
        ownerId = intent.getStringExtra("ownerId") ?: ""

        adRef = FirebaseDatabase.getInstance()
            .getReference("Anuncios")
            .child(adId)

        // Datos recibidos por intent
        val title       = intent.getStringExtra("title"      ) ?: ""
        val price       = intent.getStringExtra("price"      ) ?: "0.0"
        val condition   = intent.getStringExtra("condition"  ) ?: ""
        val category    = intent.getStringExtra("category"   ) ?: ""
        val brand       = intent.getStringExtra("brand"      ) ?: ""
        val location    = intent.getStringExtra("location"   ) ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val images      = intent.getStringArrayListExtra("images") ?: arrayListOf()

        // Pintado inmediato
        binding.apply {
            productTitle.text       = title
            productPrice.text       = getString(R.string.product_price_format, price)
            productCondition.text   = condition
            productCategory.text    = category
            productBrand.text       = getString(R.string.product_brand_format, brand)
            productLocation.text    = getString(R.string.product_location_format, location)
            productDescription.text = description
            imageCarousel.adapter   = ImageSliderAdapter(images)
            imageCounter.text       = getString(R.string.image_counter_format, 1, images.size)
        }

        // Mostrar / ocultar botones según quién sea el usuario (comprador o vendedor)
        val isOwner = firebaseAuth.currentUser?.uid == ownerId
        binding.btnReserve.visibility  = if (isOwner) View.GONE else View.VISIBLE
        binding.btnEditAd.visibility   = if (isOwner) View.VISIBLE else View.GONE
        binding.btnDeleteAd.visibility = if (isOwner) View.VISIBLE else View.GONE

        loadSellerData(ownerId)
        checkFavoriteStatus()
        verificarEstadoReserva()
        // escucha los cambios en “status” del anuncio
        listenStatusRealtime()

        // ─ Listeners ─
        binding.btnFavorite.setOnClickListener {
            if (isFavorite) removeFromFavorites() else addToFavorites()
        }
        binding.btnDeleteAd.setOnClickListener { confirmAdDeletion() }
        binding.btnReserve  .setOnClickListener { enviarSolicitudReserva() }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // 2) Reservas
    /** Observa el nodo status en tiempo-real */
    private fun listenStatusRealtime() {
        adRef.child("status").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val estado = snapshot.getValue(String::class.java) ?: "Disponible"
                refreshReserveButton(estado)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /** Ajusta el botón “Reservar” según el estado general del anuncio */
    private fun refreshReserveButton(adStatus: String) {
        binding.productStatus.text = "Estado: $adStatus"

        val currentUser = firebaseAuth.currentUser ?: return
        if (currentUser.uid == ownerId) return   // el dueño no ve el botón

        if (adStatus == "Reservado") {
            verificarEstadoReserva(adStatus)    // revisa si está reservado para este user
        } else {
            binding.btnReserve.apply {
                isEnabled = true
                text      = getString(R.string.reserve_ad)
            }
        }
    }

    /** Envía la solicitud de reserva */
    private fun enviarSolicitudReserva() {
        val buyerId = firebaseAuth.currentUser?.uid ?: return
        val reservaRef = FirebaseDatabase.getInstance()
            .getReference("Reservas")
            .child(adId)
            .child(buyerId)

        val reservaData = mapOf(
            "fecha"  to System.currentTimeMillis(),
            "estado" to "pendiente"
        )

        reservaRef.setValue(reservaData)
            .addOnSuccessListener {
                Toast.makeText(this, "Reserva enviada correctamente", Toast.LENGTH_SHORT).show()
                binding.btnReserve.apply {
                    isEnabled = false
                    text      = getString(R.string.reservation_btn_pending)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al enviar reserva", Toast.LENGTH_SHORT).show()
            }
    }

    /** Comprueba el estado de la reserva para el usuario activo */
    private fun verificarEstadoReserva(adStatusFromDb: String = adStatus) {

        val uid = firebaseAuth.currentUser?.uid ?: return
        val resRef = FirebaseDatabase.getInstance()
            .getReference("Reservas")
            .child(adId)
            .child(uid)

        resRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    when (snapshot.child("estado").getValue(String::class.java)) {
                        "pendiente" -> binding.btnReserve.apply {
                            isEnabled = false
                            text      = getString(R.string.reservation_btn_pending)
                        }
                        "aceptado"  -> binding.btnReserve.apply {
                            isEnabled = false
                            text      = getString(R.string.reservation_btn_accepted)
                        }
                        "rechazado" -> binding.btnReserve.apply {
                            isEnabled = false
                            text      = getString(R.string.reservation_btn_rejected)
                        }
                    }
                } else {
                    if (adStatusFromDb == "Reservado") {
                        binding.btnReserve.apply {
                            isEnabled = false
                            text      = getString(R.string.reservation_btn_reserved)
                        }
                    } else {
                        binding.btnReserve.apply {
                            isEnabled = true
                            text      = getString(R.string.reserve_ad)
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }


    // ──────────────────────────────────────────────────────────────────────────────
    //3) Favoritos
    private fun checkFavoriteStatus() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val favRef = FirebaseDatabase.getInstance()
            .getReference("Usuarios")
            .child(uid)
            .child("Favoritos")
            .child(adId)

        favRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isFavorite = snapshot.exists()
                updateFavoriteIcon()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateFavoriteIcon() {
        val icon = if (isFavorite) R.drawable.ad_favorite_icon else R.drawable.ad_no_favorite_icon
        binding.btnFavorite.setImageResource(icon)
    }

    private fun addToFavorites() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val favRef = FirebaseDatabase.getInstance()
            .getReference("Usuarios")
            .child(uid)
            .child("Favoritos")
            .child(adId)

        val data = mapOf("adId" to adId, "addedAt" to System.currentTimeMillis())

        favRef.setValue(data).addOnSuccessListener {
            isFavorite = true
            updateFavoriteIcon()
            Toast.makeText(this, R.string.ad_favorite_add, Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeFromFavorites() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val favRef = FirebaseDatabase.getInstance()
            .getReference("Usuarios")
            .child(uid)
            .child("Favoritos")
            .child(adId)

        favRef.removeValue().addOnSuccessListener {
            isFavorite = false
            updateFavoriteIcon()
            Toast.makeText(this, R.string.ad_favorite_remove, Toast.LENGTH_SHORT).show()
        }
    }


    // ──────────────────────────────────────────────────────────────────────────────
    //4) Eliminar anuncio
    private fun confirmAdDeletion() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar anuncio")
            .setMessage("¿Estás seguro de eliminar este anuncio?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ -> deleteAdFromDatabase() }
            .show()
    }

    private fun deleteAdFromDatabase() {

        val adRef = FirebaseDatabase.getInstance()
            .getReference("Anuncios")
            .child(adId)

        val storageRef = FirebaseStorage.getInstance()
            .getReference("AdImages")
            .child(adId)

        adRef.removeValue().addOnSuccessListener {
            storageRef.listAll().addOnSuccessListener { list ->
                list.items.forEach { it.delete() }      // borra cada imagen
                Toast.makeText(this, R.string.ad_deleted_ok, Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, R.string.ad_deleted_error, Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, R.string.ad_deleted_error, Toast.LENGTH_SHORT).show()
        }
    }


    // ──────────────────────────────────────────────────────────────────────────────
    //5) Carga los datos del vendedor
    private fun loadSellerData(userId: String) {

        if (userId.isBlank()) {
            binding.sellerInfoSection.visibility = View.GONE
            return
        }

        val userRef = FirebaseDatabase.getInstance()
            .getReference("Usuarios")
            .child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val nombre = snapshot.child("nombreCompleto")
                    .getValue(String::class.java).orEmpty()
                binding.sellerName.text =
                    if (nombre.isBlank()) getString(R.string.default_username) else nombre

                val avatarUrl = snapshot.child("urlAvatar")
                    .getValue(String::class.java).orEmpty()

                Glide.with(this@ProductDetailActivity)
                    .load(avatarUrl.ifBlank { R.drawable.ic_profile })
                    .placeholder(R.drawable.ic_profile)
                    .circleCrop()
                    .into(binding.sellerAvatar)

                val ts = snapshot.child("fechaDeRegistro")
                    .getValue(Long::class.java) ?: 0L
                val fecha =
                    if (ts != 0L) android.text.format.DateFormat.format("dd/MM/yyyy", ts).toString()
                    else "—"
                binding.sellerSince.text = getString(R.string.member_since, fecha)
            }

            override fun onCancelled(error: DatabaseError) {
                binding.sellerInfoSection.visibility = View.GONE
            }
        })
    }
}

