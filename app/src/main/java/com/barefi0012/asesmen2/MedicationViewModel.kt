package com.barefi0012.asesmen2

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.barefi0012.asesmen2.data.AppDatabase
import com.barefi0012.asesmen2.data.Medication
import com.barefi0012.asesmen2.data.ThemePreferences
import com.barefi0012.asesmen2.model.RemoteMedication
import com.barefi0012.asesmen2.network.ApiStatus
import com.barefi0012.asesmen2.network.RemoteMedicationApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.min

class MedicationViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val GUEST_OWNER = "__guest__"
    }

    private val dao = AppDatabase.getDatabase(application).medicationDao()
    private val themePrefs = ThemePreferences(application)

    val isDarkMode: StateFlow<Boolean> = themePrefs.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _remoteMedications = MutableStateFlow<List<RemoteMedication>>(emptyList())
    val remoteMedications: StateFlow<List<RemoteMedication>> = _remoteMedications

    private val _apiStatus = MutableStateFlow(ApiStatus.IDLE)
    val apiStatus: StateFlow<ApiStatus> = _apiStatus

    private val _apiMessage = MutableStateFlow<String?>(null)
    val apiMessage: StateFlow<String?> = _apiMessage


    fun validateInput(name: String, dosage: String, time: String): String? {
        if (name.trim().isEmpty()) return "Nama obat tidak boleh kosong!"
        if (dosage.trim().isEmpty()) return "Dosis obat tidak boleh kosong!"
        if (time.trim().isEmpty()) return "Waktu konsumsi tidak boleh kosong!"
        return null
    }

    fun getMedications(ownerEmail: String): Flow<List<Medication>> {
        return if (ownerEmail.isBlank()) {
            flowOf(emptyList())
        } else {
            dao.getAllActiveMedications(ownerEmail)
        }
    }

    fun getDeletedMedications(ownerEmail: String): Flow<List<Medication>> {
        return if (ownerEmail.isBlank()) {
            flowOf(emptyList())
        } else {
            dao.getDeletedMedications(ownerEmail)
        }
    }

    fun insert(
        ownerEmail: String,
        name: String,
        dosage: String,
        time: String,
        photoPath: String?
    ) = viewModelScope.launch {
        dao.insertMedication(
            Medication(
                ownerEmail = ownerEmail,
                name = name,
                dosage = dosage,
                time = time,
                photoPath = photoPath
            )
        )
    }

    fun update(medication: Medication) = viewModelScope.launch {
        dao.updateMedication(medication)
    }

    fun softDelete(medication: Medication) = viewModelScope.launch {
        dao.updateMedication(medication.copy(isDeleted = true))
    }

    fun undoDelete(medication: Medication) = viewModelScope.launch {
        dao.updateMedication(medication.copy(isDeleted = false))
    }

    fun permanentDelete(medication: Medication) = viewModelScope.launch {
        dao.permanentDelete(medication)
    }

    fun toggleTheme(darkMode: Boolean) = viewModelScope.launch {
        themePrefs.saveThemeSetting(darkMode)
    }

    val currentMedication = MutableStateFlow<Medication?>(null)
    fun loadMedicationById(id: Int, ownerEmail: String) = viewModelScope.launch {
        currentMedication.value = dao.getMedicationById(id, ownerEmail)
    }

    fun retrieveRemoteData(userId: String) = viewModelScope.launch(Dispatchers.IO) {
        _apiStatus.value = ApiStatus.LOADING
        try {
            _remoteMedications.value = RemoteMedicationApi.service.getMedications(userId)
            _apiStatus.value = ApiStatus.SUCCESS
        } catch (e: Exception) {
            Log.d("MedicationViewModel", "Remote load failed: ${e.message}")
            _apiStatus.value = ApiStatus.FAILED
            _apiMessage.value = e.toUserMessage("Gagal memuat data dari server.")
        }
    }

    fun uploadRemoteMedication(
        userId: String,
        name: String,
        details: String,
        bitmap: Bitmap
    ) = viewModelScope.launch(Dispatchers.IO) {
        if (userId.isBlank()) {
            _apiMessage.value = "Login dahulu untuk mengirim data."
            return@launch
        }

        try {
            val result = RemoteMedicationApi.service.postMedication(
                userId = userId,
                name = name.toRequestBody("text/plain".toMediaTypeOrNull()),
                details = details.toRequestBody("text/plain".toMediaTypeOrNull()),
                image = bitmap.toMultipartBody()
            )
            if (result.isSuccess) {
                retrieveRemoteData(userId)
            } else {
                _apiMessage.value = result.errorMessage ?: "Server menolak data yang dikirim."
            }
        } catch (e: Exception) {
            Log.d("MedicationViewModel", "Remote upload failed: ${e.message}")
            _apiMessage.value = e.toUserMessage("Gagal mengirim data ke server.")
        }
    }

    fun deleteRemoteMedication(
        userId: String,
        medication: RemoteMedication
    ) = viewModelScope.launch(Dispatchers.IO) {
        if (userId.isBlank()) {
            _apiMessage.value = "Login dahulu untuk menghapus data."
            return@launch
        }

        try {
            val deleteId = medication.id ?: medication.imageId
            if (deleteId.isNullOrBlank()) {
                _apiMessage.value = "Data ini tidak memiliki id untuk dihapus."
                return@launch
            }

            val result = RemoteMedicationApi.service.deleteMedication(userId, deleteId)
            if (result.isSuccess) {
                _remoteMedications.value = _remoteMedications.value.filterNot {
                    it.id == medication.id || it.imageId == medication.imageId
                }
                retrieveRemoteData(userId)
            } else {
                _apiMessage.value = result.errorMessage ?: "Data tidak dapat dihapus dari server."
            }
        } catch (e: Exception) {
            Log.d("MedicationViewModel", "Remote delete failed: ${e.message}")
            _apiMessage.value = e.toUserMessage("Gagal menghapus data dari server.")
        }
    }

    fun clearApiMessage() {
        _apiMessage.value = null
    }

    fun saveLocalPhoto(bitmap: Bitmap): String? {
        return try {
            val directory = File(getApplication<Application>().filesDir, "medication_photos")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, "medication_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { output ->
                bitmap.centerCropSquare().compress(Bitmap.CompressFormat.JPEG, 85, output)
            }
            file.absolutePath
        } catch (e: IOException) {
            Log.d("MedicationViewModel", "Local photo save failed: ${e.message}")
            null
        }
    }

    private fun Bitmap.toMultipartBody(): MultipartBody.Part {
        val squareBitmap = centerCropSquare()
        val stream = ByteArrayOutputStream()
        squareBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        val requestBody = byteArray.toRequestBody(
            "image/jpeg".toMediaTypeOrNull(),
            0,
            byteArray.size
        )
        return MultipartBody.Part.createFormData("image", "medication.jpg", requestBody)
    }

    private fun Bitmap.centerCropSquare(): Bitmap {
        val size = min(width, height)
        val x = (width - size) / 2
        val y = (height - size) / 2
        return Bitmap.createBitmap(this, x, y, size, size)
    }

    private fun Exception.toUserMessage(fallback: String): String {
        return when (this) {
            is IOException -> "Koneksi internet tidak tersedia atau server tidak dapat dihubungi."
            else -> message?.let { "$fallback $it" } ?: fallback
        }
    }
}
