package com.mrlapidus.techcycle.ads

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.mrlapidus.techcycle.R
import com.mrlapidus.techcycle.Utilities.CATEGORIES
import com.mrlapidus.techcycle.Utilities.CONDITIONS
import com.mrlapidus.techcycle.adapter.SelectedImageAdapter
import com.mrlapidus.techcycle.databinding.ActivityEditAdBinding
import com.mrlapidus.techcycle.model.SelectedImageModel

class EditAd : AppCompatActivity() {

    private lateinit var binding: ActivityEditAdBinding
    private lateinit var selectedImages: ArrayList<SelectedImageModel>
    private lateinit var imageAdapter: SelectedImageAdapter

    private var imageUri: Uri? = null
    private val maxImages = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración inicial
        binding = ActivityEditAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedImages = ArrayList()
        imageAdapter = SelectedImageAdapter(this, selectedImages)

        // Configurar RecyclerView
        binding.selectedImagesRecyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.selectedImagesRecyclerView.adapter = imageAdapter

        // Configurar dropdowns
        setupDropdowns()

        // Configurar clic en el ImageView para agregar imágenes
        binding.addImageView.setOnClickListener { showImagePickerDialog() }

        // Configurar el botón para publicar el anuncio
        binding.publishButton.setOnClickListener {
            if (validateInputs()) {
                Toast.makeText(this, "Todos los datos son válidos", Toast.LENGTH_SHORT).show()
                // Proceder con la lógica para guardar el anuncio (se implementará en el siguiente paso)
            }
        }
    }

    private fun setupDropdowns() {
        val categoryAdapter = ArrayAdapter(this, R.layout.item_category, CATEGORIES)
        binding.categoryAutoCompleteTextView.setAdapter(categoryAdapter)

        val conditionAdapter = ArrayAdapter(this, R.layout.item_condition, CONDITIONS)
        binding.conditionAutoCompleteTextView.setAdapter(conditionAdapter)
    }

    private fun showImagePickerDialog() {
        if (selectedImages.size >= maxImages) {
            Toast.makeText(this, "Máximo de $maxImages imágenes alcanzado", Toast.LENGTH_SHORT).show()
            return
        }

        val options = arrayOf("Cámara", "Galería")
        AlertDialog.Builder(this)
            .setTitle("Seleccionar imagen")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }.show()
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && imageUri != null) {
                addImageToRecyclerView(imageUri!!)
            } else {
                Toast.makeText(this, "No se capturó ninguna imagen", Toast.LENGTH_SHORT).show()
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                Log.d("EditAd", "URI seleccionada: $uri")
                addImageToRecyclerView(uri)
            } else {
                Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
            }
        }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, "Nueva Imagen")
                put(MediaStore.Images.Media.DESCRIPTION, "Desde la cámara")
            }
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            cameraLauncher.launch(cameraIntent)
        }
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun addImageToRecyclerView(uri: Uri) {
        val newImage = SelectedImageModel(
            id = System.currentTimeMillis().toString(),
            imageUri = uri,
            imageUrl = null,
            isFromInternet = false
        )
        selectedImages.add(newImage)
        imageAdapter.notifyItemInserted(selectedImages.size - 1)
    }

    private fun validateInputs(): Boolean {
        val brand = binding.brandEditText.text.toString().trim()
        val category = binding.categoryAutoCompleteTextView.text.toString().trim()
        val condition = binding.conditionAutoCompleteTextView.text.toString().trim()
        val location = binding.locationAutoCompleteTextView.text.toString().trim()
        val price = binding.priceEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()

        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "Debe agregar al menos una imagen", Toast.LENGTH_SHORT).show()
            return false
        }

        if (brand.isEmpty()) {
            binding.brandEditText.error = "Por favor, ingrese el nombre de la marca"
            binding.brandEditText.requestFocus()
            return false
        }

        if (category.isEmpty()) {
            binding.categoryAutoCompleteTextView.error = "Por favor, seleccione una categoría"
            binding.categoryAutoCompleteTextView.requestFocus()
            return false
        }

        if (condition.isEmpty()) {
            binding.conditionAutoCompleteTextView.error = "Por favor, seleccione una condición"
            binding.conditionAutoCompleteTextView.requestFocus()
            return false
        }

        if (location.isEmpty()) {
            binding.locationAutoCompleteTextView.error = "Por favor, ingrese una ubicación"
            binding.locationAutoCompleteTextView.requestFocus()
            return false
        }

        if (price.isEmpty()) {
            binding.priceEditText.error = "Por favor, ingrese un precio"
            binding.priceEditText.requestFocus()
            return false
        }

        if (description.isEmpty()) {
            binding.descriptionEditText.error = "Por favor, ingrese una descripción"
            binding.descriptionEditText.requestFocus()
            return false
        }

        return true
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }
}



