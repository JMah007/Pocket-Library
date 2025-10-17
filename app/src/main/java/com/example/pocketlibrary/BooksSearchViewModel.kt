package com.example.pocketlibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

// This data class holds the clean, UI-friendly book data
data class Books(
    val title: String,
    val author: String,
    val publishYear: Int,
    val coverUrl: String?
)

// This data class holds the entire state for the search screen
data class BooksSearchUiState(
    val query: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val results: List<Books> = emptyList()
)

class BooksSearchViewModel : ViewModel() {
    private val _state = MutableStateFlow(BooksSearchUiState())
    val state = _state.asStateFlow()

    private var searchJob: Job? = null

    fun updateQuery(newQuery: String) {
        // Using direct assignment for state update
        _state.value = _state.value.copy(query = newQuery)
        // Debounce: cancel the previous job and start a new one after a delay.
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500L) // A 500ms delay is good for typing.
            searchBooks()
        }
    }

    fun searchBooks() {
        val query = _state.value.query.trim()
        if (query.isBlank()) {
            _state.value = _state.value.copy(results = emptyList(), error = null, loading = false)
            return
        }

        viewModelScope.launch {
            // Set loading state
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                // Call the OpenLibrary API
                val response = Network.api.searchBooks(query = query)

                // Map the raw network results ('Doc') to UI-friendly 'Book' objects.
                val books = response.docs.mapNotNull { doc ->
                    if (doc.title == null) return@mapNotNull null

                    Books(
                        title = doc.title,
                        author = doc.authorName?.firstOrNull() ?: "Unknown Author",
                        publishYear = doc.firstPublishYear ?: 0,
                        coverUrl = doc.coverId?.let {
                            "https://covers.openlibrary.org/b/id/$it-L.jpg"
                        }
                    )
                }

                // Update the state with the final list of books
                _state.value = _state.value.copy(results = books, loading = false)

            } catch (t: Throwable) { // Using a single, general catch block
                _state.value = _state.value.copy(
                    loading = false,
                    error = t.message ?: "An unexpected error occurred."
                )
            }
        }
    }
}
