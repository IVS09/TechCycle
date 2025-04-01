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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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

    private lateinit var adAdapter: AdAdapter
    private var adList = mutableListOf<AdModel>()

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
        // Mostrar ubicación actual
        binding.locationText.text = Editable.Factory.getInstance().newEditable(userAddress)

        // Configurar selección de ubicación
        binding.locationText.setOnClickListener {
            val intent = Intent(requireContext(), SelectLocation::class.java)
            locationResultLauncher.launch(intent)
        }

        // Configurar RecyclerView de categorías
        setupCategoryRecyclerView()

        // Configurar RecyclerView de anuncios
        setupAdRecyclerView()

        // Configurar búsqueda
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

    private fun loadAds(category: String = "Todos") {
        firebaseDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FIREBASE", "Snapshot recibido con ${snapshot.childrenCount} anuncios")
                adList.clear()
                for (ds in snapshot.children) {
                    try {
                        val ad = ds.getValue(AdModel::class.java)
                        val distance = calculateDistance(ad?.latitude ?: 0.0, ad?.longitude ?: 0.0)

                        if (category == "Todos" || ad?.category == category) {
                            if (distance <= 10.0) { // Mostrar anuncios dentro de 10 km
                                Log.d("AD_CARGADO", "Añadiendo anuncio: ${ad?.title} en ${ad?.category}")
                                Log.d("DISTANCIA", "Distancia calculada: $distance km")
                                ad?.let { adList.add(it) }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                adAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error al cargar anuncios", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterAds(query: String) {
        val filteredList = adList.filter {
            it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
        }
        adAdapter = AdAdapter(requireContext(), filteredList.toMutableList())
        binding.adRecyclerView.adapter = adAdapter
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

        return startPoint.distanceTo(endPoint).toDouble() / 1000 // Convertir a kilómetros
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


