package com.barefi0012.asesmen2.model

import com.squareup.moshi.Json

data class RemoteMedication(
    val id: String? = null,
    @param:Json(name = "nama") val name: String,
    @param:Json(name = "detail") val details: String,
    val imageId: String? = null
)
