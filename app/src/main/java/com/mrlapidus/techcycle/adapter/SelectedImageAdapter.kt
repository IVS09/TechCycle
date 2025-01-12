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
        // Inflamos el layout usando View Binding
        val binding = ItemSelectedImageBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return ImageViewHolder(binding)
    }

    override fun getItemCount(): Int {
        // Devuelve el tamaño de la lista de imágenes
        return images.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageModel = images[position]

        // Cargar la imagen usando Glide
        Glide.with(context)
            .load(imageModel.imageUri) // Carga el URI desde el modelo
            .placeholder(R.drawable.photo_item_icon) // Imagen por defecto
            .into(holder.binding.selectedImageView)

        // Configurar el botón de cerrar
        holder.binding.closeImageView.setOnClickListener {
            images.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
