package com.mrlapidus.techcycle

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ReserveAdTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun loginAndLaunchMainActivity() {
        val latch = CountDownLatch(1)

        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword("test1@gmail.com", "12345678")
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

        ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun reserveFirstAd_changesButtonState() {
        Thread.sleep(2000)

        onView(withId(R.id.adRecyclerView))
            .check(matches(hasMinimumChildCount(1)))

        onView(withId(R.id.adRecyclerView))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0, click()
                )
            )

        Thread.sleep(2000)

        // Pulsar el botón de reserva
        onView(withId(R.id.btnReserve)).perform(click())

        Thread.sleep(1000)

        // Verificar que el botón está desactivado y el texto ha cambiado
        onView(withId(R.id.btnReserve))
            .check(matches(not(isEnabled())))
            .check(matches(withText("Reserva pendiente")))
    }
}








