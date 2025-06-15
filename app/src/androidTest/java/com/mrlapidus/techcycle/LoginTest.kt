package com.mrlapidus.techcycle

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginTest {

    @get:Rule
    val rule = ActivityScenarioRule(Login::class.java)

    @Test
    fun loginFailsWithMalformedEmail_keepsUserOnLoginScreen() {
        // Caso 1: email sin @ → debería fallar validación local
        onView(withId(R.id.emailEditText)).perform(replaceText("test1gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.passwordEditText)).perform(replaceText("cualquierclave"), closeSoftKeyboard())
        onView(withId(R.id.loginButton)).perform(click())

        Thread.sleep(1000)

        // Si falla, seguimos en la pantalla de login
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()))
    }

    @Test
    fun loginFailsWithWrongCredentials_keepsUserOnLoginScreen() {
        // Caso 2: email válido, pero credenciales incorrectas (test1@gmail.com no existe o clave incorrecta)
        onView(withId(R.id.emailEditText)).perform(replaceText("test1@gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.passwordEditText)).perform(replaceText("claveincorrecta"), closeSoftKeyboard())
        onView(withId(R.id.loginButton)).perform(click())

        Thread.sleep(1500)

        // De nuevo, si no se navega a Home, es que sigue en Login → correcto
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()))
    }
}


