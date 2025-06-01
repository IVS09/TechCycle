package com.mrlapidus.techcycle

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mrlapidus.techcycle.adapter.ImageSliderAdapter
import com.mrlapidus.techcycle.databinding.ActivityProductDetailBinding
import com.google.firebase.auth.FirebaseAuth

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recuperar datos del intent
        val title = intent.getStringExtra("title") ?: ""
        val price = intent.getStringExtra("price") ?: ""
        val condition = intent.getStringExtra("condition") ?: ""
        val category = intent.getStringExtra("category") ?: ""
        val brand = intent.getStringExtra("brand") ?: ""
        val location = intent.getStringExtra("location") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val sellerName = intent.getStringExtra("sellerName") ?: ""
        val sellerSince = intent.getStringExtra("sellerSince") ?: ""
        val sellerAvatarUrl = intent.getStringExtra("sellerAvatarUrl") ?: ""
        val userId = intent.getStringExtra("userId") ?: ""
        val images = intent.getStringArrayListExtra("images") ?: arrayListOf()

        // Mostrar datos
        binding.productTitle.text = title
        binding.productPrice.text = getString(R.string.product_price_format, price)
        binding.productCondition.text = condition
        binding.productCategory.text = category
        binding.productBrand.text = getString(R.string.product_brand_format, brand)
        binding.productLocation.text = getString(R.string.product_location_format, location)
        binding.productDescription.text = description
        binding.sellerName.text = sellerName
        binding.sellerSince.text = getString(R.string.member_since, sellerSince)

        // Cargar avatar del vendedor
        Glide.with(this)
            .load(sellerAvatarUrl)
            .placeholder(R.drawable.ic_profile)
            .into(binding.sellerAvatar)

        // Lógica de visibilidad según si el producto pertenece al usuario actual
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId == userId) {
            binding.btnReserve.visibility = View.GONE
        } else {
            binding.btnEditAd.visibility = View.GONE
            binding.btnDeleteAd.visibility = View.GONE
        }

        // Adaptador del carrusel
        val imageSliderAdapter = ImageSliderAdapter(images)
        binding.imageCarousel.adapter = imageSliderAdapter

        // Mostrar contador de imágenes si hay más de una
        if (images.size > 1) {
            binding.imageCounter.text =
                getString(R.string.image_counter_format, 1, images.size)
            binding.imageCarousel.registerOnPageChangeCallback(object :
                androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    binding.imageCounter.text = getString(
                        R.string.image_counter_format,
                        position + 1,
                        images.size
                    )
                }
            })
        } else {
            binding.imageCounter.visibility = View.GONE
        }
    }
}
