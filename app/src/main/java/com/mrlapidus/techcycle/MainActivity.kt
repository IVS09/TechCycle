package com.mrlapidus.techcycle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.mrlapidus.techcycle.databinding.ActivityMainBinding
import com.mrlapidus.techcycle.fragments.AdsFragment
import com.mrlapidus.techcycle.fragments.ChatFragment
import com.mrlapidus.techcycle.fragments.HomeFragment
import com.mrlapidus.techcycle.fragments.ProfileFragment
import com.mrlapidus.techcycle.fragments.ShopFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Configurar el BottomNavigationView con Fragments
        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val fragment = when (menuItem.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_chat -> ChatFragment()
                R.id.nav_shop -> ShopFragment()
                R.id.nav_ads -> AdsFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> null
            }
            fragment?.let { loadFragment(it) }
            true
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