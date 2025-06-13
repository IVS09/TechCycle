package com.mrlapidus.techcycle

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mrlapidus.techcycle.adapter.AdReservationAdapter
import com.mrlapidus.techcycle.databinding.ActivityAdReservationsBinding
import com.mrlapidus.techcycle.model.AdModel

class AdReservationsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAdReservationsBinding
    private lateinit var db      : DatabaseReference
    private lateinit var adList  : MutableList<AdModel>
    private lateinit var adapter : AdReservationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdReservationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        adList  = mutableListOf()
        adapter = AdReservationAdapter(adList) { ad ->
            startActivity(
                Intent(this, ReservaDetalleActivity::class.java).apply {
                    putExtra("adId", ad.id)
                }
            )
        }

        binding.recyclerReservations.layoutManager = LinearLayoutManager(this)
        binding.recyclerReservations.adapter       = adapter

        db = FirebaseDatabase.getInstance().getReference("Anuncios")
        cargarAnunciosConReservaAceptada(userId)      // 🔥 NUEVO nombre más claro
    }

    // --------------------------------------------------------------------
    //  Solo anuncios con alguna reserva == "aceptado"
    // --------------------------------------------------------------------
    private fun cargarAnunciosConReservaAceptada(ownerId: String) {

        db.orderByChild("userId").equalTo(ownerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    adList.clear()

                    snapshot.children.forEach { adSnap ->
                        val adId    = adSnap.key ?: return@forEach
                        val reservasRef = FirebaseDatabase.getInstance()
                            .getReference("Reservas")
                            .child(adId)

                        reservasRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(reservaSnap: DataSnapshot) {

                                // 🔥 NUEVO --> ¿Al menos una reserva aceptada?
                                var hayAceptada = false
                                for (child in reservaSnap.children) {
                                    val estado = child.child("estado")
                                        .getValue(String::class.java) ?: "pendiente"
                                    if (estado == "aceptado") {
                                        hayAceptada = true
                                        break
                                    }
                                }

                                if (hayAceptada) {
                                    // --- datos mínimos para la tarjeta
                                    val title  = adSnap.child("title")
                                        .getValue(String::class.java) ?: "—"
                                    val price  = adSnap.child("price")
                                        .getValue(String::class.java) ?: "0"
                                    val images = mutableListOf<String>()
                                    adSnap.child("images").children.forEach { img ->
                                        img.child("imageUrl")
                                            .getValue(String::class.java)
                                            ?.let { images.add(it) }
                                    }

                                    adList.add(
                                        AdModel(
                                            id         = adId,
                                            title      = title,
                                            price      = price,
                                            status     = "Reservado",        // 🔥 NUEVO
                                            imageUrls  = images
                                        )
                                    )
                                    adapter.notifyDataSetChanged()
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
