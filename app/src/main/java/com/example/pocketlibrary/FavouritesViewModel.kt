package com.example.pocketlibrary

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private val database = FirebaseDatabase.getInstance("https://juba21-default-rtdb.asia-southeast1.firebasedatabase.app")
    .getReference("books")

class FavouritesViewModel(application: Application) : AndroidViewModel(application) {

    private val bookDao = AppDatabase.getDatabase(application).bookDao()

    private val _savedBooks = MutableLiveData<List<Book>>()

    val savedBooks: LiveData<List<Book>> = _savedBooks

    init {
        // Explicitly tell Firebase to connect and start syncing its offline queue.
        FirebaseDatabase.getInstance().goOnline()

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
                        val book = snapshot.getValue(Book::class.java)
                        if (book != null) {
                            firebaseBooks.add(book) // Collects all books that are in firebase
                        }
                    }

                    // Adds all books in firebase local room to display from
                    bookDao.insertAll(firebaseBooks)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FirebaseSync", "Failed to read value.", databaseError.toException())
            }
        }

        // Attach the listener to your database reference.
        database.addValueEventListener(firebaseListener)
    }



    fun deleteBook(id: String) {
        viewModelScope.launch {
            bookDao.deleteBookById(id)
            database.child(id).removeValue()
        }
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
