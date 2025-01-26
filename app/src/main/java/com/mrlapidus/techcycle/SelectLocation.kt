package com.mrlapidus.techcycle

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.mrlapidus.techcycle.databinding.ActivitySelectLocationBinding

class SelectLocation : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivitySelectLocationBinding
    private lateinit var googleMap: GoogleMap
    private var selectedMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración inicial
        binding = ActivitySelectLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_api_key))
        }

        // Configurar fragmentos dinámicamente
        setupAutocompleteFragment()
        setupMapFragment()

        // Configurar el botón de confirmación
        binding.buttonConfirm.setOnClickListener {
            confirmSelectedLocation()
        }
    }

    private fun setupAutocompleteFragment() {
        val autocompleteFragment = AutocompleteSupportFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.autocomplete_fragment_container, autocompleteFragment)
            .commit()

        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS
            )
        )
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val latLng = place.latLng ?: return
                updateMapLocation(latLng, place.name ?: "Ubicación seleccionada")
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
                Toast.makeText(
                    this@SelectLocation,
                    "Error al seleccionar lugar: ${status.statusMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setupMapFragment() {
        val mapFragment = SupportMapFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.map_fragment_container, mapFragment)
            .commit()

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        // Agregar marcador al hacer clic en el mapa
        googleMap.setOnMapClickListener { latLng ->
            updateMapLocation(latLng, "Ubicación seleccionada manualmente")
        }
    }

    private fun updateMapLocation(latLng: LatLng, title: String) {
        // Eliminar marcador anterior si existe
        selectedMarker?.remove()

        // Agregar nuevo marcador y mover cámara
        selectedMarker = googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
        )
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        // Actualizar texto de ubicación seleccionada
        binding.textSelectedLocation.text = title
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
            Toast.makeText(this, "Seleccione una ubicación antes de confirmar", Toast.LENGTH_SHORT)
                .show()
        }
    }
}
