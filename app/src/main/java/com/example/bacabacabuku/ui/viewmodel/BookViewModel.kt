package com.example.bacabacabuku.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bacabacabuku.data.local.BookEntity
import com.example.bacabacabuku.data.remote.BookItem
import com.example.bacabacabuku.data.repository.BookRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BookViewModel(private val repository: BookRepository) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<BookItem>>(emptyList())
    val searchResults: StateFlow<List<BookItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    val allBooks: StateFlow<List<BookEntity>> = repository.allBooks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun searchBooks(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isSearching.value = true
            _errorMessage.value = null
            val results = repository.searchBooks(query)
            _searchResults.value = results
            if (results.isEmpty()) {
                _errorMessage.value = "No books found or search failed."
            }
            _isSearching.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun login(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByUsername(username)
            if (user != null && user.password == password) {
                _isLoggedIn.value = true
                onResult(true)
            } else {
                _errorMessage.value = "Invalid username or password"
                onResult(false)
            }
        }
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    fun addBook(bookItem: BookItem, status: String, genre: String) {
        viewModelScope.launch {
            val thumbnailUrl = bookItem.volumeInfo.imageLinks?.thumbnail?.replace("http:", "https:")
            val bookEntity = BookEntity(
                id = bookItem.id,
                title = bookItem.volumeInfo.title,
                authors = bookItem.volumeInfo.authors?.joinToString(", ") ?: "Unknown",
                thumbnailUrl = thumbnailUrl,
                defaultThumbnailUrl = thumbnailUrl,
                description = bookItem.volumeInfo.description,
                genre = genre,
                status = status,
                rating = 0f
            )
            repository.insertBook(bookEntity)
        }
    }

    fun updateBookStatus(book: BookEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateBook(book.copy(status = newStatus))
        }
    }

    fun updateBookRating(book: BookEntity, newRating: Float) {
        viewModelScope.launch {
            repository.updateBook(book.copy(rating = newRating))
        }
    }

    fun updateBookGenre(book: BookEntity, newGenre: String) {
        viewModelScope.launch {
            repository.updateBook(book.copy(genre = newGenre))
        }
    }

    fun updateBookReview(book: BookEntity, newReview: String) {
        viewModelScope.launch {
            repository.updateBook(book.copy(review = newReview))
        }
    }

    fun deleteBook(book: BookEntity) {
        viewModelScope.launch {
            repository.deleteBook(book)
        }
    }

    fun updateBookCover(book: BookEntity, localUri: String) {
        viewModelScope.launch {
            repository.updateBook(book.copy(thumbnailUrl = localUri))
        }
    }

    fun resetBookCover(book: BookEntity) {
        viewModelScope.launch {
            repository.updateBook(book.copy(thumbnailUrl = book.defaultThumbnailUrl))
        }
    }

    suspend fun getBookById(id: String): BookEntity? {
        return repository.getBookById(id)
    }

    fun getBookByIdFlow(id: String): Flow<BookEntity?> {
        return repository.getBookByIdFlow(id)
    }
}

class BookViewModelFactory(private val repository: BookRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
