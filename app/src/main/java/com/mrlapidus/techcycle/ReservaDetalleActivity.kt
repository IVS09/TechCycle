package com.mrlapidus.techcycle

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.mrlapidus.techcycle.adapter.ReservationRequestAdapter
import com.mrlapidus.techcycle.databinding.ActivityReservaDetalleBinding
import com.mrlapidus.techcycle.model.ReservationRequest

class ReservaDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReservaDetalleBinding
    private lateinit var db   : DatabaseReference
    private lateinit var adapter: ReservationRequestAdapter
    private val reservationList = mutableListOf<ReservationRequest>()
    private var adId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReservaDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adId = intent.getStringExtra("adId") ?: return
        db   = FirebaseDatabase.getInstance().reference

        adapter = ReservationRequestAdapter(
            reservationList,
            onAcceptClick = { request -> updateReservationStatus(request, "aceptado") },
            onRejectClick = { request -> updateReservationStatus(request, "rechazado") }
        )

        binding.recyclerReservationRequests.layoutManager = LinearLayoutManager(this)
        binding.recyclerReservationRequests.adapter       = adapter


        loadReservations()
    }

    // --------------------------------------------------------------------
    //  Carga (en tiempo-real) de las solicitudes
    // --------------------------------------------------------------------
    private fun loadReservations() {

        binding.progressBar.visibility = View.VISIBLE      // 🔄 loader

        val reservationsRef = db.child("Reservas").child(adId)
        reservationsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                reservationList.clear()

                snapshot.children.forEach { reservaSnap ->

                    val buyerId = reservaSnap.key ?: return@forEach
                    val estado  = reservaSnap.child("estado").getValue(String::class.java) ?: "pendiente"
                    val fecha   = reservaSnap.child("fecha").getValue(Long::class.java) ?: 0L

                    // ── Datos del comprador ───────────────────────────────
                    db.child("Usuarios").child(buyerId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            @SuppressLint("NotifyDataSetChanged")
                            override fun onDataChange(userSnap: DataSnapshot) {

                                val nombre    = userSnap.child("nombreCompleto")
                                    .getValue(String::class.java) ?: "Usuario"
                                val avatarUrl = userSnap.child("urlAvatar")
                                    .getValue(String::class.java) ?: ""

                                reservationList.add(
                                    ReservationRequest(
                                        buyerId        = buyerId,
                                        buyerName      = nombre,
                                        buyerAvatarUrl = avatarUrl,
                                        fecha          = fecha,
                                        estado         = estado
                                    )
                                )

                                adapter.notifyDataSetChanged()
                                togglePlaceholder()
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                }

                togglePlaceholder()
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                togglePlaceholder()
                binding.progressBar.visibility = View.GONE
            }
        })
    }


    private fun togglePlaceholder() {
        if (reservationList.isEmpty()) {
            binding.recyclerReservationRequests.visibility = View.GONE
            binding.tvNoRequests.visibility                = View.VISIBLE
        } else {
            binding.recyclerReservationRequests.visibility = View.VISIBLE
            binding.tvNoRequests.visibility                = View.GONE
        }
    }


    // --------------------------------------------------------------------
    //  Cambiar estado  (aceptar / rechazar)
    // --------------------------------------------------------------------
    private fun updateReservationStatus(
        request: ReservationRequest,
        newStatus: String
    ) {
        val reservasRef     = db.child("Reservas").child(adId)
        val currentResRef   = reservasRef.child(request.buyerId)
        val anunciosRef     = db.child("Anuncios").child(adId)

        /* ---------- 1. Cambiamos el estado de ESTA reserva ---------- */
        currentResRef.child("estado").setValue(newStatus).addOnSuccessListener {

            if (newStatus == "aceptado") {

                // 1-A. Marcamos el anuncio como Reservado
                anunciosRef.child("status").setValue("Reservado")

                // 1-B. Borramos SOLO las demás reservas (para evitar doble booking)
                reservasRef.get().addOnSuccessListener { snap ->
                    snap.children.forEach { child ->
                        if (child.key != request.buyerId) child.ref.removeValue()
                    }
                }
            } else {                 // ←  “rechazado”  (o cuando rechaces un aceptado)
                // 1-C. Si no queda ningún otro “aceptado”, liberamos el anuncio
                reservasRef.get().addOnSuccessListener { snap ->
                    val sigueReservado = snap.children.any {
                        it.child("estado").getValue(String::class.java) == "aceptado"
                    }
                    if (!sigueReservado) anunciosRef.child("status").setValue("Disponible")
                }
            }

            Toast.makeText(
                this,
                if (newStatus == "aceptado") "Reserva aceptada"
                else "Reserva rechazada",
                Toast.LENGTH_SHORT
            ).show()

            loadReservations()      // 🔄  refrescamos la lista
        }.addOnFailureListener {
            Toast.makeText(this, "Error al actualizar la reserva", Toast.LENGTH_SHORT).show()
        }
    }




    // --------------------------------------------------------------------
    //  Enviar notificación push en futuro
    // --------------------------------------------------------------------
    @Suppress("unused")
    private fun enviarNotificacionReserva(
        compradorId: String,
        mensaje: String
    ) {
        val notificacionRef = db.child("Notificaciones")
            .child(compradorId)
            .push()

        notificacionRef.setValue(
            mapOf(
                "mensaje"   to mensaje,
                "timestamp" to System.currentTimeMillis(),
                "leido"     to false
            )
        )
    }
}

