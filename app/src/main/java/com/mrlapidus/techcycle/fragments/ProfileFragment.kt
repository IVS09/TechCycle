package com.mrlapidus.techcycle.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mrlapidus.techcycle.R
import com.mrlapidus.techcycle.Utilities
import com.mrlapidus.techcycle.databinding.FragmentProfileBinding
import com.mrlapidus.techcycle.EditProfile

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        // Mostrar barra de carga y cargar información del usuario
        binding.progressBar.visibility = View.VISIBLE
        loadUserInfo()

        // Lógica del botón para editar el perfil
        binding.editProfileButton.setOnClickListener {
            // Navegar a la actividad EditProfile
            val intent = Intent(requireContext(), EditProfile::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserInfo() {
        // Mostrar el ProgressBar al inicio de la carga
        binding.progressBar.visibility = View.VISIBLE

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid ?: return)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("nombreCompleto").value as? String ?: "N/A"
                    val email = snapshot.child("correo").value as? String ?: "N/A"
                    val profileImage = snapshot.child("urlAvatar").value as? String ?: ""
                    val registrationDate = snapshot.child("fechaDeRegistro").value as? Long ?: 0L
                    val provider = snapshot.child("métodoDeRegistro").value as? String ?: "N/A"

                    // Actualizar la interfaz con datos del usuario
                    binding.nameValueTextView.text = name
                    binding.emailValueTextView.text = email
                    binding.memberSinceValueTextView.text =
                        Utilities.formatTimestampToDate(registrationDate)

                    // Cargar imagen de perfil
                    Glide.with(requireContext())
                        .load(profileImage)
                        .placeholder(R.drawable.avatar_profile)
                        .into(binding.profileImageView)

                    // Ocultar el ProgressBar después de cargar los datos
                    binding.progressBar.visibility = View.GONE

                    // Verificar estado de la cuenta
                    if (provider == getString(R.string.login_provider_email)) {
                        val isVerified = firebaseAuth.currentUser?.isEmailVerified ?: false
                        if (isVerified) {
                            // Usuario verificado
                            binding.accountStatusValueTextView.text = getString(R.string.account_verified)
                            binding.accountStatusValueTextView.setTextColor(
                                requireContext().getColor(R.color.primaryColor)
                            )
                        } else {
                            // Usuario no verificado
                            binding.accountStatusValueTextView.text = getString(R.string.account_not_verified)
                            binding.accountStatusValueTextView.setTextColor(
                                requireContext().getColor(R.color.red)
                            )
                        }
                    } else if (provider == getString(R.string.login_provider_google)) {
                        // Usuario con cuenta de Google siempre está verificado
                        binding.accountStatusValueTextView.text = getString(R.string.account_verified)
                        binding.accountStatusValueTextView.setTextColor(
                            requireContext().getColor(R.color.primaryColor)
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_loading_data, error.message),
                        Toast.LENGTH_SHORT
                    ).show()

                    // También ocultar el ProgressBar en caso de error
                    binding.progressBar.visibility = View.GONE
                }
            })
    }
}
