package com.example.pocketlibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Books(
    val title: String,
    val author: String,
    val publishYear: Int,
    val coverUrl: String?
)

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
        _state.value = _state.value.copy(query = newQuery)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500L)
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
                val response = Network.api.searchBooks(query = query)

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

                _state.value = _state.value.copy(results = books, loading = false)

            } catch (t: Throwable) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = t.message ?: "An unexpected error occurred."
                )
            }
        }
    }
}
