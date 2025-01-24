package com.mrlapidus.techcycle.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mrlapidus.techcycle.databinding.ItemCategoryStartBinding
import com.mrlapidus.techcycle.model.CategoryModel
import com.mrlapidus.techcycle.OnCategoryClickListener

class CategoryAdapter(
    private val context: Context,
    private val categories: List<CategoryModel>,
    private val listener: OnCategoryClickListener
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    // ViewHolder para el adaptador
    inner class CategoryViewHolder(val binding: ItemCategoryStartBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryStartBinding.inflate(
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
