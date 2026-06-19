package com.barefi0012.asesmen2.network

import com.barefi0012.asesmen2.BuildConfig
import com.barefi0012.asesmen2.model.OperationStatus
import com.barefi0012.asesmen2.model.RemoteMedication
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

private val baseUrl = BuildConfig.API_BASE_URL.let {
    if (it.endsWith("/")) it else "$it/"
}

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(baseUrl)
    .build()

interface RemoteMedicationApiService {
    @GET("obat")
    suspend fun getMedications(
        @Header("Authorization") userId: String
    ): List<RemoteMedication>

    @Multipart
    @POST("obat")
    suspend fun postMedication(
        @Header("Authorization") userId: String,
        @Part("nama") name: RequestBody,
        @Part("detail") details: RequestBody,
        @Part image: MultipartBody.Part
    ): OperationStatus

    @DELETE("obat")
    suspend fun deleteMedication(
        @Header("Authorization") userId: String,
        @Query("id") id: String
    ): OperationStatus
}

object RemoteMedicationApi {
    val service: RemoteMedicationApiService by lazy {
        retrofit.create(RemoteMedicationApiService::class.java)
    }

    fun imageUrl(imageId: String): String {
        return "${baseUrl}images/$imageId"
    }
}

enum class ApiStatus {
    IDLE,
    LOADING,
    SUCCESS,
    FAILED
}
