package com.mrlapidus.techcycle

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.mrlapidus.techcycle.databinding.ActivityEditProfileBinding

class EditProfile : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private var imageUri: Uri? = null

    // Registrar el resultado de PhotoPicker para Android 14+
    private val photoPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            imageUri = result.data?.data
            updateProfileImage()
        } else {
            Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
        }
    }

    // Registrar el resultado para la galería (versiones anteriores)
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            imageUri = result.data?.data
            updateProfileImage()
        } else {
            Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
        }
    }

    // Registrar el resultado de la cámara
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            updateProfileImage()
        } else {
            Toast.makeText(this, "No se capturó ninguna imagen", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Manejo de los márgenes del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Cargar la imagen de perfil inicial
        loadProfileImage()

        // Configurar el botón flotante para mostrar el menú
        binding.changeImageButton.setOnClickListener {
            showImageSelectionMenu()
        }
    }

    // Método para cargar una imagen de perfil
    private fun loadProfileImage() {
        Glide.with(this)
            .load(imageUri ?: R.drawable.avatar_profile) // Usa un placeholder si no hay imagen
            .circleCrop()
            .into(binding.editProfileImageView)
    }

    // Método para mostrar el menú (Cámara o Galería)
    private fun showImageSelectionMenu() {
        val options = arrayOf("Usar Cámara", "Seleccionar de Galería")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Seleccionar una opción")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> launchCamera() // Opción Cámara
                1 -> handleImageSelection() // Opción Galería
            }
        }
        builder.show()
    }

    // Manejar la selección de imágenes (Galería o PhotoPicker)
    private fun handleImageSelection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
            val intent = Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, 1) // Permitir solo una imagen
            }
            photoPickerLauncher.launch(intent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13
            val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
            photoPickerLauncher.launch(intent)
        } else {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            galleryLauncher.launch(intent)
        }
    }

    // Actualizar la imagen de perfil seleccionada
    private fun updateProfileImage() {
        if (imageUri != null) {
            Glide.with(this)
                .load(imageUri)
                .circleCrop()
                .into(binding.editProfileImageView)
        } else {
            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    // Abrir la cámara y capturar una imagen
    private fun launchCamera() {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "Nueva Imagen")
            put(MediaStore.Images.Media.DESCRIPTION, "Desde la cámara")
        }
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(cameraIntent)
    }
}


