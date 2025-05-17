package com.mrlapidus.techcycle.adapter

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class AdsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return PlaceholderFragment(position)
    }

    class PlaceholderFragment(private val index: Int) : Fragment() {
        override fun onCreateView(
            inflater: android.view.LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            return TextView(requireContext()).apply {
                text = if (index == 0) "Publicados" else "Favoritos"
                textSize = 24f
                gravity = Gravity.CENTER
            }
        }
    }
}

