package com.mrlapidus.techcycle.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mrlapidus.techcycle.fragments.FragmentMyPublishedAds
import com.mrlapidus.techcycle.fragments.FragmentFavAds

class AdsPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentMyPublishedAds()
            1 -> FragmentFavAds()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}

