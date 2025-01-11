package com.mrlapidus.techcycle.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.mrlapidus.techcycle.R
import com.mrlapidus.techcycle.model.SelectedImageModel

class SelectedImageAdapter(
    private val context: Context,
    private val images: MutableList<SelectedImageModel>,
    private val onRemoveImage: (SelectedImageModel) -> Unit
) : RecyclerView.Adapter<SelectedImageAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.selectedImageView)
        val closeButton: ImageView = itemView.findViewById(R.id.closeImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_selected_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = images[position]
        // Mostrar la imagen seleccionada
        holder.imageView.setImageURI(image.imageUri)
        // Eliminar imagen al pulsar el botón de cerrar
        holder.closeButton.setOnClickListener {
            onRemoveImage(image)
        }
    }

    override fun getItemCount(): Int = images.size
}
