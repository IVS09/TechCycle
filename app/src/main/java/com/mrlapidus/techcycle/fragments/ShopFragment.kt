package com.mrlapidus.techcycle.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mrlapidus.techcycle.R

class ShopFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout del fragmento shop_fragment.xml
        return inflater.inflate(R.layout.fragment_shop, container, false)
    }
}
