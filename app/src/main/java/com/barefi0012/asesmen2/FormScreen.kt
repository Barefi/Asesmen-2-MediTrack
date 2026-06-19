package com.barefi0012.asesmen2

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(viewModel: MedicationViewModel, medicationId: Int?, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        capturedBitmap = bitmap
    }

    val currentMedication by viewModel.currentMedication.collectAsState()
    val isEditMode = medicationId != null && medicationId != -1

    LaunchedEffect(medicationId) {
        if (medicationId != null && medicationId != -1) {
            viewModel.loadMedicationById(medicationId)
        }
    }

    LaunchedEffect(currentMedication) {
        if (isEditMode && currentMedication != null) {
            name = currentMedication!!.name
            dosage = currentMedication!!.dosage
            time = currentMedication!!.time
            photoPath = currentMedication!!.photoPath
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Data Obat" else "Tambah Obat Baru") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali") } }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            LocalPhotoPicker(
                name = name,
                photoPath = photoPath,
                capturedBitmap = capturedBitmap,
                onTakePhoto = { cameraLauncher.launch(null) }
            )
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
                        val savedPhotoPath = capturedBitmap?.let { viewModel.saveLocalPhoto(it) } ?: photoPath
                        if (isEditMode && currentMedication != null) {
                            viewModel.update(
                                currentMedication!!.copy(
                                    name = name,
                                    dosage = dosage,
                                    time = time,
                                    photoPath = savedPhotoPath
                                )
                            )
                        } else {
                            viewModel.insert(name, dosage, time, savedPhotoPath)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (isEditMode) "Simpan Perubahan" else "Tambah Jadwal") }
        }
    }
}

@Composable
private fun LocalPhotoPicker(
    name: String,
    photoPath: String?,
    capturedBitmap: Bitmap?,
    onTakePhoto: () -> Unit
) {
    val existingBitmap = remember(photoPath) {
        photoPath?.let { BitmapFactory.decodeFile(it) }
    }
    val previewBitmap = capturedBitmap ?: existingBitmap
    val fallbackName = stringResource(R.string.label_med_name)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (previewBitmap != null) {
            Image(
                bitmap = previewBitmap.asImageBitmap(),
                contentDescription = stringResource(
                    R.string.medication_photo,
                    name.ifBlank { fallbackName }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
        OutlinedButton(
            onClick = onTakePhoto,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(
                    if (previewBitmap == null) R.string.btn_take_photo else R.string.btn_retake_photo
                )
            )
        }
    }
}
