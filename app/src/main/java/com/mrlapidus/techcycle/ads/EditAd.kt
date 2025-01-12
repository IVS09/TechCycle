package com.mrlapidus.techcycle.ads

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mrlapidus.techcycle.R
import com.mrlapidus.techcycle.Utilities.CATEGORIES
import com.mrlapidus.techcycle.Utilities.CONDITIONS
import com.mrlapidus.techcycle.adapter.SelectedImageAdapter
import com.mrlapidus.techcycle.databinding.ActivityEditAdBinding
import com.mrlapidus.techcycle.model.SelectedImageModel

class EditAd : AppCompatActivity() {

    private lateinit var binding: ActivityEditAdBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var customProgressDialog: AlertDialog

    private lateinit var selectedImages: ArrayList<SelectedImageModel>
    private lateinit var imageAdapter: SelectedImageAdapter

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialización de ViewBinding
        binding = ActivityEditAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar adaptador para categorías
        val categoryAdapter = ArrayAdapter(
            this,
            R.layout.item_category, // Archivo XML para las categorías
            CATEGORIES // Lista de categorías
        )
        binding.categoryAutoCompleteTextView.setAdapter(categoryAdapter)

        // Configurar adaptador para condiciones
        val conditionAdapter = ArrayAdapter(
            this,
            R.layout.item_condition, // Archivo XML para las condiciones
            CONDITIONS // Lista de condiciones
        )
        binding.conditionAutoCompleteTextView.setAdapter(conditionAdapter)

        // Inicializar FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Configura el ProgressDialog personalizado
        customProgressDialog = AlertDialog.Builder(this)
            .setView(layoutInflater.inflate(R.layout.progress_dialog, binding.root, false))
            .setCancelable(false)
            .create()

        // Inicializo la lista de imágenes seleccionadas
        selectedImages = ArrayList()

        // Inicializo el adaptador
        imageAdapter = SelectedImageAdapter(this, selectedImages)

        // Llama a setupCustomProgressDialog
        setupCustomProgressDialog()

        showProgressDialog()

        android.os.Handler().postDelayed({
            dismissProgressDialog()
        }, 3000)
    }

    // Configuración del diálogo de progreso personalizado
    private fun setupCustomProgressDialog() {
        val dialogView = layoutInflater.inflate(R.layout.progress_dialog, null)
        customProgressDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // No cerrar el diálogo al tocar fuera
            .create()
    }

    // Función para mostrar el diálogo de progreso
    private fun showProgressDialog() {
        if (!customProgressDialog.isShowing) {
            customProgressDialog.show()
        }
    }

    // Función para ocultar el diálogo de progreso
    private fun dismissProgressDialog() {
        if (customProgressDialog.isShowing) {
            customProgressDialog.dismiss()
        }
    }
}








