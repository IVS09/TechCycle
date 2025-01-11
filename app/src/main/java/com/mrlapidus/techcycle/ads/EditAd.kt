package com.mrlapidus.techcycle.ads

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.mrlapidus.techcycle.R
import com.mrlapidus.techcycle.adapter.SelectedImageAdapter
import com.mrlapidus.techcycle.databinding.ActivityEditAdBinding
import com.mrlapidus.techcycle.model.SelectedImageModel
import java.util.UUID

class EditAd : AppCompatActivity() {

    private lateinit var binding: ActivityEditAdBinding
    private val selectedImages = mutableListOf<SelectedImageModel>()
    private lateinit var imageAdapter: SelectedImageAdapter
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupDropdowns()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        imageAdapter = SelectedImageAdapter(this, selectedImages) { image ->
            selectedImages.remove(image)
            imageAdapter.notifyDataSetChanged()
        }
        binding.selectedImagesRecyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.selectedImagesRecyclerView.adapter = imageAdapter
    }

    private fun setupDropdowns() {
        // Lista de categorías
        val categories = listOf("Móviles", "Electrodomésticos", "Vehículos", "Consolas", "Muebles")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.categoryAutoCompleteTextView.setAdapter(categoryAdapter)

        // Lista de condiciones
        val conditions = listOf("Nuevo", "Usado", "Reacondicionado")
        val conditionAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, conditions)
        binding.conditionAutoCompleteTextView.setAdapter(conditionAdapter)

        // Lista de locaciones
        val locations = listOf("Madrid", "Barcelona", "Valencia", "Sevilla", "Bilbao")
        val locationAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, locations)
        binding.locationAutoCompleteTextView.setAdapter(locationAdapter)
    }

    private fun setupClickListeners() {
        binding.addImageView.setOnClickListener { showImageOptions() }
        binding.publishButton.setOnClickListener { validateAndPublishAd() }
    }

    private fun showImageOptions() {
        val popupMenu = androidx.appcompat.widget.PopupMenu(this, binding.addImageView)
        popupMenu.menu.add(1, 1, 1, getString(R.string.option_camera))
        popupMenu.menu.add(1, 2, 2, getString(R.string.option_gallery))
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> requestCameraPermission()
                2 -> pickImageFromGallery()
            }
            true
        }
        popupMenu.show()
    }

    private fun requestCameraPermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.CAMERA)
        } else {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        cameraPermissionLauncher.launch(permissions)
    }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.all { it.value }) captureImageFromCamera()
            else Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
        }

    private fun captureImageFromCamera() {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, getString(R.string.image_title))
            put(MediaStore.Images.Media.DESCRIPTION, getString(R.string.image_description))
        }
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(intent)
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && imageUri != null) {
                selectedImages.add(SelectedImageModel(UUID.randomUUID().toString(), imageUri))
                imageAdapter.notifyDataSetChanged()
            }
        }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    selectedImages.add(SelectedImageModel(UUID.randomUUID().toString(), imageUri))
                    imageAdapter.notifyDataSetChanged()
                }
            }
        }

    private fun validateAndPublishAd() {
        val brand = binding.brandEditText.text.toString().trim()
        val category = binding.categoryAutoCompleteTextView.text.toString().trim()
        val condition = binding.conditionAutoCompleteTextView.text.toString().trim()
        val location = binding.locationAutoCompleteTextView.text.toString().trim()
        val price = binding.priceEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()

        when {
            brand.isEmpty() -> binding.brandEditText.error = getString(R.string.error_empty_brand)
            category.isEmpty() -> binding.categoryAutoCompleteTextView.error = getString(R.string.error_empty_category)
            condition.isEmpty() -> binding.conditionAutoCompleteTextView.error = getString(R.string.error_empty_condition)
            location.isEmpty() -> binding.locationAutoCompleteTextView.error = getString(R.string.error_empty_location)
            price.isEmpty() -> binding.priceEditText.error = getString(R.string.error_empty_price)
            description.isEmpty() -> binding.descriptionEditText.error = getString(R.string.error_empty_description)
            selectedImages.isEmpty() -> Toast.makeText(this, getString(R.string.error_no_images), Toast.LENGTH_SHORT).show()
            else -> saveAdToFirebase(brand, category, condition, location, price, description)
        }
    }

    private fun saveAdToFirebase(
        brand: String,
        category: String,
        condition: String,
        location: String,
        price: String,
        description: String
    ) {
        val database = FirebaseDatabase.getInstance().getReference("Anuncios")
        val adId = database.push().key ?: return
        val ad = mapOf(
            "id" to adId,
            "brand" to brand,
            "category" to category,
            "condition" to condition,
            "location" to location,
            "price" to price,
            "description" to description
        )
        database.child(adId).setValue(ad).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                uploadImagesToFirebase(adId)
            } else {
                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImagesToFirebase(adId: String) {
        val storage = FirebaseStorage.getInstance()
        selectedImages.forEach { image ->
            val ref = storage.reference.child("Anuncios/$adId/${image.id}.jpg")
            image.imageUri?.let { uri ->
                ref.putFile(uri).addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.ad_published), Toast.LENGTH_SHORT).show()
                    clearFields()
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "Error uploading image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun clearFields() {
        binding.brandEditText.text = null
        binding.categoryAutoCompleteTextView.text = null
        binding.conditionAutoCompleteTextView.text = null
        binding.locationAutoCompleteTextView.text = null
        binding.priceEditText.text = null
        binding.descriptionEditText.text = null
        selectedImages.clear()
        imageAdapter.notifyDataSetChanged()
    }
}



