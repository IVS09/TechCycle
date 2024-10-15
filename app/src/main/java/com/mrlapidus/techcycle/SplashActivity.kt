package com.mrlapidus.techcycle

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mrlapidus.techcycle.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configura viewBinding
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Aplicar la animación al logotipo antes del retardo
        val logoAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_fade_in)
        binding.logoImageView.startAnimation(logoAnimation)

        // Inicio el splash con un retardo usando coroutines
        lifecycleScope.launch {
            delay(3000) // 3 segundos de retardo (ajústar según lo necesite)
            navigateToOnboarding()
        }
    }

    private fun navigateToOnboarding() {
        // Cambia OnboardingActivity a la Activity de destino, o MainActivity
        val intent = Intent(this, Onboarding::class.java)
        startActivity(intent)
        finish() // Cierra la SplashActivity para que no vuelva a aparecer al presionar back
    }
}
