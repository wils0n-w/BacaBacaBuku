package com.example.bacabacabuku.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bacabacabuku.data.local.BookEntity
import com.example.bacabacabuku.ui.viewmodel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: BookViewModel,
    onBookClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val allBooks by viewModel.allBooks.collectAsState()
    var selectedStatus by remember { mutableStateOf("All") }
    var selectedGenre by remember { mutableStateOf("All") }

    val statuses = listOf("All", "To Be Read", "Reading", "Read", "Dropped", "DNF")
    val genres = remember(allBooks) {
        listOf("All") + allBooks.map { it.genre }.distinct().sorted()
    }

    val filteredBooks = allBooks.filter {
        (selectedStatus == "All" || it.status == selectedStatus) &&
        (selectedGenre == "All" || it.genre == selectedGenre)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Library") },
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogoutClick()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Book")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Filters
            ScrollableTabRow(
                selectedTabIndex = statuses.indexOf(selectedStatus),
                edgePadding = 16.dp,
                divider = {}
            ) {
                statuses.forEach { status ->
                    Tab(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status },
                        text = { Text(status) }
                    )
                }
            }

            if (genres.size > 1) {
                ScrollableTabRow(
                    selectedTabIndex = genres.indexOf(selectedGenre),
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    divider = {}
                ) {
                    genres.forEach { genre ->
                        Tab(
                            selected = selectedGenre == genre,
                            onClick = { selectedGenre = genre },
                            text = { Text(genre, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredBooks) { book ->
                    BookCoverItem(book = book, onClick = { onBookClick(book.id) })
                }
            }
        }
    }
}

@Composable
fun BookCoverItem(book: BookEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(0.7f)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            AsyncImage(
                model = book.thumbnailUrl,
                contentDescription = book.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}
