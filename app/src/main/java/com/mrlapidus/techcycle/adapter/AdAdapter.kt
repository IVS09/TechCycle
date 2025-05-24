package com.mrlapidus.techcycle.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mrlapidus.techcycle.R
import com.mrlapidus.techcycle.databinding.ItemAdBinding
import com.mrlapidus.techcycle.model.AdModel
import com.mrlapidus.techcycle.Utilities

class AdAdapter(
    private val context: Context,
    private val adsDataSet: ArrayList<AdModel>
) : RecyclerView.Adapter<AdAdapter.AdsViewHolder>() {

    private val authInstance = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdsViewHolder {
        val binding = ItemAdBinding.inflate(LayoutInflater.from(context), parent, false)
        return AdsViewHolder(binding)
    }

    override fun getItemCount(): Int = adsDataSet.size

    override fun onBindViewHolder(holder: AdsViewHolder, position: Int) {
        val currentAd = adsDataSet[position]
        holder.bind(currentAd)
        loadFavoriteStatus(currentAd, holder.binding.adCardFavoriteButton)
    }

    private fun loadFavoriteStatus(ad: AdModel, button: ImageButton) {
        val userRef = FirebaseDatabase.getInstance().getReference("Usuarios")
            .child(authInstance.uid ?: return)
            .child("Favoritos")
            .child(ad.id)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val favExists = snapshot.exists()
                ad.isFavorite = favExists
                val icon = if (favExists) R.drawable.ad_favorite_icon else R.drawable.ad_no_favorite_icon
                button.setImageResource(icon)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    inner class AdsViewHolder(val binding: ItemAdBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ad: AdModel) {
            binding.adCardTitle.text = ad.title
            binding.adCardDescription.text = ad.description
            binding.adCardCondition.text = ad.condition
            binding.adCardLocation.text = ad.location
            binding.adCardPrice.text = "${ad.price} €"
            binding.adCardPostDate.text = android.text.format.DateFormat.format("dd/MM/yyyy", ad.timestamp)

            Glide.with(context)
                .load(ad.imageUrls.firstOrNull() ?: R.drawable.ad_image_icon)
                .placeholder(R.drawable.ad_image_icon)
                .into(binding.adCardImage)

            updateFavoriteIcon(ad.isFavorite)

            binding.adCardFavoriteButton.setOnClickListener {
                ad.isFavorite = !ad.isFavorite
                updateFavoriteIcon(ad.isFavorite)

                if (ad.isFavorite) {
                    Utilities.saveAdToFavorites(context, ad.id)
                } else {
                    Utilities.removeAdFromFavorites(context, ad.id)
                }

                notifyItemChanged(bindingAdapterPosition)
            }
        }

        private fun updateFavoriteIcon(isFav: Boolean) {
            val icon = if (isFav) {
                R.drawable.ad_favorite_icon
            } else {
                R.drawable.ad_no_favorite_icon
            }
            binding.adCardFavoriteButton.setImageResource(icon)
        }
    }

    fun updateAds(newAds: List<AdModel>) {
        adsDataSet.clear()
        adsDataSet.addAll(newAds)
        notifyDataSetChanged()
    }
}



