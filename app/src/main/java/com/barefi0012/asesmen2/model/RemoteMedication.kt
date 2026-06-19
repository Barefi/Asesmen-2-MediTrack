package com.barefi0012.asesmen2.model

import com.squareup.moshi.Json

data class RemoteMedication(
    @Json(name = "nama") val name: String,
    @Json(name = "namaLatin") val details: String,
    val imageId: String
)
