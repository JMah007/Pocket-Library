package com.example.pocketlibrary

import android.app.Application
import android.util.Log
import androidx.compose.ui.input.key.key
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


private val database = Firebase.database("https://juba21-default-rtdb.asia-southeast1.firebasedatabase.app")
    .getReference("books")

class FavouritesViewModel(application: Application) : AndroidViewModel(application) {

    private val bookDao = AppDatabase.getDatabase(application).bookDao()

    private val _savedBooks = MutableLiveData<List<Book>>()
    val savedBooks: LiveData<List<Book>> = _savedBooks

    init {
        // Explicitly tell Firebase to connect and start syncing its offline queue.
        // This might not be strictly necessary with a persistent listener, but it doesn't hurt.
        FirebaseDatabase.getInstance("https://juba21-default-rtdb.asia-southeast1.firebasedatabase.app").goOnline()

        // Start observing the local Room database for instant UI updates.
        viewModelScope.launch {
            bookDao.getAllBooksFlow().collectLatest { bookList ->
                _savedBooks.postValue(bookList)
            }
        }

        // Create the persistent listener that keeps Room in sync with Firebase.
        val firebaseListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                viewModelScope.launch {
                    val firebaseBooks = mutableListOf<Book>()
                    for (snapshot in dataSnapshot.children) {
                        // Using try-catch to prevent crashes from malformed data in Firebase
                        try {
                            val book = snapshot.getValue(Book::class.java)
                            if (book != null) {
                                firebaseBooks.add(book)
                            }
                        } catch (e: Exception) {
                            Log.e("FirebaseSync", "Error parsing book data: ${snapshot.key}", e)
                        }
                    }

                    // Replace local data with the fresh data from Firebase
                    if (firebaseBooks.isNotEmpty()) {
                        bookDao.insertAll(firebaseBooks)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FirebaseSync", "Failed to read value.", databaseError.toException())
            }
        }

        // Attach the listener to your database reference.
        database.addValueEventListener(firebaseListener)
    }

    // The rest of your ViewModel is well-structured and looks good!
    fun deleteBook(id: String) {
        viewModelScope.launch {
            bookDao.deleteBookById(id)
            database.child(id).removeValue()
        }
    }

    suspend fun getBookByDetails(title: String, author: String): Book? {
        return bookDao.getBookByDetails(title, author)
    }


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
            val newBookRef = database.push()
            val newId = newBookRef.key

            if (newId != null) {
                val bookWithSyncedId = book.copy(id = newId)
                newBookRef.setValue(bookWithSyncedId)
                bookDao.insert(bookWithSyncedId) // This is handled by the ValueEventListener
            }
        }
    }
}
