package com.example.bacabacabuku.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bacabacabuku.data.remote.BookItem
import com.example.bacabacabuku.ui.viewmodel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: BookViewModel,
    onBackClick: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var showAddDialog by remember { mutableStateOf<BookItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search Google Books...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { viewModel.searchBooks(query) }),
                        trailingIcon = {
                            IconButton(onClick = { viewModel.searchBooks(query) }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                isSearching -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.searchBooks(query) }) {
                            Text("Retry")
                        }
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(searchResults) { book ->
                            SearchResultItem(book = book, onClick = { showAddDialog = book })
                        }
                    }
                }
            }
        }

        if (showAddDialog != null) {
            AddBookDialog(
                book = showAddDialog!!,
                onDismiss = { showAddDialog = null },
                onConfirm = { status, genre ->
                    viewModel.addBook(showAddDialog!!, status, genre)
                    showAddDialog = null
                    onBackClick()
                }
            )
        }
    }
}

@Composable
fun SearchResultItem(book: BookItem, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(book.volumeInfo.title) },
        supportingContent = { Text(book.volumeInfo.authors?.joinToString(", ") ?: "Unknown") },
        leadingContent = {
            AsyncImage(
                model = book.volumeInfo.imageLinks?.thumbnail?.replace("http:", "https:"),
                contentDescription = null,
                modifier = Modifier.size(50.dp)
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookDialog(
    book: BookItem,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf("To Be Read") }
    var genre by remember { mutableStateOf(book.volumeInfo.categories?.firstOrNull() ?: "") }
    val statuses = listOf("To Be Read", "Reading", "Read", "Dropped", "DNF")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Library") },
        text = {
            Column {
                Text(book.volumeInfo.title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Status:")
                statuses.forEach { status ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { selectedStatus = status }
                    ) {
                        RadioButton(selected = selectedStatus == status, onClick = { selectedStatus = status })
                        Text(status)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text("Genre") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedStatus, genre) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
