package com.example.pocketlibrary

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Retrofit interface
interface OpenLibraryService {
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("fields") fields: String = "title,author_name,first_publish_year,cover_i",
        @Query("limit") limit: Int = 20
    ): OpenLibraryResponse
}

// Response data classes
@JsonClass(generateAdapter = true)
data class OpenLibraryResponse(
    val docs: List<Doc>
)

@JsonClass(generateAdapter = true)
data class Doc(
    val title: String?,
    @Json(name = "author_name") val authorName: List<String>?,
    @Json(name = "first_publish_year") val firstPublishYear: Int?,
    @Json(name = "cover_i") val coverId: Int?
)

// Singleton API object
object OpenLibraryAPI {
    private const val BASE_URL = "https://openlibrary.org/"

    private val service: OpenLibraryService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(OpenLibraryService::class.java)
    }

    suspend fun searchBooks(query: String): List<Book> {
        if (query.isBlank()) return emptyList()

        return try {
            val response = service.searchBooks(query)
            response.docs.map { doc ->
                val title = doc.title ?: "Unknown Title"
                val author = doc.authorName?.firstOrNull() ?: "Unknown Author"
                val year = doc.firstPublishYear?.toString() ?: "Unknown Year"
                val coverUrl = doc.coverId?.let { "https://covers.openlibrary.org/b/id/${it}-S.jpg" }

                Book(
                    title = title,
                    author = author,
                    year = year,
                    addedManually = false,
                    coverUrl = coverUrl
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
