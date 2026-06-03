package com.example.bacabacabuku.data.repository

import com.example.bacabacabuku.data.local.BookDao
import com.example.bacabacabuku.data.local.BookEntity
import com.example.bacabacabuku.data.local.UserDao
import com.example.bacabacabuku.data.local.UserEntity
import com.example.bacabacabuku.data.remote.GoogleBooksService
import com.example.bacabacabuku.data.remote.BookItem
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

class BookRepository(
    private val bookDao: BookDao,
    private val userDao: UserDao,
    private val googleBooksService: GoogleBooksService,
    private val apiKey: String
) {
    val allBooks: Flow<List<BookEntity>> = bookDao.getAllBooks()

    suspend fun getUserByUsername(username: String): UserEntity? {
        return userDao.getUserByUsername(username)
    }

    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun searchBooks(query: String): List<BookItem> {
        return try {
            val response = googleBooksService.searchBooks(query, apiKey)
            android.util.Log.d("BookRepository", "Search results count: ${response.items?.size ?: 0}")
            response.items ?: emptyList()
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("BookRepository", "Search failed with HTTP ${e.code()}: $errorBody")
            emptyList()
        } catch (e: Exception) {
            android.util.Log.e("BookRepository", "Search failed", e)
            emptyList()
        }
    }

    suspend fun insertBook(book: BookEntity) {
        bookDao.insertBook(book)
    }

    suspend fun updateBook(book: BookEntity) {
        bookDao.updateBook(book)
    }

    suspend fun deleteBook(book: BookEntity) {
        bookDao.deleteBook(book)
    }

    suspend fun getBookById(id: String): BookEntity? {
        return bookDao.getBookById(id)
    }

    fun getBookByIdFlow(id: String): Flow<BookEntity?> {
        return bookDao.getBookByIdFlow(id)
    }

    fun getBooksByStatus(status: String): Flow<List<BookEntity>> {
        return bookDao.getBooksByStatus(status)
    }
}
