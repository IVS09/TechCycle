package com.mrlapidus.techcycle.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
    private val allFavoriteAds = arrayListOf<AdModel>()
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
        setupSearchBar()
        loadFavoriteAdIds()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewFavAds.layoutManager = LinearLayoutManager(requireContext())
        adAdapter = AdAdapter(requireContext(), favoriteAds)
        binding.recyclerViewFavAds.adapter = adAdapter
    }

    private fun setupSearchBar() {
        binding.searchBarFav.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(query: CharSequence?, start: Int, before: Int, count: Int) {
                filterFavorites(query.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterFavorites(query: String) {
        val lowerQuery = query.trim().lowercase()
        favoriteAds.clear()

        if (lowerQuery.isEmpty()) {
            favoriteAds.addAll(allFavoriteAds)
        } else {
            favoriteAds.addAll(allFavoriteAds.filter {
                it.title.lowercase().contains(lowerQuery)
            })
        }

        binding.textNoFavAds.visibility = if (favoriteAds.isEmpty()) View.VISIBLE else View.GONE
        adAdapter.notifyDataSetChanged()
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
                allFavoriteAds.clear()

                for (child in snapshot.children) {
                    val ad = child.getValue(AdModel::class.java)
                    if (ad != null && favAdIds.contains(ad.id)) {
                        ad.isFavorite = true
                        allFavoriteAds.add(ad)
                    }
                }

                favoriteAds.addAll(allFavoriteAds)
                binding.textNoFavAds.visibility =
                    if (favoriteAds.isEmpty()) View.VISIBLE else View.GONE
                adAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

