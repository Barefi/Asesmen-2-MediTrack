package com.barefi0012.asesmen2

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.barefi0012.asesmen2.model.RemoteMedication
import com.barefi0012.asesmen2.model.UserProfile
import com.barefi0012.asesmen2.network.ApiStatus
import com.barefi0012.asesmen2.network.RemoteMedicationApi
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudMedicationScreen(
    viewModel: MedicationViewModel,
    userProfile: UserProfile,
    onNavigateBack: () -> Unit,
    onLoginClick: () -> Unit
) {
    val medications by viewModel.remoteMedications.collectAsState()
    val status by viewModel.apiStatus.collectAsState()
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var selectedMedication by remember { mutableStateOf<RemoteMedication?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        capturedBitmap = bitmap?.centerCropSquare()
        showUploadDialog = capturedBitmap != null
    }

    LaunchedEffect(userProfile.email) {
        if (userProfile.isLoggedIn) {
            viewModel.retrieveRemoteData(userProfile.email)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cloud_title)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.desc_back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (userProfile.isLoggedIn) {
                FloatingActionButton(onClick = { cameraLauncher.launch(null) }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.desc_add_cloud)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (!userProfile.isLoggedIn) {
                LoginRequiredContent(onLoginClick)
            } else {
                CloudContent(
                    status = status,
                    medications = medications,
                    ownerEmail = userProfile.email,
                    onRetry = { viewModel.retrieveRemoteData(userProfile.email) },
                    onDelete = { selectedMedication = it }
                )
            }
        }
    }

    if (showUploadDialog && capturedBitmap != null) {
        UploadMedicationDialog(
            bitmap = capturedBitmap!!,
            onDismissRequest = { showUploadDialog = false },
            onSave = { name, details ->
                viewModel.uploadRemoteMedication(userProfile.email, name, details, capturedBitmap!!)
                showUploadDialog = false
            }
        )
    }

    selectedMedication?.let { medication ->
        AlertDialog(
            onDismissRequest = { selectedMedication = null },
            title = { Text(stringResource(R.string.delete_cloud_title)) },
            text = { Text(stringResource(R.string.delete_cloud_message, medication.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRemoteMedication(userProfile.email, medication)
                        selectedMedication = null
                    }
                ) {
                    Text(stringResource(R.string.btn_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMedication = null }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
private fun LoginRequiredContent(onLoginClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.cloud_login_required),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onLoginClick) {
            Text(stringResource(R.string.btn_login))
        }
    }
}

@Composable
private fun CloudContent(
    status: ApiStatus,
    medications: List<RemoteMedication>,
    ownerEmail: String,
    onRetry: () -> Unit,
    onDelete: (RemoteMedication) -> Unit
) {
    when (status) {
        ApiStatus.LOADING -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        ApiStatus.FAILED -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = stringResource(R.string.cloud_load_error))
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRetry) {
                    Text(text = stringResource(R.string.btn_try_again))
                }
            }
        }

        ApiStatus.IDLE,
        ApiStatus.SUCCESS -> {
            if (medications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(R.string.cloud_empty))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = medications,
                        key = { it.id ?: it.imageId ?: "${it.name}-${it.details}" }
                    ) { medication ->
                        RemoteMedicationCard(
                            medication = medication,
                            ownerEmail = ownerEmail,
                            onDelete = { onDelete(medication) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RemoteMedicationCard(
    medication: RemoteMedication,
    ownerEmail: String,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubcomposeAsyncImage(
                model = medication.imageId?.let {
                    ImageRequest.Builder(context)
                        .data(RemoteMedicationApi.imageUrl(it))
                        .addHeader("Authorization", ownerEmail)
                        .build()
                },
                contentDescription = stringResource(R.string.cloud_image, medication.name),
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                },
                error = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CloudOff, contentDescription = null)
                    }
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = medication.details,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.desc_delete_cloud)
                )
            }
        }
    }
}

@Composable
private fun UploadMedicationDialog(
    bitmap: Bitmap,
    onDismissRequest: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    val canSave = name.isNotBlank() && details.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(R.string.add_cloud_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.label_cloud_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text(stringResource(R.string.label_cloud_details)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = { onSave(name.trim(), details.trim()) }
            ) {
                Text(text = stringResource(R.string.btn_send))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.btn_cancel))
            }
        }
    )
}

private fun Bitmap.centerCropSquare(): Bitmap {
    val size = min(width, height)
    val x = (width - size) / 2
    val y = (height - size) / 2
    return Bitmap.createBitmap(this, x, y, size, size)
}
