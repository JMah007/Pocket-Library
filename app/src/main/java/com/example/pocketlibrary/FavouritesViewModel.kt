package com.example.pocketlibrary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavouritesViewModel(application: Application) : AndroidViewModel(application) {

    private val bookDao = AppDatabase.getDatabase(application).bookDao()

    // 1. A private, changeable (Mutable) LiveData that only the ViewModel can modify.
    private val _savedBooks = MutableLiveData<List<Book>>()

    // 2. A public, un-changeable LiveData for the UI to observe.
    // The UI can read this data, but cannot change it.
    val savedBooks: LiveData<List<Book>> = _savedBooks

    // 3. The 'init' block runs once when the ViewModel is first created.
    // This is where we start listening for database updates.
    init {
        // 4. Launch a coroutine in the ViewModel's lifecycle-aware scope.
        // This coroutine will be automatically cancelled when the ViewModel is destroyed.
        viewModelScope.launch {
            // 5. `.collectLatest` listens to the Flow from the database.
            // Every time the 'books' table changes, the code inside these brackets will run
            // with the new, updated list of books.
            bookDao.getAllBooksFlow().collectLatest { bookList ->
                // 6. Update the private MutableLiveData with the new list.
                // We use postValue() because .collect() runs on a background thread.
                _savedBooks.postValue(bookList)
            }
        }
    }
}
