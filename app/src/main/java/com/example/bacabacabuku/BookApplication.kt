package com.example.bacabacabuku

import android.app.Application
import com.example.bacabacabuku.data.local.BookDatabase
import com.example.bacabacabuku.data.local.UserEntity
import com.example.bacabacabuku.data.remote.GoogleBooksService
import com.example.bacabacabuku.data.repository.BookRepository
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BookApplication : Application() {
    val database by lazy { BookDatabase.getDatabase(this) }
    
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/books/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val googleBooksService by lazy {
        retrofit.create(GoogleBooksService::class.java)
    }

    val repository by lazy {
        BookRepository(
            database.bookDao(),
            database.userDao(),
            googleBooksService,
            "AIzaSyCtma1b69-0yCP3AtSh4Fk7gKmYorw725s"
        )
    }

    override fun onCreate() {
        super.onCreate()
        // Pre-populate with default user
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
        scope.launch {
            if (repository.getUserByUsername("user") == null) {
                repository.insertUser(UserEntity("user", "password"))
            }
        }
    }
}
