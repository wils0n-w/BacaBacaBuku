package com.example.bacabacabuku.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val authors: String, // Comma separated list
    val thumbnailUrl: String?, // Current thumbnail (remote or local)
    val defaultThumbnailUrl: String?, // Original thumbnail from API
    val description: String?,
    val genre: String,
    val status: String,
    val rating: Float,
    val review: String? = null
)
