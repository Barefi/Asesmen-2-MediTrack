package com.barefi0012.asesmen2

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.barefi0012.asesmen2.model.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MedicationViewModel,
    userProfile: UserProfile,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateToBin: () -> Unit,
    onShowProfile: () -> Unit
) {
    val medications by viewModel.medications.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MediTrack") },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Dark Mode", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(checked = isDark, onCheckedChange = { viewModel.toggleTheme(it) })
                    }
                    IconButton(onClick = onNavigateToBin) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Recycle Bin")
                    }
                    IconButton(onClick = onShowProfile) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = stringResource(R.string.desc_profile),
                            tint = if (userProfile.isLoggedIn) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Obat")
            }
        }
    ) { paddingValues ->
        if (medications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Tidak ada jadwal obat hari ini.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(paddingValues).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(medications, key = { it.id }) { med ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onNavigateToEdit(med.id) }) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = med.name, style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Dosis: ${med.dosage} | Jam: ${med.time}")
                            }
                            Checkbox(checked = med.isTaken, onCheckedChange = { viewModel.update(med.copy(isTaken = it)) })
                            IconButton(onClick = { viewModel.softDelete(med) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus Sementara")
                            }
                        }
                    }
                }
            }
        }
    }
}
