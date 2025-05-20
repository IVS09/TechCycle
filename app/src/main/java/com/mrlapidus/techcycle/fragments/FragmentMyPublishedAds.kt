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
import com.mrlapidus.techcycle.databinding.FragmentMyPublishedAdsBinding
import com.mrlapidus.techcycle.model.AdModel

class FragmentMyPublishedAds : Fragment() {

    private var _binding: FragmentMyPublishedAdsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adAdapter: AdAdapter
    private val publishedAds = ArrayList<AdModel>()
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
                publishedAds.clear()

                for (ds in snapshot.children) {
                    val ad = ds.getValue(AdModel::class.java)
                    if (ad != null && ad.userId == currentUserId) {
                        publishedAds.add(ad)
                    }
                }

                adAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
