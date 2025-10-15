package com.example.pocketlibrary

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

// Retrofit interface
interface OpenLibraryApi {
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
    @Json(name = "author_name")
    val authorName: List<String>? = null,
    @Json(name = "first_publish_year")
    val firstPublishYear: Int? = null,
    @Json(name = "cover_i")
    val cover: Int? = null
)
