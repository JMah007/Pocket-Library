package com.example.pocketlibrary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.database.FirebaseDatabase

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private val database = FirebaseDatabase.getInstance("https://juba21-default-rtdb.asia-southeast1.firebasedatabase.app")
    .getReference("books")

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
        // Also save all books in local to firebase and catch if it fails
        viewModelScope.launch {
            bookDao.getAllBooksFlow().collectLatest { bookList ->
                _savedBooks.postValue(bookList)
            }
        }
    }

    fun deleteBook(id: String) {
        viewModelScope.launch {
            bookDao.deleteBookById(id)
            database.child(id).removeValue()
        }
    }

    // This is the correct, asynchronous way to provide a single book.
    // It returns a LiveData object that the UI can observe.
    fun getBookById(id: String): LiveData<Book?> {
        return bookDao.getBookByIdLive(id)
    }


    fun updateBook(book: Book){
        viewModelScope.launch {
            bookDao.update(book)
            database.child(book.id).setValue(book)
        }
    }

    fun addBook(book: Book) {
        viewModelScope.launch {
            // 2. Ask Firebase to generate a new, unique ID for us first.
            val newBookRef = database.push()
            val newId = newBookRef.key // This is the unique ID string from Firebase

            if (newId != null) {
                // 3. Create a copy of the book with the new Firebase ID.
                val bookWithSyncedId = book.copy(id = newId)

                // 4. Save this complete object to Firebase.
                newBookRef.setValue(bookWithSyncedId)

                // 5. Save the EXACT SAME object (with the synced ID) to Room.
                bookDao.insert(bookWithSyncedId)
            }
        }
    }


}
