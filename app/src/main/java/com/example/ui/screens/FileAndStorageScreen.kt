package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.CyberDarkCard
import com.example.ui.theme.CyberDarkCardElevated
import com.example.ui.theme.HoloBlue
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.util.StorageHelper

@Composable
fun FileAndStorageScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    val storageStats by viewModel.storageStats.collectAsState()
    val permittedFiles by viewModel.permittedFiles.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFolder by remember { mutableStateOf("All") }
    var newFolderName by remember { mutableStateOf("") }
    var folders by remember { mutableStateOf(StorageHelper.getWorkspaceFolders(context)) }

    LaunchedEffect(Unit) {
        viewModel.refreshStorageStats()
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val targetFolder = if (selectedFolder == "All") "Documents" else selectedFolder
            viewModel.addPermittedFile(uri, mime, targetFolder)
        }
    }

    val filteredFiles = permittedFiles.filter { entry ->
        val matchesFolder = selectedFolder == "All" || entry.folderCategory.equals(selectedFolder, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() || entry.displayName.contains(searchQuery, ignoreCase = true)
        matchesFolder && matchesSearch
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("files_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Storage, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Files & Storage Manager",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Device Storage Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberDarkCard),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("INTERNAL DEVICE STORAGE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonCyan, letterSpacing = 1.sp)
                            Text("${(storageStats.usedPercent * 100).toInt()}% USED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonAmber)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { storageStats.usedPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = NeonCyan,
                            trackColor = CyberDarkCardElevated
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Used: ${storageStats.usedFormatted}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Free: ${storageStats.freeFormatted}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                            Text("Total: ${storageStats.totalFormatted}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Folder Organizer & Add Permitted File
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberDarkCard),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("PERMITTED FILES & FOLDERS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonCyan, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FolderChip(
                                    name = "All",
                                    isSelected = selectedFolder == "All",
                                    onClick = { selectedFolder = "All" }
                                )
                            }
                            items(folders) { folder ->
                                FolderChip(
                                    name = folder,
                                    isSelected = selectedFolder == folder,
                                    onClick = { selectedFolder = folder }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newFolderName,
                                onValueChange = { newFolderName = it },
                                label = { Text("New Folder") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    if (StorageHelper.createWorkspaceFolder(context, newFolderName)) {
                                        folders = StorageHelper.getWorkspaceFolders(context)
                                        selectedFolder = newFolderName.trim()
                                        newFolderName = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = HoloBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CreateNewFolder, contentDescription = "Create Folder", modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { documentPickerLauncher.launch(arrayOf("*/*")) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("add_permitted_file_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Select File (SAF)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        context.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        })
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("System Storage", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Search Files
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search permitted files...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            if (filteredFiles.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberDarkCardElevated),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No permitted files indexed here yet. Use 'Select File (SAF)' to safely access documents, PDFs, or media.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredFiles, key = { it.id }) { fileEntry ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberDarkCardElevated),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(fileEntry.displayName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        text = "${fileEntry.folderCategory} • ${StorageHelper.formatSize(fileEntry.sizeBytes)}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Row {
                                IconButton(
                                    onClick = {
                                        viewModel.shareMediaGeneric(
                                            Uri.parse(fileEntry.uriString),
                                            fileEntry.mimeType,
                                            fileEntry.displayName
                                        )
                                    }
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share file", tint = NeonGreen, modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = { viewModel.deletePermittedFileEntry(fileEntry.id) }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove from list", tint = NeonPink, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun FolderChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else CyberDarkCardElevated,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) NeonCyan else Color.Transparent
        ),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Folder, contentDescription = null, tint = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(name, fontSize = 12.sp, color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurface)
        }
    }
}
