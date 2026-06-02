package com.example.bacabacabuku.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bacabacabuku.data.local.BookEntity
import com.example.bacabacabuku.ui.viewmodel.BookViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,
    viewModel: BookViewModel,
    onBackClick: () -> Unit
) {
    val book by viewModel.getBookByIdFlow(bookId).collectAsState(initial = null)
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            val currentBook = book
            if (uri != null && currentBook != null) {
                // Copy to internal storage
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.filesDir, "cover_${currentBook.id}.jpg")
                inputStream?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                viewModel.updateBookCover(currentBook, file.absolutePath)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book?.title ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val currentBook = book
                    if (currentBook != null) {
                        IconButton(onClick = {
                            viewModel.deleteBook(currentBook)
                            onBackClick()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        val currentBook = book
        if (currentBook == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                AsyncImage(
                    model = currentBook.thumbnailUrl,
                    contentDescription = currentBook.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Fit
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = {
                            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Cover")
                    }
                    
                    if (currentBook.thumbnailUrl != currentBook.defaultThumbnailUrl) {
                        TextButton(
                            onClick = { viewModel.resetBookCover(currentBook) }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(currentBook.title, style = MaterialTheme.typography.headlineMedium)
                Text(currentBook.authors, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Status: ", style = MaterialTheme.typography.bodyLarge)
                    var expanded by remember { mutableStateOf(false) }
                    val statuses = listOf("To Be Read", "Reading", "Read", "Dropped", "DNF")
                    
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text(currentBook.status)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            statuses.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status) },
                                    onClick = {
                                        viewModel.updateBookStatus(currentBook, status)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Genre: ${currentBook.genre}", style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Rating: ${currentBook.rating}/5")
                Slider(
                    value = currentBook.rating,
                    onValueChange = { viewModel.updateBookRating(currentBook, it) },
                    valueRange = 0f..5f,
                    steps = 4
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Synopsis", style = MaterialTheme.typography.titleLarge)
                Text(currentBook.description ?: "No description available.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
