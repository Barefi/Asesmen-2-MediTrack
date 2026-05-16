package com.barefi0012.asesmen2

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.barefi0012.asesmen2.data.AppDatabase
import com.barefi0012.asesmen2.data.Medication
import com.barefi0012.asesmen2.data.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedicationViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).medicationDao()
    private val themePrefs = ThemePreferences(application)

    val medications: StateFlow<List<Medication>> = dao.getAllActiveMedications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedMedications: StateFlow<List<Medication>> = dao.getDeletedMedications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isDarkMode: StateFlow<Boolean> = themePrefs.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)


    fun validateInput(name: String, dosage: String, time: String): String? {
        if (name.trim().isEmpty()) return "Nama obat tidak boleh kosong!"
        if (dosage.trim().isEmpty()) return "Dosis obat tidak boleh kosong!"
        if (time.trim().isEmpty()) return "Waktu konsumsi tidak boleh kosong!"
        return null
    }

    fun insert(name: String, dosage: String, time: String) = viewModelScope.launch {
        dao.insertMedication(Medication(name = name, dosage = dosage, time = time))
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
    fun loadMedicationById(id: Int) = viewModelScope.launch {
        currentMedication.value = dao.getMedicationById(id)
    }
}