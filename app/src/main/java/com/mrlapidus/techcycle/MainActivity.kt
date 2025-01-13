package com.mrlapidus.techcycle

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.mrlapidus.techcycle.ads.EditAd
import com.mrlapidus.techcycle.databinding.ActivityMainBinding
import com.mrlapidus.techcycle.fragments.AdsFragment
import com.mrlapidus.techcycle.fragments.ChatFragment
import com.mrlapidus.techcycle.fragments.HomeFragment
import com.mrlapidus.techcycle.fragments.ProfileFragment
import com.mrlapidus.techcycle.fragments.ShopFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Configurar el BottomNavigationView con Fragments
        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_chat -> {
                    loadFragment(ChatFragment())
                    true
                }
                R.id.nav_shop -> {
                    // Abrir la actividad EditAd directamente
                    val intent = Intent(this, EditAd::class.java)
                    startActivity(intent)
                    false // No marcar "Shop" como seleccionado en el menú
                }
                R.id.nav_ads -> {
                    loadFragment(AdsFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        // Cargar el fragmento inicial (opcionalmente HomeFragment)
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
