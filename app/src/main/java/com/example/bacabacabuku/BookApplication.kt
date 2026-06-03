package com.example.bacabacabuku

import android.app.Application
import android.util.Log
import com.example.bacabacabuku.data.local.BookDatabase
import com.example.bacabacabuku.data.local.UserEntity
import com.example.bacabacabuku.data.remote.GoogleBooksService
import com.example.bacabacabuku.data.repository.BookRepository
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BookApplication : Application() {
    val database by lazy { BookDatabase.getDatabase(this) }
    
    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val packageName = "com.example.bacabacabuku"
                val sha1 = "28:79:0D:92:5F:F4:BA:D4:4B:1B:B0:DA:4D:BD:7E:86:E9:28:5E:80"
                
                // Use Log.e to make sure it shows up in Logcat clearly
                Log.e("NETWORK_HEADERS", "Applying headers - Pkg: $packageName, SHA1: $sha1")
                
                val request = chain.request().newBuilder()
                    .header("X-Android-Package", packageName)
                    .header("X-Android-Cert", sha1)
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/books/v1/")
            .client(client)
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
