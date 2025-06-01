package com.mrlapidus.techcycle

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.mrlapidus.techcycle.adapter.ImageSliderAdapter
import com.mrlapidus.techcycle.databinding.ActivityProductDetailBinding

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recoge los datos del Intent
        val title = intent.getStringExtra("title") ?: ""
        val price = intent.getStringExtra("price") ?: "0.0"
        val condition = intent.getStringExtra("condition") ?: ""
        val category = intent.getStringExtra("category") ?: ""
        val brand = intent.getStringExtra("brand") ?: ""
        val location = intent.getStringExtra("location") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val sellerName = intent.getStringExtra("sellerName") ?: ""
        val sellerSince = intent.getStringExtra("sellerSince") ?: ""
        val sellerAvatarUrl = intent.getStringExtra("sellerAvatarUrl") ?: ""
        val images = intent.getStringArrayListExtra("images") ?: arrayListOf()
        val ownerId = intent.getStringExtra("ownerId") ?: ""

        // Muestra los datos
        binding.productTitle.text = title
        binding.productPrice.text = getString(R.string.product_price_format, price)
        binding.productCondition.text = condition
        binding.productCategory.text = category
        binding.productBrand.text = getString(R.string.product_brand_format, brand)
        binding.productLocation.text = getString(R.string.product_location_format, location)
        binding.productDescription.text = description
        binding.sellerName.text = sellerName
        binding.sellerSince.text = getString(R.string.member_since, sellerSince)

        Glide.with(this)
            .load(sellerAvatarUrl)
            .placeholder(R.drawable.ic_profile)
            .into(binding.sellerAvatar)

        // Carga el carrusel de imágenes
        val adapter = ImageSliderAdapter(images)
        binding.imageCarousel.adapter = adapter
        binding.imageCounter.text = getString(R.string.image_counter_format, 1, images.size)

        // Oculta botones si el usuario es el dueño
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val isOwner = currentUserId == ownerId

        binding.btnReserve.visibility = if (isOwner) View.GONE else View.VISIBLE
        binding.btnEditAd.visibility = if (isOwner) View.VISIBLE else View.GONE
        binding.btnDeleteAd.visibility = if (isOwner) View.VISIBLE else View.GONE
    }
}
