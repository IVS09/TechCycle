package com.mrlapidus.techcycle

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mrlapidus.techcycle.adapter.AdReservationAdapter
import com.mrlapidus.techcycle.databinding.ActivityManageMyAdsBinding
import com.mrlapidus.techcycle.model.AdModel

class ManageMyAdsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageMyAdsBinding
    private lateinit var database: DatabaseReference
    private lateinit var adList: MutableList<AdModel>
    private lateinit var adapter: AdReservationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageMyAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        adList = mutableListOf()
        adapter = AdReservationAdapter(adList) { ad ->
            val intent = Intent(this, ReservaDetalleActivity::class.java)
            intent.putExtra("adId", ad.id)
            startActivity(intent)
        }

        binding.recyclerManageAds.layoutManager = LinearLayoutManager(this)
        binding.recyclerManageAds.adapter = adapter

        database = FirebaseDatabase.getInstance().getReference("Anuncios")
        cargarMisAnuncios(userId)
    }

    private fun cargarMisAnuncios(ownerId: String) {
        database.orderByChild("userId").equalTo(ownerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    adList.clear()
                    for (adSnapshot in snapshot.children) {
                        val adId = adSnapshot.key ?: continue
                        val title = adSnapshot.child("title").getValue(String::class.java) ?: continue
                        val price = adSnapshot.child("price").getValue(String::class.java) ?: "0"
                        val status = adSnapshot.child("status").getValue(String::class.java) ?: "Disponible"
                        val images = mutableListOf<String>()

                        adSnapshot.child("images").children.forEach { img ->
                            img.child("imageUrl").getValue(String::class.java)?.let { images.add(it) }
                        }

                        val ad = AdModel(id = adId, title = title, price = price, status = status, imageUrls = images)
                        adList.add(ad)
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
