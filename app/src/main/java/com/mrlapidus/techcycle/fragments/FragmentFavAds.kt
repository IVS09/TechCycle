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

    private var _binding: FragmentFavAdsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adAdapter: AdAdapter
    private val favoriteAds = arrayListOf<AdModel>()
    private val favAdIds = mutableSetOf<String>()

    private val userId by lazy { FirebaseAuth.getInstance().currentUser?.uid.orEmpty() }
    private val dbUsers by lazy { FirebaseDatabase.getInstance().getReference("Usuarios") }
    private val dbAds by lazy { FirebaseDatabase.getInstance().getReference("Anuncios") }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavAdsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadFavoriteAdIds()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewFavAds.layoutManager = LinearLayoutManager(requireContext())
        adAdapter = AdAdapter(requireContext(), favoriteAds)
        binding.recyclerViewFavAds.adapter = adAdapter
    }

    private fun loadFavoriteAdIds() {
        dbUsers.child(userId).child("Favoritos")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    favAdIds.clear()
                    for (child in snapshot.children) {
                        val adId = child.key
                        if (!adId.isNullOrEmpty()) favAdIds.add(adId)
                    }
                    loadFavoriteAds()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadFavoriteAds() {
        dbAds.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favoriteAds.clear()
                for (child in snapshot.children) {
                    val ad = child.getValue(AdModel::class.java)
                    if (ad != null && favAdIds.contains(ad.id)) {
                        ad.isFavorite = true
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

