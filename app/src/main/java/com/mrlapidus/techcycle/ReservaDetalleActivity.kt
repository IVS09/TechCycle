package com.mrlapidus.techcycle

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.mrlapidus.techcycle.adapter.ReservationRequestAdapter
import com.mrlapidus.techcycle.databinding.ActivityReservaDetalleBinding
import com.mrlapidus.techcycle.model.ReservationRequest
import com.mrlapidus.techcycle.model.UserModel

class ReservaDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReservaDetalleBinding
    private lateinit var database: DatabaseReference
    private lateinit var adapter: ReservationRequestAdapter
    private val reservationList = mutableListOf<ReservationRequest>()
    private var adId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReservaDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adId = intent.getStringExtra("adId") ?: return

        adapter = ReservationRequestAdapter(reservationList,
            onAccept = { request -> updateReservationStatus(request, "aceptado") },
            onReject = { request -> updateReservationStatus(request, "rechazado") }
        )

        binding.recyclerReservationRequests.layoutManager = LinearLayoutManager(this)
        binding.recyclerReservationRequests.adapter = adapter

        database = FirebaseDatabase.getInstance().reference

        loadReservations()
    }

    private fun loadReservations() {
        val reservationsRef = database.child("Reservas").child(adId)
        reservationsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reservationList.clear()
                for (child in snapshot.children) {
                    val buyerId = child.key ?: continue
                    val estado = child.child("estado").getValue(String::class.java) ?: "pendiente"
                    val fecha = child.child("fecha").getValue(Long::class.java) ?: 0L

                    // Obtener información del usuario
                    database.child("Usuarios").child(buyerId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val nombre = userSnapshot.child("nombreCompleto").getValue(String::class.java) ?: "Usuario"
                                val avatarUrl = userSnapshot.child("urlAvatar").getValue(String::class.java) ?: ""

                                val request = ReservationRequest(
                                    buyerId = buyerId,
                                    buyerName = nombre,
                                    buyerAvatarUrl = avatarUrl,
                                    fecha = fecha,
                                    estado = estado
                                )
                                reservationList.add(request)
                                adapter.notifyDataSetChanged()
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateReservationStatus(request: ReservationRequest, newStatus: String) {
        val reservationRef = database.child("Reservas").child(adId).child(request.buyerId)
        reservationRef.child("estado").setValue(newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, "Reserva ${newStatus}", Toast.LENGTH_SHORT).show()
                if (newStatus == "aceptado") {
                    // Actualizar estado del anuncio a "Reservado"
                    database.child("Anuncios").child(adId).child("status").setValue("Reservado")
                }
                loadReservations()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar la reserva", Toast.LENGTH_SHORT).show()
            }
    }

    private fun enviarNotificacionReserva(
        compradorId: String,
        mensaje: String
    ) {
        val notificacionRef = FirebaseDatabase.getInstance()
            .getReference("Notificaciones")
            .child(compradorId)
            .push()

        val data = mapOf(
            "mensaje" to mensaje,
            "timestamp" to System.currentTimeMillis(),
            "leido" to false
        )

        notificacionRef.setValue(data)
    }

}
