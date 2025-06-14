package com.mrlapidus.techcycle

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.mrlapidus.techcycle.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logoImageView.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.logo_fade_in)
        )

        lifecycleScope.launch {
            delay(3_000)          // 3 s
            goNextScreen()
        }
    }

    /** Si hay sesión iniciada vamos a Main, si no al On-boarding */
    private fun goNextScreen() {
        val target =
            if (FirebaseAuth.getInstance().currentUser != null) MainActivity::class.java
            else Onboarding::class.java

        startActivity(Intent(this, target))
        finish()
    }
}

