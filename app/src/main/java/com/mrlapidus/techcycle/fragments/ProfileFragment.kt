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
import com.google.firebase.database.*
import com.mrlapidus.techcycle.Login
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

        // Cargar datos del usuario
        binding.progressBar.visibility = View.VISIBLE
        loadUserInfo()

        // Botón Editar Perfil
        binding.editProfileButton.setOnClickListener {
            val intent = Intent(requireContext(), EditProfile::class.java)
            startActivity(intent)
        }

        // Botón Cerrar Sesión
        binding.logoutButton.setOnClickListener {
            firebaseAuth.signOut()
            Toast.makeText(requireContext(), getString(R.string.session_closed), Toast.LENGTH_SHORT).show()

            val intent = Intent(requireContext(), Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid ?: return)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("nombreCompleto").value as? String ?: "N/A"
                    val email = snapshot.child("correo").value as? String ?: "N/A"
                    val profileImage = snapshot.child("urlAvatar").value as? String ?: ""
                    val registrationDate = snapshot.child("fechaDeRegistro").value as? Long ?: 0L
                    val provider = snapshot.child("métodoDeRegistro").value as? String ?: "N/A"

                    binding.nameValueTextView.text = name
                    binding.emailValueTextView.text = email
                    binding.memberSinceValueTextView.text =
                        Utilities.formatTimestampToDate(registrationDate)

                    Glide.with(requireContext())
                        .load(profileImage)
                        .placeholder(R.drawable.avatar_profile)
                        .into(binding.profileImageView)

                    binding.progressBar.visibility = View.GONE

                    if (provider == getString(R.string.login_provider_email)) {
                        val isVerified = firebaseAuth.currentUser?.isEmailVerified ?: false
                        binding.accountStatusValueTextView.text = if (isVerified)
                            getString(R.string.account_verified) else getString(R.string.account_not_verified)

                        binding.accountStatusValueTextView.setTextColor(
                            requireContext().getColor(
                                if (isVerified) R.color.primaryColor else R.color.red
                            )
                        )
                    } else if (provider == getString(R.string.login_provider_google)) {
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
                    binding.progressBar.visibility = View.GONE
                }
            })
    }
}

