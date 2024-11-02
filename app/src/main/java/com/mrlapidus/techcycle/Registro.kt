package com.mrlapidus.techcycle

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.mrlapidus.techcycle.databinding.ActivityRegistroBinding
import com.mrlapidus.techcycle.databinding.ProgressDialogBinding

class Registro : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var loadingDialog: Dialog

    private var userEmail = ""
    private var userPassword = ""
    private var confirmPassword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar FirebaseAuth y el Dialog de carga
        firebaseAuth = FirebaseAuth.getInstance()
        setupLoadingDialog()

        // Configuración para el botón de registro
        binding.registerButton.setOnClickListener {
            verifyInputData()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupLoadingDialog() {
        // Configuración del Dialog personalizado para el progreso
        loadingDialog = Dialog(this)
        val dialogBinding = ProgressDialogBinding.inflate(LayoutInflater.from(this))
        loadingDialog.setContentView(dialogBinding.root)
        loadingDialog.setCancelable(false) // No se puede cancelar al tocar fuera
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Fondo transparente
    }

    private fun verifyInputData() {
        userEmail = binding.emailEditText.text.toString().trim()
        userPassword = binding.passwordEditText.text.toString().trim()
        confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

        when {
            !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches() -> {
                binding.emailEditText.error = "Por favor, ingrese un correo electrónico válido"
                binding.emailEditText.requestFocus()
            }
            userEmail.isEmpty() -> {
                binding.emailEditText.error = "El campo de correo electrónico no puede estar vacío"
                binding.emailEditText.requestFocus()
            }
            userPassword.isEmpty() -> {
                binding.passwordEditText.error = "El campo de contraseña no puede estar vacío"
                binding.passwordEditText.requestFocus()
            }
            confirmPassword.isEmpty() -> {
                binding.confirmPasswordEditText.error = "Por favor, confirme su contraseña"
                binding.confirmPasswordEditText.requestFocus()
            }
            userPassword != confirmPassword -> {
                binding.confirmPasswordEditText.error = "Las contraseñas no coinciden"
                binding.confirmPasswordEditText.requestFocus()
            }
            else -> {
                createAccount()
            }
        }
    }

    private fun createAccount() {
        // Mostrar el diálogo de carga
        loadingDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
            .addOnSuccessListener {
                saveUserInfoToDatabase()
            }
            .addOnFailureListener { e ->
                loadingDialog.dismiss()
                Toast.makeText(
                    this,
                    "No se ha podido crear la cuenta: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun saveUserInfoToDatabase() {
        val currentTime = System.currentTimeMillis()
        val email = firebaseAuth.currentUser?.email ?: ""
        val userId = firebaseAuth.uid ?: ""

        val userMap = hashMapOf(
            "nombreCompleto" to "",
            "codigoPais" to "",
            "telefono" to "",
            "urlAvatar" to "",
            "métodoDeRegistro" to "Email",
            "estadoUsuario" to "activo",
            "correo" to email,
            "idUsuario" to userId,
            "fechaDeRegistro" to currentTime,
            "escribiendo" to "",
            "tiempo" to currentTime,  // Puedes reemplazar currentTime por una función similar a `Constantes.obtenerTiempoDis()`
            "fecha_nac" to ""  // Campo para la fecha de nacimiento
        )

        val databaseRef = FirebaseDatabase.getInstance().getReference("Usuarios")
        databaseRef.child(userId)
            .setValue(userMap)
            .addOnSuccessListener {
                loadingDialog.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                loadingDialog.dismiss()
                Toast.makeText(
                    this,
                    "No se guardó la información: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
