package com.mrlapidus.techcycle.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mrlapidus.techcycle.R
import com.mrlapidus.techcycle.databinding.ItemSelectedImageBinding
import com.mrlapidus.techcycle.model.SelectedImageModel

class SelectedImageAdapter(
    private val context: Context,
    private val images: ArrayList<SelectedImageModel>
) : RecyclerView.Adapter<SelectedImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: ItemSelectedImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemSelectedImageBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return ImageViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageModel = images[position]

        // Glide para cargar la imagen
        Glide.with(context)
            .load(imageModel.imageUri)
            .placeholder(R.drawable.photo_item_icon) // Usar un drawable existente
            .into(holder.binding.selectedImageView)

        // Configurar el botón de eliminar
        holder.binding.closeImageView.setOnClickListener {
            images.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}

