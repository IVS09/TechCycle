package com.mrlapidus.techcycle.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mrlapidus.techcycle.OnCategoryClickListener
import com.mrlapidus.techcycle.R
import com.mrlapidus.techcycle.Utilities
import com.mrlapidus.techcycle.adapter.CategoryAdapter
import com.mrlapidus.techcycle.databinding.FragmentHomeBinding
import com.mrlapidus.techcycle.model.CategoryModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Configurar RecyclerView de categorías
        setupCategoryRecyclerView()

        return binding.root
    }

    private fun setupCategoryRecyclerView() {
        val categories = Utilities.CATEGORIES.mapIndexed { index, name ->
            CategoryModel(name, Utilities.CATEGORY_ICONS[index])
        }

        val adapter = CategoryAdapter(requireContext(), categories, object :
            OnCategoryClickListener {
            override fun onCategoryClick(category: CategoryModel) {
                // Acción cuando se selecciona una categoría
                // Por ejemplo, filtrar anuncios
            }
        })

        binding.categoryRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.categoryRecyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
