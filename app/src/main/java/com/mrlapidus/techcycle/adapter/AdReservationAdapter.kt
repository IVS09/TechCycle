package com.mrlapidus.techcycle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mrlapidus.techcycle.databinding.ItemReservationBinding
import com.mrlapidus.techcycle.model.AdModel

class AdReservationAdapter(
    private val ads: List<AdModel>,
    private val onItemClick: (AdModel) -> Unit
) : RecyclerView.Adapter<AdReservationAdapter.ReservationViewHolder>() {

    inner class ReservationViewHolder(val binding: ItemReservationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemReservationBinding.inflate(inflater, parent, false)
        return ReservationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        val ad = ads[position]
        holder.binding.tvTitle.text = ad.title
        holder.binding.tvPrice.text = "Precio: ${ad.price}€"
        holder.binding.tvStatus.text = "Estado: ${ad.status}"

        val firstImageUrl = ad.imageUrls.firstOrNull()
        Glide.with(holder.itemView.context)
            .load(firstImageUrl)
            .into(holder.binding.ivAdImage)

        holder.binding.root.setOnClickListener {
            onItemClick(ad)
        }


    }

    override fun getItemCount(): Int = ads.size
}

