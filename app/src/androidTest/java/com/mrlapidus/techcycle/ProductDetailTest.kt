package com.mrlapidus.techcycle

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductDetailTest {

    @get:Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun openFirstAd_showsTitle() {
        // 1) Home debe mostrar al menos 1 anuncio
        onView(withId(R.id.adRecyclerView))
            .check(matches(hasMinimumChildCount(1)))

        // 2) Tocar el primer anuncio del RecyclerView
        onView(withId(R.id.adRecyclerView))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0, click()
                )
            )

        // 3) Comprobar que en ProductDetailActivity se ve el título
        onView(withId(R.id.productTitle))
            .check(matches(isDisplayed()))
    }
}
