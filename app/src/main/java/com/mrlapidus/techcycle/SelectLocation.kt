package com.mrlapidus.techcycle

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mrlapidus.techcycle.databinding.ActivitySelectLocationBinding
import java.util.*

class SelectLocation : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivitySelectLocationBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
    private var selectedMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Comprobar si Places SDK está inicializado
        val apiKey = getString(R.string.google_maps_api_key)
        if (apiKey.isEmpty() || apiKey == "YOUR_API_KEY_HERE") {
            Toast.makeText(this, "Clave API no configurada correctamente", Toast.LENGTH_LONG).show()
            finish() // Finaliza la actividad si la clave API es inválida
            return
        }

        // Inicializar cliente de ubicación y mapa
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initializeMapFragment()
        setupButtons()
        initializePermissionLauncher()
    }

    private fun initializeMapFragment() {
        val mapFragment = SupportMapFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.map_fragment_container, mapFragment)
            .commit()
        mapFragment.getMapAsync(this)
    }

    private fun setupButtons() {
        binding.buttonConfirm.setOnClickListener { confirmSelectedLocation() }
        binding.buttonBack.setOnClickListener { finish() }
        binding.buttonGps.setOnClickListener {
            checkLocationPermission()
        }
    }

    private fun initializePermissionLauncher() {
        locationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) getCurrentLocation()
                else Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        googleMap.setOnMapClickListener { latLng ->
            val address = geocodeLatLng(latLng)
            updateMapLocation(latLng, address ?: "Ubicación seleccionada")
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if (!isGpsEnabled()) {
            Toast.makeText(this, "El GPS no está activado", Toast.LENGTH_SHORT).show()
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                val address = geocodeLatLng(latLng)
                updateMapLocation(latLng, address ?: "Ubicación actual")
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun isGpsEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun updateMapLocation(latLng: LatLng, title: String) {
        selectedMarker?.remove()
        selectedMarker = googleMap.addMarker(
            MarkerOptions().position(latLng).title(title)
        )
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        binding.textSelectedLocation.text = title
    }

    private fun geocodeLatLng(latLng: LatLng): String? {
        val geocoder = Geocoder(this, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            addresses?.get(0)?.getAddressLine(0)
        } catch (e: Exception) {
            null
        }
    }

    private fun confirmSelectedLocation() {
        val latLng = selectedMarker?.position
        val address = selectedMarker?.title

        if (latLng != null && address != null) {
            val resultIntent = Intent().apply {
                putExtra("latitude", latLng.latitude)
                putExtra("longitude", latLng.longitude)
                putExtra("address", address)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        } else {
            Toast.makeText(this, "Seleccione una ubicación antes de confirmar", Toast.LENGTH_SHORT).show()
        }
    }
}




