package com.example.pocketlibrary

import retrofit2.http.GET
import retrofit2.http.Query
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

// Retrofit API Interface
interface OpenLibraryApiService {
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("fields") fields: String = "title,author_name,first_publish_year,cover_i",
        @Query("limit") limit: Int = 20
    ): OpenLibraryResponse
}

// Response Data Classes
@JsonClass(generateAdapter = true)
data class OpenLibraryResponse(
    val docs: List<OpenLibraryBook>
)

@JsonClass(generateAdapter = true)
data class OpenLibraryBook(
    val title: String?,
    @Json(name = "author_name") val authorName: List<String>?,
    @Json(name = "first_publish_year") val firstPublishYear: Int?,
    @Json(name = "cover_i") val coverId: Int?
) {
    fun toBook(): Book {
        return Book(
            title = title ?: "Unknown Title",
            author = authorName?.firstOrNull() ?: "Unknown Author",
            year = firstPublishYear?.toString() ?: "Unknown Year",
            addedManually = false,
            coverUrl = coverId?.let { "https://covers.openlibrary.org/b/id/${it}-S.jpg" }
        )
    }
}

// Singleton object for API
object OpenLibraryAPI {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://openlibrary.org/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val service = retrofit.create(OpenLibraryApiService::class.java)

    // Function to call API and return List<Book>
    suspend fun searchBooks(query: String): List<Book> {
        if (query.isBlank()) return emptyList()

        return try {
            val response = service.searchBooks(query)
            response.docs.map { it.toBook() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
