package com.mrlapidus.techcycle

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.mrlapidus.techcycle.databinding.ActivityLoginBinding
import com.mrlapidus.techcycle.Utilities.EXTRA_AUTO_GOOGLE

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: AlertDialog
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        setupGoogleSignIn()
        checkAutoGoogle()

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
            initiateGoogleSignIn()
        }
    }

    @Suppress("DEPRECATION")
    private fun setupGoogleSignIn() {
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()
    }



    private fun initiateGoogleSignIn() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                val intent = IntentSenderRequest.Builder(result.pendingIntent).build()
                googleSignInLauncher.launch(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al iniciar sesión: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken)
                } else {
                    Toast.makeText(this, "No se pudo obtener el token de Google", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error en la autenticación: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    checkAndSaveUserInfo()
                } else {
                    Toast.makeText(this, "Error de autenticación: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkAndSaveUserInfo() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("Usuarios")

        databaseRef.child(userId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                Toast.makeText(this, "Bienvenido de nuevo, ${firebaseAuth.currentUser?.displayName ?: "Usuario"}", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            } else {
                saveUserInfoToDatabase()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al verificar los datos del usuario: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserInfoToDatabase() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val currentTime = System.currentTimeMillis()

        val userMap = hashMapOf(
            "nombreCompleto" to (firebaseAuth.currentUser?.displayName ?: "Usuario Desconocido"),
            "codigoPais" to "",
            "telefono" to "",
            "urlAvatar" to "",
            "métodoDeRegistro" to "Google",
            "estadoUsuario" to "activo",
            "correo" to (firebaseAuth.currentUser?.email ?: ""),
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

    private fun checkAutoGoogle() {
        if (intent.getBooleanExtra(EXTRA_AUTO_GOOGLE, false)) {
            initiateGoogleSignIn()
        }
    }

}
