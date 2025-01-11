package com.mrlapidus.techcycle.model

import android.net.Uri

data class SelectedImageModel(
    val id: String,
    val imageUri: Uri? = null,
    val imageUrl: String? = null,
    val isFromInternet: Boolean = false
)