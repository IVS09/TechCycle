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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.mrlapidus.techcycle.databinding.ActivityEditProfileBinding

class EditProfile : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private var imageUri: Uri? = null

    // Solicitar permisos en tiempo de ejecución
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[android.Manifest.permission.CAMERA] ?: false
        val storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[android.Manifest.permission.READ_MEDIA_IMAGES] ?: false
        } else {
            permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
        }

        if (cameraGranted && storageGranted) {
            Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permisos denegados. No se podrá acceder a la cámara o galería", Toast.LENGTH_SHORT).show()
        }
    }

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

        // Verificar y solicitar permisos
        requestPermissions()

        // Manejo de los márgenes del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Cargar los datos del usuario al abrir la pantalla
        loadUserData()

        // Configurar el botón flotante para mostrar el menú
        binding.changeImageButton.setOnClickListener {
            showImageSelectionMenu()
        }

        // Configurar el botón de guardar cambios
        binding.updateButton.setOnClickListener {
            saveUserData()
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionsLauncher.launch(
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                )
            )
        } else {
            requestPermissionsLauncher.launch(
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        }
    }

    private fun loadUserData() {
        val userId = FirebaseAuth.getInstance().uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(userId)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nombre = snapshot.child("nombreCompleto").value.toString()
                val email = snapshot.child("correo").value.toString()
                val telefono = snapshot.child("telefono").value.toString()
                val imageUrl = snapshot.child("urlAvatar").value.toString()

                // Actualizar los campos del diseño
                binding.fullNameEditText.setText(nombre)
                binding.emailEditText.setText(email) // Solo lectura
                binding.phoneEditText.setText(telefono)

                // Cargar la imagen con Glide
                Glide.with(this@EditProfile)
                    .load(imageUrl.ifEmpty { R.drawable.avatar_profile }) // Placeholder si no hay imagen
                    .circleCrop()
                    .into(binding.editProfileImageView)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditProfile, "Error al cargar datos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveUserData() {
        val userId = FirebaseAuth.getInstance().uid ?: return

        val nombre = binding.fullNameEditText.text.toString().trim()
        val telefono = binding.phoneEditText.text.toString().trim()

        if (nombre.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val databaseRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(userId)
        val updates = mapOf(
            "nombreCompleto" to nombre,
            "telefono" to telefono
        )

        databaseRef.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error al guardar datos: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showImageSelectionMenu() {
        val options = arrayOf("Usar Cámara", "Seleccionar de Galería")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Seleccionar una opción")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> launchCamera()
                1 -> handleImageSelection()
            }
        }
        builder.show()
    }

    private fun handleImageSelection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
            val intent = Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, 1)
            }
            photoPickerLauncher.launch(intent)
        } else {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            galleryLauncher.launch(intent)
        }
    }

    private fun updateProfileImage() {
        if (imageUri != null) {
            Glide.with(this)
                .load(imageUri)
                .circleCrop()
                .into(binding.editProfileImageView)
            uploadProfileImage()
        } else {
            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadProfileImage() {
        if (imageUri == null) return

        val userId = FirebaseAuth.getInstance().uid ?: return
        val storageRef = FirebaseStorage.getInstance().getReference("imagenesPerfil/$userId.jpg")

        storageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveImageUrlToDatabase(uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al subir imagen: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveImageUrlToDatabase(imageUrl: String) {
        val userId = FirebaseAuth.getInstance().uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(userId)

        databaseRef.child("urlAvatar").setValue(imageUrl).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Imagen actualizada en la base de datos", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error al guardar la imagen: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

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

