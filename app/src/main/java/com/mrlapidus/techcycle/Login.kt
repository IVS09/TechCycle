package com.mrlapidus.techcycle

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.mrlapidus.techcycle.databinding.ActivityLoginBinding

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val progressDialogView = layoutInflater.inflate(R.layout.progress_dialog, binding.root, false)
        progressDialog = AlertDialog.Builder(this)
            .setTitle("Espere por favor")
            .setView(progressDialogView)
            .setCancelable(false)
            .create()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.registerTextView.setOnClickListener {
            val intent = Intent(this@Login, Registro::class.java)
            startActivity(intent)
        }

        binding.forgotPasswordTextView.setOnClickListener {
            val intent = Intent(this@Login, Recuperacion::class.java)
            startActivity(intent)
        }

        binding.loginButton.setOnClickListener {
            validarInfo()
        }
    }

    private fun validarInfo() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.error = "Correo electrónico inválido"
            binding.emailEditText.requestFocus()
        } else if (email.isEmpty()) {
            binding.emailEditText.error = "Ingrese correo electrónico"
            binding.emailEditText.requestFocus()
        } else if (password.isEmpty()) {
            binding.passwordEditText.error = "Ingrese contraseña"
            binding.passwordEditText.requestFocus()
        } else {
            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        progressDialog.setMessage("Iniciando sesión")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    // Inicio de sesión exitoso, navegar a la actividad principal
                    val intent = Intent(this@Login, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // Si el inicio de sesión falla, mostrar un mensaje al usuario.
                    Toast.makeText(this, "Error de autenticación: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}