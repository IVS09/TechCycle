package com.mrlapidus.techcycle.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mrlapidus.techcycle.R
import com.mrlapidus.techcycle.databinding.ItemAdBinding
import com.mrlapidus.techcycle.model.AdModel

class AdAdapter(private val context: Context, private val adList: MutableList<AdModel>) :
    RecyclerView.Adapter<AdAdapter.AdViewHolder>() {

    inner class AdViewHolder(private val binding: ItemAdBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ad: AdModel) {
            binding.adCardTitle.text = ad.title
            binding.adCardDescription.text = ad.description // <-- Añadido
            binding.adCardCondition.text = ad.condition // <-- Añadido
            binding.adCardLocation.text = ad.location
            binding.adCardPrice.text = context.getString(R.string.ad_card_price, ad.price)
            binding.adCardPostDate.text = android.text.format.DateFormat.format("dd/MM/yyyy", ad.timestamp)

            Glide.with(context)
                .load(ad.imageUrls.firstOrNull() ?: R.drawable.ad_image_icon)
                .placeholder(R.drawable.ad_image_icon)
                .into(binding.adCardImage)

            val favoriteIcon = if (ad.isFavorite) {
                R.drawable.ad_favorite_icon
            } else {
                R.drawable.ad_no_favorite_icon
            }
            binding.adCardFavoriteButton.setImageResource(favoriteIcon)

            binding.adCardFavoriteButton.setOnClickListener {
                val newFavoriteStatus = !ad.isFavorite
                adList[bindingAdapterPosition] = ad.copy(isFavorite = newFavoriteStatus)
                notifyItemChanged(bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdViewHolder {
        val binding = ItemAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdViewHolder, position: Int) {
        holder.bind(adList[position])
    }

    override fun getItemCount(): Int = adList.size

    fun updateList(newList: List<AdModel>) {
        adList.clear()
        adList.addAll(newList)
        notifyDataSetChanged()
    }
}
