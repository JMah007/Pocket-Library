package com.example.pocketlibrary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UiState(
    val query: String = "",
)

class BooksSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val _results = MutableStateFlow<List<Book>>(emptyList())
    val results: StateFlow<List<Book>> = _results

    fun search(query: String) {
        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    _results.value = emptyList()
                    return@launch
                }

                val books = OpenLibraryAPI.searchBooks(query)
                _results.value = books
            } catch (e: Exception) {
                _results.value = emptyList()
            }
        }
    }
}
