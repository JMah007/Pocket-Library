package com.example.pocketlibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BooksSearchViewModel : ViewModel() {

    private val _results = MutableStateFlow<List<Book>>(emptyList())
    val results: StateFlow<List<Book>> = _results

    fun search(query: String) {
        if (query.isBlank()) {
            _results.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                val response = Network.OpenLibraryApi.searchBooks(query)
                val books = response.docs.map { doc ->
                    Book(
                        title = doc.title ?: "Unknown Title",
                        author = doc.authorName?.firstOrNull() ?: "Unknown Author",
                        year = doc.firstPublishYear?.toString() ?: "Unknown Year",
                        addedManually = false,
                        coverUrl = doc.cover?.let {
                            "https://covers.openlibrary.org/b/id/${it}-M.jpg"
                        }
                    )
                }
                _results.value = books
            } catch (e: Exception) {
                e.printStackTrace()
                _results.value = emptyList()
            }
        }
    }

    fun clearResults() {
        _results.value = emptyList()
    }
}
