package com.mrlapidus.techcycle

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.mrlapidus.techcycle.databinding.ActivityRegistroBinding

class Registro : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var userEmail = ""
    private var userPassword = ""
    private var confirmPassword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //inicio FirebaseAuth y ProgressDialog
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this).apply {
            setTitle("Espere por favor")
            setMessage("Creando cuenta...")
            setCanceledOnTouchOutside(false)
        }

        //setting para el botón de registro
        binding.registerButton.setOnClickListener{
            verifyInputData()
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun verifyInputData() {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }
}