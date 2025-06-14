package com.mrlapidus.techcycle

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.mrlapidus.techcycle.ads.EditAd
import com.mrlapidus.techcycle.databinding.ActivityMainBinding
import com.mrlapidus.techcycle.fragments.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home      -> load(HomeFragment())
                R.id.nav_my_ads    -> load(FragmentMyPublishedAds())
                R.id.nav_publish   -> { startActivity(Intent(this, EditAd::class.java)); false }
                R.id.nav_favorites -> load(FragmentFavAds())
                R.id.nav_profile   -> load(ProfileFragment())
                else               -> false
            }
        }

        if (savedInstanceState == null) load(HomeFragment())
    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, Onboarding::class.java))
            finish()
        }
    }

    private fun load(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }
}

