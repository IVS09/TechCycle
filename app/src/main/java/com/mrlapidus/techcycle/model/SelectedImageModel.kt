package com.mrlapidus.techcycle.model

import android.net.Uri

class SelectedImageModel {

    var id: String = ""
    var imageUri: Uri? = null
    var imageUrl: String? = null
    var isFromInternet: Boolean = false

    // Constructor vacío requerido por Firebase
    constructor()

    // Constructor con parámetros
    constructor(id: String, imageUri: Uri?, imageUrl: String?, isFromInternet: Boolean) {
        this.id = id
        this.imageUri = imageUri
        this.imageUrl = imageUrl
        this.isFromInternet = isFromInternet
    }
}
