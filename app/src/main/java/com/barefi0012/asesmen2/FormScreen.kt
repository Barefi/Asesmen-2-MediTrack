package com.barefi0012.asesmen2

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(viewModel: MedicationViewModel, medicationId: Int?, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    val currentMedication by viewModel.currentMedication.collectAsState()
    val isEditMode = medicationId != null && medicationId != -1

    LaunchedEffect(medicationId) {
        if (isEditMode) viewModel.loadMedicationById(medicationId!!)
    }

    LaunchedEffect(currentMedication) {
        if (isEditMode && currentMedication != null) {
            name = currentMedication!!.name
            dosage = currentMedication!!.dosage
            time = currentMedication!!.time
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Data Obat" else "Tambah Obat Baru") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Kembali") } }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Obat") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = dosage, onValueChange = { dosage = it }, label = { Text("Dosis") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Waktu Minum") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    val validationError = viewModel.validateInput(name, dosage, time)
                    if (validationError != null) {
                        Toast.makeText(context, validationError, Toast.LENGTH_SHORT).show()
                    } else {
                        if (isEditMode && currentMedication != null) {
                            viewModel.update(currentMedication!!.copy(name = name, dosage = dosage, time = time))
                        } else {
                            viewModel.insert(name, dosage, time)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (isEditMode) "Simpan Perubahan" else "Tambah Jadwal") }
        }
    }
}