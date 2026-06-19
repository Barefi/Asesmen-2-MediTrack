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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    viewModel: MedicationViewModel,
    medicationId: Int?,
    ownerEmail: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var bitmapToCrop by remember { mutableStateOf<Bitmap?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmapToCrop = bitmap
    }

    val currentMedication by viewModel.currentMedication.collectAsState()
    val isEditMode = medicationId != null && medicationId != -1

    LaunchedEffect(medicationId, ownerEmail) {
        if (medicationId != null && medicationId != -1 && ownerEmail.isNotBlank()) {
            viewModel.loadMedicationById(medicationId, ownerEmail)
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
                            viewModel.insert(ownerEmail, name, dosage, time, savedPhotoPath)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (isEditMode) "Simpan Perubahan" else "Tambah Jadwal") }
        }
    }

    bitmapToCrop?.let { sourceBitmap ->
        CropPhotoDialog(
            sourceBitmap = sourceBitmap,
            onDismissRequest = { bitmapToCrop = null },
            onUsePhoto = { croppedBitmap ->
                capturedBitmap = croppedBitmap
                bitmapToCrop = null
            }
        )
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

@Composable
private fun CropPhotoDialog(
    sourceBitmap: Bitmap,
    onDismissRequest: () -> Unit,
    onUsePhoto: (Bitmap) -> Unit
) {
    var cropZoom by remember(sourceBitmap) { mutableStateOf(1f) }
    var horizontalOffset by remember(sourceBitmap) { mutableStateOf(0.5f) }
    var verticalOffset by remember(sourceBitmap) { mutableStateOf(0.5f) }
    val croppedBitmap = remember(sourceBitmap, cropZoom, horizontalOffset, verticalOffset) {
        cropBitmap(sourceBitmap, cropZoom, horizontalOffset, verticalOffset)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.crop_photo_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    bitmap = croppedBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Text(stringResource(R.string.crop_zoom_label))
                Slider(
                    value = cropZoom,
                    onValueChange = { cropZoom = it },
                    valueRange = 1f..3f
                )
                Text(stringResource(R.string.crop_horizontal_label))
                Slider(
                    value = horizontalOffset,
                    onValueChange = { horizontalOffset = it },
                    valueRange = 0f..1f
                )
                Text(stringResource(R.string.crop_vertical_label))
                Slider(
                    value = verticalOffset,
                    onValueChange = { verticalOffset = it },
                    valueRange = 0f..1f
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onUsePhoto(croppedBitmap) }) {
                Text(stringResource(R.string.btn_use_photo))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

private fun cropBitmap(
    source: Bitmap,
    zoom: Float,
    horizontalOffset: Float,
    verticalOffset: Float
): Bitmap {
    val minDimension = min(source.width, source.height)
    val cropSize = (minDimension / zoom)
        .roundToInt()
        .coerceIn(1, minDimension)
    val maxX = (source.width - cropSize).coerceAtLeast(0)
    val maxY = (source.height - cropSize).coerceAtLeast(0)
    val x = (maxX * horizontalOffset).roundToInt().coerceIn(0, maxX)
    val y = (maxY * verticalOffset).roundToInt().coerceIn(0, maxY)

    return Bitmap.createBitmap(source, x, y, cropSize, cropSize)
}
