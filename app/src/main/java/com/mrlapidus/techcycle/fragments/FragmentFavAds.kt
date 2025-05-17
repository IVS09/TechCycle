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

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var favRef: DatabaseReference

    private lateinit var adAdapter: AdAdapter
    private var favAdList = mutableListOf<AdModel>()
    private var originalList = mutableListOf<AdModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavAdsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        firebaseAuth = FirebaseAuth.getInstance()
        favRef = FirebaseDatabase.getInstance().getReference("Anuncios")

        setupRecyclerView()
        loadFavorites()
        setupSearchBar()
    }

    private fun setupRecyclerView() {
        adAdapter = AdAdapter(requireContext(), favAdList)
        binding.recyclerViewFavAds.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFavAds.adapter = adAdapter
    }

    private fun loadFavorites() {
        val userId = firebaseAuth.uid ?: return
        val userFavRef = FirebaseDatabase.getInstance()
            .getReference("Usuarios").child(userId).child("Favoritos")

        userFavRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favAdList.clear()
                val favIds = snapshot.children.mapNotNull { it.key }

                favRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(allSnapshot: DataSnapshot) {
                        for (ds in allSnapshot.children) {
                            val ad = ds.getValue(AdModel::class.java)
                            if (ad != null && favIds.contains(ad.id)) {
                                favAdList.add(ad)
                            }
                        }
                        originalList = favAdList.toMutableList()
                        adAdapter.updateList(favAdList)
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupSearchBar() {
        binding.searchBarFav.setOnFocusChangeListener { _, _ -> filterAds(binding.searchBarFav.text.toString()) }

        binding.searchBarFav.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterAds(s.toString())
            }
        })
    }

    private fun filterAds(query: String) {
        if (query.isBlank()) {
            adAdapter.updateList(originalList)
            return
        }

        val filtered = originalList.filter {
            it.title.contains(query, true) ||
                    it.description.contains(query, true) ||
                    it.brand.contains(query, true) ||
                    it.category.contains(query, true)
        }

        adAdapter.updateList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
