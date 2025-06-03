package com.mrlapidus.techcycle.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mrlapidus.techcycle.R
import com.mrlapidus.techcycle.SelectLocation
import com.mrlapidus.techcycle.Utilities
import com.mrlapidus.techcycle.adapter.AdAdapter
import com.mrlapidus.techcycle.adapter.CategoryAdapter
import com.mrlapidus.techcycle.databinding.FragmentHomeBinding
import com.mrlapidus.techcycle.model.AdModel
import com.mrlapidus.techcycle.model.CategoryModel
import com.mrlapidus.techcycle.OnCategoryClickListener

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPrefs: SharedPreferences
    private var userLat: Double = 0.0
    private var userLng: Double = 0.0
    private var userAddress: String = ""

    private var originalFilteredList = mutableListOf<AdModel>()
    private lateinit var adAdapter: AdAdapter
    private var adList = ArrayList<AdModel>()
    private lateinit var categoryAdapter: CategoryAdapter
    private val firebaseDatabase = FirebaseDatabase.getInstance().getReference("Anuncios")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        sharedPrefs = requireContext().getSharedPreferences("LOCATION_PREFS", Context.MODE_PRIVATE)
        userLat = sharedPrefs.getFloat("LATITUDE", 0.0f).toDouble()
        userLng = sharedPrefs.getFloat("LONGITUDE", 0.0f).toDouble()
        userAddress = sharedPrefs.getString("ADDRESS", "Seleccionar ubicación") ?: "Seleccionar ubicación"

        setupUI()
        return binding.root
    }

    private fun setupUI() {
        binding.locationText.text = Editable.Factory.getInstance().newEditable(userAddress)

        binding.locationText.setOnClickListener {
            val intent = Intent(requireContext(), SelectLocation::class.java)
            locationResultLauncher.launch(intent)
        }

        setupCategoryRecyclerView()
        setupAdRecyclerView()

        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(query: CharSequence?, start: Int, before: Int, count: Int) {
                filterAds(query.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private val locationResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                userLat = data.getDoubleExtra("latitude", 0.0)
                userLng = data.getDoubleExtra("longitude", 0.0)
                userAddress = data.getStringExtra("address").toString()

                sharedPrefs.edit()
                    .putFloat("LATITUDE", userLat.toFloat())
                    .putFloat("LONGITUDE", userLng.toFloat())
                    .putString("ADDRESS", userAddress)
                    .apply()

                binding.locationText.text = Editable.Factory.getInstance().newEditable(userAddress)
                loadAds()
            }
        }
    }

    private fun setupCategoryRecyclerView() {
        val categories = Utilities.CATEGORIES.mapIndexed { index, name ->
            CategoryModel(name, Utilities.CATEGORY_ICONS[index])
        }

        categoryAdapter = CategoryAdapter(requireContext(), categories, object : OnCategoryClickListener {
            override fun onCategoryClick(category: CategoryModel) {
                loadAds(category.name)
            }
        })

        binding.categoryRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.categoryRecyclerView.adapter = categoryAdapter
    }

    private fun setupAdRecyclerView() {
        adAdapter = AdAdapter(requireContext(), adList)
        binding.adRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.adRecyclerView.adapter = adAdapter
        loadAds()
    }

    override fun onResume() {
        super.onResume()
        loadAds()
    }

    private fun loadAds(category: String = "Todos") {
        binding.adLoadingText.text = getString(R.string.home_loading_ads)
        binding.adLoadingText.visibility = View.VISIBLE

        firebaseDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newAds = mutableListOf<AdModel>()

                for (ds in snapshot.children) {
                    try {
                        val id = ds.key ?: continue
                        val ad = AdModel(
                            id = id,
                            userId = ds.child("userId").getValue(String::class.java) ?: "",
                            brand = ds.child("brand").getValue(String::class.java) ?: "",
                            category = ds.child("category").getValue(String::class.java) ?: "",
                            condition = ds.child("condition").getValue(String::class.java) ?: "",
                            location = ds.child("location").getValue(String::class.java) ?: "",
                            price = ds.child("price").getValue(String::class.java) ?: "0.0",
                            title = ds.child("title").getValue(String::class.java) ?: "",
                            description = ds.child("description").getValue(String::class.java) ?: "",
                            status = ds.child("status").getValue(String::class.java) ?: "Disponible",
                            timestamp = ds.child("timestamp").getValue(Long::class.java) ?: 0L,
                            latitud = ds.child("latitud").getValue(Double::class.java) ?: 0.0,
                            longitud = ds.child("longitud").getValue(Double::class.java) ?: 0.0,
                            viewCount = ds.child("viewCount").getValue(Int::class.java) ?: 0,
                            imageUrls = ds.child("images").children.mapNotNull {
                                it.child("imageUrl").getValue(String::class.java)
                            }.toMutableList()
                        )

                        if (ad.latitud != 0.0 && ad.longitud != 0.0) {
                            val distance = calculateDistance(ad.latitud, ad.longitud)
                            if ((category == "Todos" || category == ad.category) && distance <= 10.0) {
                                newAds.add(ad)
                            }
                        }

                    } catch (e: Exception) {
                        Log.e("AD_PARSE_ERROR", "Error: ${e.message}")
                    }
                }

                if (newAds.isEmpty()) {
                    binding.adLoadingText.text = getString(R.string.home_no_ads_found)
                } else {
                    binding.adLoadingText.visibility = View.GONE
                }

                loadUserFavorites { userFavs ->
                    val adsWithFavs = newAds.map { it.copy(isFavorite = userFavs.contains(it.id)) }
                    originalFilteredList = adsWithFavs.toMutableList()
                    adList.clear()
                    adList.addAll(originalFilteredList)
                    adAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FIREBASE_ERROR", "Error Firebase: ${error.message}")
                binding.adLoadingText.text = getString(R.string.home_no_ads_found)
            }
        })
    }

    private fun filterAds(query: String) {
        if (query.isBlank()) {
            adList.clear()
            adList.addAll(originalFilteredList)
            adAdapter.notifyDataSetChanged()
            return
        }

        val filtered = originalFilteredList.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true) ||
                    it.brand.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
        }

        adList.clear()
        adList.addAll(filtered)
        adAdapter.notifyDataSetChanged()
    }

    private fun calculateDistance(lat: Double, lng: Double): Double {
        val startPoint = Location(LocationManager.NETWORK_PROVIDER).apply {
            latitude = userLat
            longitude = userLng
        }
        val endPoint = Location(LocationManager.NETWORK_PROVIDER).apply {
            latitude = lat
            longitude = lng
        }
        return startPoint.distanceTo(endPoint).toDouble() / 1000
    }

    private fun loadUserFavorites(onResult: (Set<String>) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onResult(emptySet())
            return
        }

        val favRef = FirebaseDatabase.getInstance()
            .getReference("Usuarios")
            .child(uid)
            .child("Favoritos")

        favRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val favIds = mutableSetOf<String>()
                for (ds in snapshot.children) {
                    ds.key?.let { favIds.add(it) }
                }
                onResult(favIds)
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(emptySet())
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




