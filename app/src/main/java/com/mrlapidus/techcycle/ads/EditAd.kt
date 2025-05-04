package com.mrlapidus.techcycle.ads

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.mrlapidus.techcycle.R
import com.mrlapidus.techcycle.SelectLocation
import com.mrlapidus.techcycle.Utilities.CATEGORIES
import com.mrlapidus.techcycle.Utilities.CONDITIONS
import com.mrlapidus.techcycle.adapter.SelectedImageAdapter
import com.mrlapidus.techcycle.databinding.ActivityEditAdBinding
import com.mrlapidus.techcycle.model.SelectedImageModel

class EditAd : AppCompatActivity() {

    private lateinit var binding: ActivityEditAdBinding
    private lateinit var selectedImages: ArrayList<SelectedImageModel>
    private lateinit var imageAdapter: SelectedImageAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    private var imageUri: Uri? = null
    private val maxImages = 3
    private var selectedLatitude: Double = 0.0
    private var selectedLongitude: Double = 0.0

    // Launcher para recibir la ubicación seleccionada
    private val selectLocationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val address = result.data?.getStringExtra("address")
                val latitude = result.data?.getDoubleExtra("latitude", 0.0) ?: 0.0
                val longitude = result.data?.getDoubleExtra("longitude", 0.0) ?: 0.0
                binding.locationAutoCompleteTextView.setText(address ?: "Ubicación no seleccionada")
                // Guardar las coordenadas para usarlas al subir el anuncio
                selectedLatitude = latitude
                selectedLongitude = longitude
            }
        }

    // Launcher para solicitar permisos de ubicación
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración inicial
        binding = ActivityEditAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        selectedImages = ArrayList()
        imageAdapter = SelectedImageAdapter(this, selectedImages)

        // Configurar RecyclerView
        binding.selectedImagesRecyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.selectedImagesRecyclerView.adapter = imageAdapter

        // Configurar dropdowns
        setupDropdowns()

        // Configurar clic en el campo de ubicación
        binding.locationAutoCompleteTextView.setOnClickListener {
            val intent = Intent(this, SelectLocation::class.java)
            selectLocationLauncher.launch(intent)
        }

        // Verificar permisos de ubicación (opcional si se usa GPS en SelectLocation)
        checkLocationPermission()

        // Configurar clic en el ImageView para agregar imágenes
        binding.addImageView.setOnClickListener { showImagePickerDialog() }

        // Configurar botón de publicar
        binding.publishButton.setOnClickListener {
            if (validateInputs()) {
                uploadAdToFirebase()
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
        val title = binding.titleEditText.text.toString().trim()

        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "Debe agregar al menos una imagen", Toast.LENGTH_SHORT).show()
            return false
        }

        if (brand.isEmpty() || category.isEmpty() || condition.isEmpty() || location.isEmpty() ||
            price.isEmpty() || description.isEmpty() || title.isEmpty()
        ) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun uploadAdToFirebase() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Subiendo anuncio...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val databaseReference = FirebaseDatabase.getInstance().getReference("Anuncios")
        val adId = databaseReference.push().key ?: return

        val adData = mapOf(
            "id" to adId,
            "brand" to binding.brandEditText.text.toString().trim(),
            "category" to binding.categoryAutoCompleteTextView.text.toString().trim(),
            "condition" to binding.conditionAutoCompleteTextView.text.toString().trim(),
            "location" to binding.locationAutoCompleteTextView.text.toString().trim(),
            "price" to binding.priceEditText.text.toString().trim(),
            "title" to title,
            "description" to binding.descriptionEditText.text.toString().trim(),
            "userId" to firebaseAuth.uid,
            "latitud" to selectedLatitude,
            "longitud" to selectedLongitude,
            "timestamp" to System.currentTimeMillis()
        )

        databaseReference.child(adId).setValue(adData)
            .addOnSuccessListener {
                uploadImagesToStorage(adId, progressDialog)
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al subir el anuncio: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImagesToStorage(adId: String, progressDialog: ProgressDialog) {
        val storageReference = FirebaseStorage.getInstance().reference.child("AdImages")
        var uploadedCount = 0

        for (image in selectedImages) {
            val imageRef = storageReference.child("$adId/${image.id}.jpg")
            imageRef.putFile(image.imageUri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        val imageData = mapOf("imageUrl" to imageUrl)

                        FirebaseDatabase.getInstance().getReference("Anuncios")
                            .child(adId).child("images").child(image.id)
                            .setValue(imageData)
                            .addOnCompleteListener {
                                uploadedCount++
                                if (uploadedCount == selectedImages.size) {
                                    progressDialog.dismiss()
                                    Toast.makeText(this, "Anuncio subido exitosamente", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            }
                    }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Error al subir imágenes: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }
}










