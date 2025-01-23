package com.mrlapidus.techcycle.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mrlapidus.techcycle.databinding.ItemCategoryBinding
import com.mrlapidus.techcycle.model.CategoryModel

class CategoryAdapter {
    class CategoryAdapter(
        private val context: Context,
        private val categoryList: List<CategoryModel>
    ) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
        // Interfaz para manejar los clics en las categorías
        interface OnCategoryClickListener {
            fun onCategoryClick(category: CategoryModel)
        }

        // ViewHolder para el adaptador
        inner class CategoryViewHolder(val binding: ItemCategoryBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            val binding = ItemCategoryBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
            return CategoryViewHolder(binding)
        }

        override fun getItemCount(): Int = categories.size

        override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
            val category = categories[position]
            holder.binding.icon.setImageResource(category.icon)
            holder.binding.name.text = category.name

            // Configurar clic en la categoría
            holder.itemView.setOnClickListener {
                listener.onCategoryClick(category)
            }
        }
    }

}