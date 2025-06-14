package com.mrlapidus.techcycle.ads

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.TextView
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

    // ────────────── modo de trabajo ──────────────
    private var mode = "create"   // create | edit
    private var adId = ""         // id del anuncio a editar

    // ────────────── UI & helpers ──────────────
    private lateinit var binding: ActivityEditAdBinding
    private lateinit var selectedImages: ArrayList<SelectedImageModel>
    private lateinit var imageAdapter: SelectedImageAdapter

    private lateinit var firebaseAuth: FirebaseAuth
    private var imageUri: Uri? = null
    private val maxImages = 3

    private var selectedLatitude: Double = 0.0
    private var selectedLongitude: Double = 0.0

    // Diálogo de progreso reutilizable
    private var progressDialog: AlertDialog? = null

    /*--------------------------------------------------*
     *   Helpers para mostrar / ocultar diálogo          *
     *--------------------------------------------------*/
    private fun showBlockingProgress(message: String) {
        if (progressDialog == null) {
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_progress, null)
            progressDialog = AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create()
        }
        progressDialog?.show()
        progressDialog
            ?.findViewById<TextView>(R.id.progress_text)
            ?.text = message
    }

    private fun hideBlockingProgress() {
        progressDialog?.dismiss()
    }

    /*--------------------------------------------------*
     *           Launchers y permisos                    *
     *--------------------------------------------------*/
    private val selectLocationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val address = data?.getStringExtra("address")
                selectedLatitude = data?.getDoubleExtra("latitude", 0.0) ?: 0.0
                selectedLongitude = data?.getDoubleExtra("longitude", 0.0) ?: 0.0
                binding.locationAutoCompleteTextView.setText(address ?: "Ubicación no seleccionada")
            }
        }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }

    /*--------------------------------------------------*
     *                  onCreate                         *
     *--------------------------------------------------*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Modo (crear | editar)
        mode = intent.getStringExtra("mode") ?: "create"
        adId = intent.getStringExtra("adId") ?: ""

        firebaseAuth = FirebaseAuth.getInstance()
        selectedImages = ArrayList()
        imageAdapter = SelectedImageAdapter(this, selectedImages)

        binding.selectedImagesRecyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.selectedImagesRecyclerView.adapter = imageAdapter

        setupDropdowns()

        if (mode == "edit" && adId.isNotEmpty()) {
            binding.publishButton.text = getString(R.string.save_changes)
            loadExistingAd(adId)
        }

        binding.locationAutoCompleteTextView.setOnClickListener {
            val intent = Intent(this, SelectLocation::class.java)
            selectLocationLauncher.launch(intent)
        }

        checkLocationPermission()

        binding.addImageView.setOnClickListener { showImagePickerDialog() }

        binding.publishButton.setOnClickListener {
            if (validateInputs()) {
                if (mode == "edit") updateAd(adId) else createAd()
            }
        }
    }

    /*--------------------------------------------------*
     *               MODO EDICIÓN                       *
     *--------------------------------------------------*/
    @SuppressLint("NotifyDataSetChanged")
    private fun loadExistingAd(id: String) {
        FirebaseDatabase.getInstance().getReference("Anuncios")
            .child(id)
            .get()
            .addOnSuccessListener { snap ->
                binding.brandEditText.setText(snap.child("brand").value.toString())
                binding.titleEditText.setText(snap.child("title").value.toString())
                binding.priceEditText.setText(snap.child("price").value.toString())
                binding.categoryAutoCompleteTextView.setText(
                    snap.child("category").value.toString(), false
                )
                binding.conditionAutoCompleteTextView.setText(
                    snap.child("condition").value.toString(), false
                )
                binding.locationAutoCompleteTextView.setText(
                    snap.child("location").value.toString(), false
                )
                binding.descriptionEditText.setText(snap.child("description").value.toString())

                selectedLatitude = snap.child("latitud").getValue(Double::class.java) ?: 0.0
                selectedLongitude = snap.child("longitud").getValue(Double::class.java) ?: 0.0

                snap.child("images").children.forEach { img ->
                    val url = img.child("imageUrl").getValue(String::class.java) ?: return@forEach
                    selectedImages.add(
                        SelectedImageModel(img.key ?: "", null, url, true)
                    )
                }
                imageAdapter.notifyDataSetChanged()
            }
    }

    private fun updateAd(id: String) {
        showBlockingProgress(getString(R.string.publishing_ad))

        val updates = mapOf(
            "brand" to binding.brandEditText.text.toString().trim(),
            "title" to binding.titleEditText.text.toString().trim(),
            "price" to binding.priceEditText.text.toString().trim(),
            "category" to binding.categoryAutoCompleteTextView.text.toString().trim(),
            "condition" to binding.conditionAutoCompleteTextView.text.toString().trim(),
            "location" to binding.locationAutoCompleteTextView.text.toString().trim(),
            "description" to binding.descriptionEditText.text.toString().trim(),
            "latitud" to selectedLatitude,
            "longitud" to selectedLongitude,
            "timestamp" to System.currentTimeMillis()
        )

        FirebaseDatabase.getInstance().getReference("Anuncios")
            .child(id)
            .updateChildren(updates)
            .addOnSuccessListener {
                if (selectedImages.any { !it.isFromInternet }) {
                    uploadImagesToStorage(id)
                } else {
                    hideBlockingProgress()
                    Toast.makeText(this, "Anuncio actualizado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                hideBlockingProgress()
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /*--------------------------------------------------*
     *               MODO CREACIÓN                       *
     *--------------------------------------------------*/
    private fun createAd() = uploadAdToFirebase()

    private fun setupDropdowns() {
        binding.categoryAutoCompleteTextView.setAdapter(
            ArrayAdapter(this, R.layout.item_category, CATEGORIES)
        )
        binding.conditionAutoCompleteTextView.setAdapter(
            ArrayAdapter(this, R.layout.item_condition, CONDITIONS)
        )
    }

    /*--------------------------------------------------*
     *      Selector de imágenes (cámara / galería)      *
     *--------------------------------------------------*/
    private fun showImagePickerDialog() {
        if (selectedImages.size >= maxImages) {
            Toast.makeText(this, "Máximo de $maxImages imágenes alcanzado", Toast.LENGTH_SHORT)
                .show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Seleccionar imagen")
            .setItems(arrayOf("Cámara", "Galería")) { _, which ->
                if (which == 0) openCamera() else openGallery()
            }
            .show()
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
            if (uri != null) addImageToRecyclerView(uri)
            else Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
        }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            return
        }
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "Nueva Imagen")
            put(MediaStore.Images.Media.DESCRIPTION, "Desde la cámara")
        }
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        }
        cameraLauncher.launch(cameraIntent)
    }

    private fun openGallery() = galleryLauncher.launch("image/*")

    private fun addImageToRecyclerView(uri: Uri) {
        selectedImages.add(
            SelectedImageModel(
                id = System.currentTimeMillis().toString(),
                imageUri = uri,
                imageUrl = null,
                isFromInternet = false
            )
        )
        imageAdapter.notifyItemInserted(selectedImages.size - 1)
    }

    /*--------------------------------------------------*
     *          Validación + subida a Firebase           *
     *--------------------------------------------------*/
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
        if (brand.isEmpty() || category.isEmpty() || condition.isEmpty() || location.isEmpty()
            || price.isEmpty() || description.isEmpty() || title.isEmpty()
        ) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun uploadAdToFirebase() {
        showBlockingProgress("Subiendo anuncio…")

        val databaseReference = FirebaseDatabase.getInstance().getReference("Anuncios")
        val adId = databaseReference.push().key ?: return

        val adData = mapOf(
            "id" to adId,
            "brand" to binding.brandEditText.text.toString().trim(),
            "category" to binding.categoryAutoCompleteTextView.text.toString().trim(),
            "condition" to binding.conditionAutoCompleteTextView.text.toString().trim(),
            "location" to binding.locationAutoCompleteTextView.text.toString().trim(),
            "price" to binding.priceEditText.text.toString().trim(),
            "title" to binding.titleEditText.text.toString().trim(),
            "description" to binding.descriptionEditText.text.toString().trim(),
            "userId" to firebaseAuth.uid,
            "latitud" to selectedLatitude,
            "longitud" to selectedLongitude,
            "timestamp" to System.currentTimeMillis()
        )

        databaseReference.child(adId).setValue(adData)
            .addOnSuccessListener { uploadImagesToStorage(adId) }
            .addOnFailureListener { e ->
                hideBlockingProgress()
                Toast.makeText(this, "Error al subir el anuncio: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun uploadImagesToStorage(adId: String) {
        val storageReference = FirebaseStorage.getInstance().reference.child("AdImages")
        var uploadedCount = 0
        val totalToUpload = selectedImages.count { !it.isFromInternet && it.imageUri != null }

        // Si no hay nuevas imágenes, terminamos
        if (totalToUpload == 0) {
            hideBlockingProgress()
            Toast.makeText(
                this,
                if (mode == "edit") "Anuncio actualizado" else "Anuncio subido",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        for (image in selectedImages) {
            if (image.isFromInternet || image.imageUri == null) continue

            val imageRef = storageReference.child("$adId/${image.id}.jpg")
            imageRef.putFile(image.imageUri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageData = mapOf("imageUrl" to uri.toString())

                        FirebaseDatabase.getInstance().getReference("Anuncios")
                            .child(adId).child("images").child(image.id)
                            .setValue(imageData)
                            .addOnCompleteListener {
                                uploadedCount++
                                if (uploadedCount == totalToUpload) {
                                    hideBlockingProgress()
                                    Toast.makeText(
                                        this,
                                        if (mode == "edit") "Anuncio actualizado"
                                        else "Anuncio subido exitosamente",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                }
                            }
                    }
                }
                .addOnFailureListener { e ->
                    hideBlockingProgress()
                    Toast.makeText(
                        this,
                        "Error al subir imágenes: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    /*--------------------------------------------------*
     *        Permiso de localización (GPS)              *
     *--------------------------------------------------*/
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }
}










