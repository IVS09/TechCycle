package com.mrlapidus.techcycle.ads

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.mrlapidus.techcycle.R
import com.mrlapidus.techcycle.Utilities
import com.mrlapidus.techcycle.adapter.SelectedImageAdapter
import com.mrlapidus.techcycle.databinding.ActivityEditAdBinding
import com.mrlapidus.techcycle.model.SelectedImageModel
import java.util.UUID

class EditAd : AppCompatActivity() {

    private lateinit var binding: ActivityEditAdBinding
    private var imageUri: Uri? = null

    private val selectedImages = mutableListOf<SelectedImageModel>()
    private lateinit var imageAdapter: SelectedImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerView()
        setupDropdowns()
    }

    private fun setupUI() {
        binding.addImageView.setOnClickListener { showImageSelectionMenu() }
        binding.publishButton.setOnClickListener { validateAndPublishAd() }
    }

    private fun setupRecyclerView() {
        imageAdapter = SelectedImageAdapter(this, selectedImages) { image ->
            val position = selectedImages.indexOf(image)
            selectedImages.remove(image)
            imageAdapter.notifyItemRemoved(position)
        }
        binding.selectedImagesRecyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.selectedImagesRecyclerView.adapter = imageAdapter
    }

    private fun setupDropdowns() {
        val categoryAdapter = ArrayAdapter(this, R.layout.item_category, Utilities.CATEGORIES)
        binding.categoryAutoCompleteTextView.setAdapter(categoryAdapter)

        val conditionAdapter = ArrayAdapter(this, R.layout.item_condition, Utilities.CONDITIONS)
        binding.conditionAutoCompleteTextView.setAdapter(conditionAdapter)
    }

    private fun validateAndPublishAd() {
        val brand = binding.brandEditText.text.toString().trim()
        val category = binding.categoryAutoCompleteTextView.text.toString().trim()
        val condition = binding.conditionAutoCompleteTextView.text.toString().trim()
        val price = binding.priceEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()

        when {
            brand.isEmpty() -> binding.brandEditText.error = getString(R.string.error_empty_brand)
            category.isEmpty() -> binding.categoryAutoCompleteTextView.error = getString(R.string.error_empty_category)
            condition.isEmpty() -> binding.conditionAutoCompleteTextView.error = getString(R.string.error_empty_condition)
            price.isEmpty() -> binding.priceEditText.error = getString(R.string.error_empty_price)
            description.isEmpty() -> binding.descriptionEditText.error = getString(R.string.error_empty_description)
            selectedImages.isEmpty() -> Toast.makeText(this, getString(R.string.error_no_images), Toast.LENGTH_SHORT).show()
            else -> publishAdToFirebase(brand, category, condition, price, description)
        }
    }

    private fun publishAdToFirebase(brand: String, category: String, condition: String, price: String, description: String) {
        val userId = FirebaseAuth.getInstance().uid ?: return
        val adId = UUID.randomUUID().toString()

        val adData = mapOf(
            "brand" to brand,
            "category" to category,
            "condition" to condition,
            "price" to price,
            "description" to description,
            "userId" to userId
        )

        val databaseRef = FirebaseDatabase.getInstance().getReference("Ads").child(adId)
        databaseRef.setValue(adData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                uploadImagesToFirebase(adId)
            } else {
                Toast.makeText(this, "Failed to publish ad: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImagesToFirebase(adId: String) {
        val storageRef = FirebaseStorage.getInstance().reference
        var uploadCount = 0

        for (image in selectedImages) {
            val imageRef = storageRef.child("Ads/$adId/${image.id}.jpg")
            image.imageUri?.let { uri ->
                imageRef.putFile(uri).addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        saveImageUrlToDatabase(adId, downloadUri.toString())
                        uploadCount++
                        if (uploadCount == selectedImages.size) {
                            Toast.makeText(this, getString(R.string.ad_published), Toast.LENGTH_SHORT).show()
                            clearFields()
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to upload image: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveImageUrlToDatabase(adId: String, imageUrl: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Ads").child(adId).child("images")
        val imageId = UUID.randomUUID().toString()
        databaseRef.child(imageId).setValue(imageUrl)
    }

    private fun clearFields() {
        binding.brandEditText.text = null
        binding.categoryAutoCompleteTextView.text = null
        binding.conditionAutoCompleteTextView.text = null
        binding.priceEditText.text = null
        binding.descriptionEditText.text = null
        val itemCount = selectedImages.size
        selectedImages.clear()
        imageAdapter.notifyItemRangeRemoved(0, itemCount)
    }

    private fun showImageSelectionMenu() {
        val options = arrayOf(getString(R.string.option_camera), getString(R.string.option_gallery))
        android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.edit_ad_add_image))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> launchCamera()
                    1 -> pickImageFromGallery()
                }
            }
            .show()
    }

    private fun launchCamera() {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, getString(R.string.image_title))
            put(MediaStore.Images.Media.DESCRIPTION, getString(R.string.image_description))
        }
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(intent)
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && imageUri != null) {
            val imageModel = SelectedImageModel(UUID.randomUUID().toString(), imageUri)
            selectedImages.add(imageModel)
            imageAdapter.notifyItemInserted(selectedImages.size - 1)
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            if (imageUri != null) {
                val imageModel = SelectedImageModel(UUID.randomUUID().toString(), imageUri)
                selectedImages.add(imageModel)
                imageAdapter.notifyItemInserted(selectedImages.size - 1)
            }
        }
    }
}


