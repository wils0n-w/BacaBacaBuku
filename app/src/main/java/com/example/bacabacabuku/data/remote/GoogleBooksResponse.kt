package com.example.bacabacabuku.data.remote

data class GoogleBooksResponse(
    val items: List<BookItem>?
)

data class BookItem(
    val id: String,
    val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    val title: String,
    val authors: List<String>?,
    val description: String?,
    val imageLinks: ImageLinks?,
    val categories: List<String>?
)

data class ImageLinks(
    val thumbnail: String?
)
