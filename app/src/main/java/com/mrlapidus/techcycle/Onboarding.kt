package com.mrlapidus.techcycle

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
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
import com.mrlapidus.techcycle.databinding.ActivityOnboardingBinding

class Onboarding : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private val auth by lazy { FirebaseAuth.getInstance() }

    /* ---------- resultado del flujo One-Tap ---------- */
    private val googleLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                credential.googleIdToken?.let { idToken ->
                    val firebaseCred = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCred)
                        .addOnSuccessListener { goToMain() }
                        .addOnFailureListener {
                            // Si algo falla, abre el login tradicional
                            startActivity(Intent(this, Login::class.java))
                        }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ajuste de insets (barra de estado / navegación)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        /* ---------- Google One-Tap ---------- */
        setupGoogle()

        /* ---------- Botones ---------- */
        binding.buttonEmailLogin.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }

        binding.buttonGmailLogin.setOnClickListener {
            startGoogleSignIn()      // ahora lanza One-Tap directamente
        }

        binding.textRegister.setOnClickListener {
            startActivity(Intent(this, Registro::class.java))
        }
    }

    /* ---------------- helpers ---------------- */
    private fun setupGoogle() {
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.Builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)          // muestra todas tus cuentas
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .build()
            )
            .build()
    }

    private fun startGoogleSignIn() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                googleLauncher.launch(
                    IntentSenderRequest.Builder(result.pendingIntent).build()
                )
            }
            .addOnFailureListener {
                // No se pudo iniciar One-Tap → abre Login para alternativas
                startActivity(Intent(this, Login::class.java))
            }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()   // evita volver al onboarding
    }
}

