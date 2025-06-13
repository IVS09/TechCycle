// app/src/main/java/com/mrlapidus/techcycle/Recuperacion.kt
package com.mrlapidus.techcycle

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mrlapidus.techcycle.databinding.ActivityRecuperacionBinding

class Recuperacion : AppCompatActivity() {

    private lateinit var binding: ActivityRecuperacionBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecuperacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.recoverButton.setOnClickListener { sendResetEmail() }
    }

    private fun sendResetEmail() {
        val email = binding.emailEditText.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = getString(R.string.email_hint)
            binding.emailEditText.requestFocus()
            return
        }

        // Pequeña UX → deshabilitar botón mientras se hace la petición
        binding.recoverButton.isEnabled = false

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                binding.recoverButton.isEnabled = true
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        getString(R.string.recover_password) + ": " + getString(R.string.email_sent),
                        Toast.LENGTH_LONG
                    ).show()
                    finish() // volvemos atrás
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.localizedMessage ?: "Error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
