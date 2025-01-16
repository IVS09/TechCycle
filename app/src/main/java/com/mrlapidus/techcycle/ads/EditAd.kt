package com.mrlapidus.techcycle.ads

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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

        // Configurar adaptadores de categorías y condiciones
        setupDropdowns()

        // Configurar clic en el ImageView para añadir imágenes
        binding.addImageView.setOnClickListener { showImagePickerDialog() }
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
            PackageManager.PERMISSION_GRANTED) {
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

    private fun addImageToRecyclerView(uri: Uri) {
        if (uri.scheme == "content" || uri.scheme == "file") {
            val newImage = SelectedImageModel(
                id = System.currentTimeMillis().toString(),
                imageUri = uri,
                imageUrl = null,
                isFromInternet = false
            )
            selectedImages.add(newImage)
            imageAdapter.notifyItemInserted(selectedImages.size - 1)
        } else {
            Toast.makeText(this, "URI inválida: ${uri.toString()}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }
}

