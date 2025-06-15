package com.mrlapidus.techcycle

import android.util.Log
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class MyAdsListTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun loginAndLaunchMainActivity() {
        val latch = CountDownLatch(1)

        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword("test1@gmail.com", "(12345678")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("TEST_LOG", "Login exitoso con test1@gmail.com")
                } else {
                    Log.e("TEST_LOG", "Error en login: ${task.exception?.message}")
                }
                latch.countDown()
            }

        if (!latch.await(10, TimeUnit.SECONDS)) {
            throw RuntimeException("Login timeout: no se pudo autenticar en el test")
        }

        // Lanzamos MainActivity ya logueado
        ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun myAdsSection_showsAtLeastOneAd() {
        // Esperamos un poco a que cargue
        Thread.sleep(2000)

        // Pulsar en el menú inferior: "Mis anuncios"
        onView(withId(R.id.nav_my_ads)).perform(click())

        // Esperamos a que cargue la sección
        Thread.sleep(2500)

        // Verificamos que hay al menos un anuncio publicado
        onView(withId(R.id.recyclerViewMyAds))
            .check(matches(hasMinimumChildCount(1)))
    }
}

