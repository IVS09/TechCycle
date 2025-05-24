package com.mrlapidus.techcycle.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mrlapidus.techcycle.adapter.AdAdapter
import com.mrlapidus.techcycle.databinding.FragmentFavAdsBinding
import com.mrlapidus.techcycle.model.AdModel

class FragmentFavAds : Fragment() {

    private lateinit var binding: FragmentFavAdsBinding
    private lateinit var adAdapter: AdAdapter
    private val favoriteAds = ArrayList<AdModel>()

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("Usuarios")
    private val adsRef = database.getReference("Anuncios")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavAdsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadFavorites()
    }

    private fun setupRecyclerView() {
        adAdapter = AdAdapter(requireContext(), favoriteAds)
        binding.recyclerViewFavAds.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFavAds.adapter = adAdapter
    }

    private fun loadFavorites() {
        val uid = auth.currentUser?.uid ?: return

        usersRef.child(uid).child("Favoritos")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favIds = snapshot.children.mapNotNull { it.key }
                    if (favIds.isEmpty()) {
                        binding.textNoFavAds.visibility = View.VISIBLE
                        favoriteAds.clear()
                        adAdapter.notifyDataSetChanged()
                        return
                    }

                    adsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(adSnap: DataSnapshot) {
                            favoriteAds.clear()
                            for (child in adSnap.children) {
                                val ad = child.getValue(AdModel::class.java)
                                if (ad != null && favIds.contains(ad.id)) {
                                    favoriteAds.add(ad)
                                }
                            }
                            adAdapter.notifyDataSetChanged()
                            binding.textNoFavAds.visibility =
                                if (favoriteAds.isEmpty()) View.VISIBLE else View.GONE
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}

