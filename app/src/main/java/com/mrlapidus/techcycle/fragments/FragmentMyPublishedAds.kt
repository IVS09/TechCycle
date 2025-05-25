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
import com.mrlapidus.techcycle.databinding.FragmentMyPublishedAdsBinding
import com.mrlapidus.techcycle.model.AdModel

class FragmentMyPublishedAds : Fragment() {

    private var _binding: FragmentMyPublishedAdsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adAdapter: AdAdapter
    private val publishedAds = ArrayList<AdModel>()
    private val allAds = ArrayList<AdModel>()

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPublishedAdsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Anuncios")

        setupRecyclerView()
        loadUserAds()
        setupSearchBar()
    }

    private fun setupRecyclerView() {
        adAdapter = AdAdapter(requireContext(), publishedAds)
        binding.recyclerViewMyAds.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adAdapter
        }
    }

    private fun loadUserAds() {
        val currentUserId = auth.currentUser?.uid ?: return

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allAds.clear()
                publishedAds.clear()

                for (ds in snapshot.children) {
                    val ad = ds.getValue(AdModel::class.java)
                    if (ad != null && ad.userId == currentUserId) {
                        allAds.add(ad)
                    }
                }

                publishedAds.addAll(allAds)
                adAdapter.notifyDataSetChanged()
                binding.textNoPublishedAds.visibility =
                    if (publishedAds.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        })
    }

    private fun setupSearchBar() {
        binding.searchBarPublished.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterAds(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filterAds(query: String) {
        val filteredList = if (query.isEmpty()) {
            allAds
        } else {
            allAds.filter {
                it.title.contains(query, ignoreCase = true)
            }
        }

        publishedAds.clear()
        publishedAds.addAll(filteredList)
        adAdapter.notifyDataSetChanged()
        binding.textNoPublishedAds.visibility =
            if (publishedAds.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

