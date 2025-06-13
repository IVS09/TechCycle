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
        db.child("Reservas").child(adId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    reservationList.clear()

                    snapshot.children.forEach { reservaSnap ->
                        val buyerId = reservaSnap.key ?: return@forEach
                        val estado  = reservaSnap.child("estado").getValue(String::class.java) ?: "pendiente"
                        val fecha   = reservaSnap.child("fecha").getValue(Long::class.java) ?: 0L

                        // obtenemos nombre + avatar del comprador
                        db.child("Usuarios").child(buyerId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnap: DataSnapshot) {
                                    val nombre    = userSnap.child("nombreCompleto").getValue(String::class.java) ?: "Usuario"
                                    val avatarUrl = userSnap.child("urlAvatar").getValue(String::class.java) ?: ""

                                    reservationList.add(
                                        ReservationRequest(
                                            buyerId         = buyerId,
                                            buyerName       = nombre,
                                            buyerAvatarUrl  = avatarUrl,
                                            fecha           = fecha,
                                            estado          = estado
                                        )
                                    )
                                    adapter.notifyDataSetChanged()
                                    toggleEmptyLabel()
                                }
                                override fun onCancelled(error: DatabaseError) {}
                            })
                    }

                    toggleEmptyLabel()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun toggleEmptyLabel() {
        binding.recyclerReservationRequests.visibility =
            if (reservationList.isEmpty()) View.GONE else View.VISIBLE
        binding.tvTituloDetalleReservas.visibility     =
            if (reservationList.isEmpty()) View.GONE else View.VISIBLE
    }

    // --------------------------------------------------------------------
    //  Cambiar estado  (aceptar / rechazar)
    // --------------------------------------------------------------------
    private fun updateReservationStatus(request: ReservationRequest, newStatus: String) {

        val reservationRef = db.child("Reservas").child(adId).child(request.buyerId)

        if (newStatus == "rechazado") {
            // 🔥 NUEVO 1 ➜  Si se rechaza, se elimina la reserva del nodo
            reservationRef.removeValue().addOnSuccessListener {
                Toast.makeText(this, "Reserva rechazada y eliminada", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Error al rechazar la reserva", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // Aceptar reserva
        reservationRef.child("estado").setValue("aceptado")
            .addOnSuccessListener {

                // 🔥 NUEVO 2 ➜  Al aceptar, se marca el anuncio como “Reservado”
                db.child("Anuncios").child(adId).child("status").setValue("Reservado")

                // 🔥 NUEVO 3 ➜  Todas las DEMÁS solicitudes se eliminan
                borrarOtrasReservas(request.buyerId)

                Toast.makeText(this, "Reserva aceptada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar la reserva", Toast.LENGTH_SHORT).show()
            }
    }

    // --------------------------------------------------------------------
    //  Borra las reservas de otros compradores cuando se acepta una
    // --------------------------------------------------------------------
    private fun borrarOtrasReservas(buyerAceptado: String) {           // 🔥 NUEVO
        val reservasRef = db.child("Reservas").child(adId)
        reservasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { child ->
                    val uid = child.key ?: return@forEach
                    if (uid != buyerAceptado) reservasRef.child(uid).removeValue()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // --------------------------------------------------------------------
    //  (Opcional) Enviar notificación push en futuro
    // --------------------------------------------------------------------
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

