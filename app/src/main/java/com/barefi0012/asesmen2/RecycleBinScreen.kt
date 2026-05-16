package com.barefi0012.asesmen2

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.barefi0012.asesmen2.data.Medication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinScreen(viewModel: MedicationViewModel, onNavigateBack: () -> Unit) {
    val deletedMedications by viewModel.deletedMedications.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedMedication by remember { mutableStateOf<Medication?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recycle Bin") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Kembali") } }
            )
        }
    ) { paddingValues ->
        if (deletedMedications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Recycle Bin kosong.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(paddingValues).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(deletedMedications, key = { it.id }) { med ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = med.name, style = MaterialTheme.typography.titleLarge)
                                Text(text = "Dosis: ${med.dosage}")
                            }
                            IconButton(onClick = { viewModel.undoDelete(med) }) { Icon(Icons.Default.Undo, contentDescription = "Pulihkan") }
                            IconButton(onClick = { selectedMedication = med; showDialog = true }) { Icon(Icons.Default.DeleteForever, contentDescription = "Hapus Absolut") }
                        }
                    }
                }
            }
        }

        if (showDialog && selectedMedication != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Hapus Obat Permanen?") },
                text = { Text("Data '${selectedMedication!!.name}' akan dihapus secara permanen dari database lokal.") },
                confirmButton = { TextButton(onClick = { viewModel.permanentDelete(selectedMedication!!); showDialog = false }) { Text("Hapus") } },
                dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Batal") } }
            )
        }
    }
}