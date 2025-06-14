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
import com.mrlapidus.techcycle.databinding.ActivityOnboardingBinding

class Onboarding : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private val auth by lazy { FirebaseAuth.getInstance() }

    // ─── recibe resultado de One-Tap ────────────────────────────────
    private val googleLauncher =
        registerForActivityResult(StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val cred = oneTapClient.getSignInCredentialFromIntent(result.data)
                cred.googleIdToken?.let { idToken ->
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(credential)
                        .addOnSuccessListener { goToMain() }
                        .addOnFailureListener { /*   ==> si falla, abre Login   */
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

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Google One-Tap
        setupGoogle()

        // ─── Botón Email ─────────────────────────────────────────────
        binding.buttonEmailLogin.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }

        // ─── Botón Google (nuevo flujo) ──────────────────────────────
        binding.buttonGmailLogin.setOnClickListener {
            if (auth.currentUser != null) {
                goToMain()                         // ya logeado
            } else {
                startGoogleSignIn()                // One-Tap
            }
        }

        // Registro
        binding.textRegister.setOnClickListener {
            startActivity(Intent(this, Registro::class.java))
        }
    }

    // ───────────────────────── helpers ──────────────────────────────
    private fun setupGoogle() {
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
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
                // Si falla, envía al Login tradicional
                startActivity(Intent(this, Login::class.java))
            }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

