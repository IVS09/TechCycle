package com.mrlapidus.techcycle.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.mrlapidus.techcycle.databinding.FragmentAdsBinding

class AdsFragment : Fragment() {

    private var _binding: FragmentAdsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = object : FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                return when (position) {
                    0 -> FragmentMyPublishedAds()
                    1 -> FragmentFavAds()
                    else -> FragmentMyPublishedAds()
                }
            }

            override fun getCount(): Int = 2

            override fun getPageTitle(position: Int): CharSequence {
                return when (position) {
                    0 -> "Publicados"
                    1 -> "Favoritos"
                    else -> ""
                }
            }
        }

        binding.viewPager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
