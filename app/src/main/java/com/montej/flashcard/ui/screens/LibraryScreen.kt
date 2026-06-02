package com.montej.flashcard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.montej.flashcard.data.local.FileEntity
import com.montej.flashcard.data.local.FolderEntity
import com.montej.flashcard.data.repository.LibraryRepository
import com.montej.flashcard.ui.theme.ThemeMode
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LibraryScreen(
    onNavigateToFlashcard: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    themeMode: ThemeMode
) {
    val context = LocalContext.current
    val repository = remember { LibraryRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    var currentFolderId by remember { mutableStateOf<Long?>(null) }
    var folders by remember { mutableStateOf<List<FolderEntity>>(emptyList()) }
    var files by remember { mutableStateOf<List<FileEntity>>(emptyList()) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var folderPath by remember { mutableStateOf<List<Pair<Long?, String>>>(emptyList()) }
    var sortBy by remember { mutableStateOf(SortOption.NAME) }

    LaunchedEffect(currentFolderId) {
        repository.apply {
            if (currentFolderId == null) {
                getRootFolders().collect { folders = it }
            } else {
                getSubFolders(currentFolderId!!).collect { folders = it }
                currentFolderId?.let {
                    getFilesByFolder(it).collect { files = it }
                }
            }
        }
    }

    val sortedFolders = when (sortBy) {
        SortOption.NAME -> folders.sortedBy { it.name }
        SortOption.DATE -> folders.sortedByDescending { it.createdAt }
        SortOption.MODIFIED -> folders.sortedByDescending { it.modifiedAt }
        else -> folders
    }

    val sortedFiles = when (sortBy) {
        SortOption.NAME -> files.sortedBy { it.name }
        SortOption.DATE -> files.sortedByDescending { it.createdAt }
        SortOption.MODIFIED -> files.sortedByDescending { it.modifiedAt }
        else -> files
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Montej Flashcard") },
            actions = {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        )

        if (folderPath.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Home",
                    modifier = Modifier
                        .clickable {
                            currentFolderId = null
                            folderPath = emptyList()
                        }
                        .padding(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall
                )
                folderPath.forEach { (id, name) ->
                    Text(" / ", modifier = Modifier.padding(0.dp, 4.dp), style = MaterialTheme.typography.labelSmall)
                    Text(
                        name,
                        modifier = Modifier
                            .clickable {
                                currentFolderId = id
                                folderPath = folderPath.takeWhile { it.first != id }
                            }
                            .padding(4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SortOption.values().forEach { option ->
                FilterChip(
                    selected = sortBy == option,
                    onClick = { sortBy = option },
                    label = { Text(option.displayName, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.height(32.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sortedFolders) { folder ->
                FolderItem(
                    folder = folder,
                    onClickFolder = {
                        folderPath = folderPath + (currentFolderId to (folders.find { it.id == currentFolderId }?.name ?: ""))
                        currentFolderId = folder.id
                    },
                    onDelete = {
                        coroutineScope.launch {
                            repository.deleteFolder(folder.id)
                        }
                    },
                    onRename = { newName ->
                        coroutineScope.launch {
                            repository.renameFolder(folder.id, newName)
                        }
                    }
                )
            }

            items(sortedFiles) { file ->
                FileItem(
                    file = file,
                    onClickFile = { onNavigateToFlashcard(file.id) },
                    onDelete = {
                        coroutineScope.launch {
                            repository.deleteFile(file.id)
                        }
                    },
                    onRename = { newName ->
                        coroutineScope.launch {
                            repository.renameFile(file.id, newName)
                        }
                    }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showCreateFolderDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                Text("New Folder")
            }
        }
    }

    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("Create New Folder") },
            text = {
                TextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Folder Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            coroutineScope.launch {
                                repository.createFolder(newFolderName, currentFolderId)
                                newFolderName = ""
                                showCreateFolderDialog = false
                            }
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FolderItem(
    folder: FolderEntity,
    onClickFolder: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(folder.name) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickFolder() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 12.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        folder.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                    Text(
                        "Modified: ${dateFormat.format(Date(folder.modifiedAt))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = {
                            showRenameDialog = true
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDelete()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                    )
                }
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Folder") },
            text = {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Folder Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRename(newName)
                        showRenameDialog = false
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FileItem(
    file: FileEntity,
    onClickFile: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(file.name) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickFile() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 12.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        file.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                    Text(
                        "Type: ${file.fileType.uppercase()} • Modified: ${dateFormat.format(Date(file.modifiedAt))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = {
                            showRenameDialog = true
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDelete()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                    )
                }
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename File") },
            text = {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("File Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRename(newName)
                        showRenameDialog = false
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

enum class SortOption(val displayName: String) {
    NAME("Name"),
    DATE("Date"),
    MODIFIED("Modified"),
    TYPE("Type")
}