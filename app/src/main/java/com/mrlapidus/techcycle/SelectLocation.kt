package com.mrlapidus.techcycle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.api.model.Place
import com.mrlapidus.techcycle.R

class SelectLocation : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_location)

        // Inicializar Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_api_key))
        }

        // Inicializar AutocompleteSupportFragment
        val autocompleteFragment = AutocompleteSupportFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.autocomplete_fragment_container, autocompleteFragment)
            .commit()

        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // Lugar seleccionado
                val name = place.name
                val latLng = place.latLng
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
                // Manejar errores
            }
        })

        // Inicializar SupportMapFragment
        val mapFragment = SupportMapFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.map_fragment_container, mapFragment)
            .commit()

        mapFragment.getMapAsync { googleMap ->
            googleMap.uiSettings.isZoomControlsEnabled = true
        }
    }
}
