package com.example.pocketlibrary

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Defines the Retrofit service interface for the Open Library API.
 */
interface OpenLibraryApi {
    /**
     * Searches for books using a query string.
     * @param query The search term (e.g., "The Lord of the Rings").
     * @param fields A comma-separated list of fields to return, for efficiency.
     * @param limit The maximum number of results to return.
     */
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("fields") fields: String = "title,author_name,first_publish_year,cover_i",
        @Query("limit") limit: Int = 20
    ): OpenLibraryResponse
}

/**
 * Represents the top-level JSON response from the Open Library search API.
 * The most important field is 'docs', which contains the list of book results.
 */
@JsonClass(generateAdapter = true)
data class OpenLibraryResponse(
    val docs: List<Doc>
)

/**
 * Represents a single book document ('Doc') from the search results.
 * This class models the fields we requested from the API.
 */
@JsonClass(generateAdapter = true)
data class Doc(
    // The title of the book. It's nullable in case the API doesn't provide it.
    val title: String?,

    // The list of author names. The API returns an array of strings.
    @Json(name = "author_name")
    val authorName: List<String>?,

    // The year the book was first published.
    @Json(name = "first_publish_year")
    val firstPublishYear: Int?,

    // The ID of the book's cover image. We use this to build the image URL.
    // Using Long? is safer as some IDs can be large.
    @Json(name = "cover_i")
    val coverId: Long?
)