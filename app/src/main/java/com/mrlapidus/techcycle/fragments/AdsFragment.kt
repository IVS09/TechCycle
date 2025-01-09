package com.mrlapidus.techcycle.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mrlapidus.techcycle.R

class AdsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout activity_edit_ad.xml en este fragmento
        return inflater.inflate(R.layout.activity_edit_ad, container, false)
    }
}
