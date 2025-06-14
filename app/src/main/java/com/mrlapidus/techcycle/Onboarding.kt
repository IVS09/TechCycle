package com.mrlapidus.techcycle

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.mrlapidus.techcycle.databinding.ActivityOnboardingBinding

class Onboarding : AppCompatActivity() {

    // ────────────────────────────────────
    //  view-binding & Firebase
    // ────────────────────────────────────
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var firebaseAuth: FirebaseAuth

    // Google One-Tap
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    // ────────────────────────────────────
    //  Google result launcher
    // ────────────────────────────────────
    private val googleLauncher = registerForActivityResult(StartIntentSenderForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken)
                }
            } catch (e: Exception) {
                // Aquí podrías mostrar un toast si lo deseas
            }
        }
    }

    // ────────────────────────────────────
    //  onCreate
    // ────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        setupGoogleSignIn()

        // Ajuste de insets (barra de estado/navegación)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // ─── Botones ────────────────────────────────────────────────
        binding.buttonEmailLogin.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }

        binding.buttonGmailLogin.setOnClickListener {
            initiateGoogleSignIn()
        }

        binding.textRegister.setOnClickListener {
            startActivity(Intent(this, Registro::class.java))
        }
    }

    // ────────────────────────────────────
    //  Google One-Tap helpers
    // ────────────────────────────────────
    private fun setupGoogleSignIn() {
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            ).build()
    }

    private fun initiateGoogleSignIn() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                val intent = IntentSenderRequest.Builder(result.pendingIntent).build()
                googleLauncher.launch(intent)
            }
            .addOnFailureListener {
                // Si falla, abre Login para que lo intente allí
                startActivity(Intent(this, Login::class.java))
            }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                val uid = firebaseAuth.currentUser?.uid ?: return@addOnSuccessListener
                checkAndCreateUser(uid)
            }
            .addOnFailureListener {
                // Si falla, abre Login para intentar de nuevo con Email
                startActivity(Intent(this, Login::class.java))
            }
    }

    // ────────────────────────────────────
    //  Crear usuario la primera vez
    // ────────────────────────────────────
    private fun checkAndCreateUser(uid: String) {
        val db = FirebaseDatabase.getInstance().getReference("Usuarios")
        db.child(uid).get().addOnSuccessListener { snap ->
            if (!snap.exists()) {
                val now = System.currentTimeMillis()
                val user = mapOf(
                    "nombreCompleto"    to (firebaseAuth.currentUser?.displayName ?: ""),
                    "correo"            to (firebaseAuth.currentUser?.email ?: ""),
                    "urlAvatar"         to (firebaseAuth.currentUser?.photoUrl?.toString() ?: ""),
                    "métodoDeRegistro"  to "Google",
                    "estadoUsuario"     to "activo",
                    "idUsuario"         to uid,
                    "fechaDeRegistro"   to now,
                    "tiempo"            to now
                )
                db.child(uid).setValue(user)
            }
            goToMain()
        }.addOnFailureListener {
            goToMain()
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}


