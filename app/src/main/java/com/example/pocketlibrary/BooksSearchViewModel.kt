package com.example.pocketlibrary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


data class UiState(
    val query: String = "",
)


class BooksSearchViewModel(application: Application) : AndroidViewModel(application) {

    // Can create the DAO instance directly inside the ViewModel because of "AndroidViewModel" and "application" context
    private val bookDao = AppDatabase.getDatabase(application).bookDao()

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    // Rest of the view model code can be copied from workshop


    // implmeent function that will on the click of a book it will save it to room model via dao insert function


}