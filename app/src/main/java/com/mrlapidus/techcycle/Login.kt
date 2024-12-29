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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.mrlapidus.techcycle.databinding.ActivityLoginBinding

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: AlertDialog
    private lateinit var googleSignInClient: GoogleSignInClient

    private val RC_SIGN_IN = 1001 // Código para identificar la actividad de Google Sign-In

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

        // Configurar Google Sign-In
        setupGoogleSignIn()

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

        binding.googleLoginButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun setupGoogleSignIn() {
        // Configuración de Google Sign-In con ID de cliente obtenido del archivo `google-services.json`
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // ID del cliente de Google
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this, "Error de autenticación con Google: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    checkAndSaveUserInfo(account)
                } else {
                    Toast.makeText(this, "Error de autenticación: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkAndSaveUserInfo(account: GoogleSignInAccount?) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("Usuarios")

        databaseRef.child(userId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // El usuario ya existe
                Toast.makeText(this, "Bienvenido de nuevo, ${account?.displayName ?: "Usuario"}", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            } else {
                // El usuario no existe, guardar los datos
                saveUserInfoToDatabase(account)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al verificar los datos del usuario: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserInfoToDatabase(account: GoogleSignInAccount?) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val currentTime = System.currentTimeMillis()

        val userMap = hashMapOf(
            "nombreCompleto" to (account?.displayName ?: "Usuario Desconocido"),
            "codigoPais" to "",
            "telefono" to "",
            "urlAvatar" to "",
            "métodoDeRegistro" to "Google",
            "estadoUsuario" to "activo",
            "correo" to (account?.email ?: ""),
            "idUsuario" to userId,
            "fechaDeRegistro" to currentTime,
            "escribiendo" to "",
            "tiempo" to currentTime,
            "fecha_nac" to ""
        )

        val databaseRef = FirebaseDatabase.getInstance().getReference("Usuarios")
        databaseRef.child(userId).setValue(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Usuario guardado exitosamente en la base de datos.", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar datos en la base de datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@Login, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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
                    navigateToMainActivity()
                } else {
                    Toast.makeText(this, "Error de autenticación: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}