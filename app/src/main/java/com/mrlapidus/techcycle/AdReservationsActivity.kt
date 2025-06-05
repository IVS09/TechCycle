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

    private lateinit var binding: ActivityAdReservationsBinding
    private lateinit var database: DatabaseReference
    private lateinit var adList: MutableList<AdModel>
    private lateinit var adapter: AdReservationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdReservationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        adList = mutableListOf()
        adapter = AdReservationAdapter(this, adList) { ad ->
            val intent = Intent(this, ReservaDetalleActivity::class.java)
            intent.putExtra("adId", ad.id)
            startActivity(intent)
        }

        binding.recyclerReservations.layoutManager = LinearLayoutManager(this)
        binding.recyclerReservations.adapter = adapter

        database = FirebaseDatabase.getInstance().getReference("Anuncios")
        cargarAnunciosConReservas(userId)
    }

    private fun cargarAnunciosConReservas(ownerId: String) {
        database.orderByChild("userId").equalTo(ownerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    adList.clear()
                    for (adSnapshot in snapshot.children) {
                        val adId = adSnapshot.key ?: continue
                        val title = adSnapshot.child("title").getValue(String::class.java) ?: continue
                        val price = adSnapshot.child("price").getValue(String::class.java) ?: "0"
                        val images = mutableListOf<String>()

                        adSnapshot.child("images").children.forEach { img ->
                            img.child("imageUrl").getValue(String::class.java)?.let { images.add(it) }
                        }

                        // Comprobar si tiene reservas
                        val reservasRef = FirebaseDatabase.getInstance().getReference("Reservas").child(adId)
                        reservasRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(reservaSnap: DataSnapshot) {
                                if (reservaSnap.exists()) {
                                    adList.add(AdModel(id = adId, title = title, price = price, imageUrls = images))
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
