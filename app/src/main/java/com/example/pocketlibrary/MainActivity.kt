package com.example.pocketlibrary

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var searchBtn: ImageButton
    private lateinit var addBtn: ImageButton
    private lateinit var searchQuery: SearchView
    private lateinit var booksRecyclerView: RecyclerView
    private lateinit var bookCount: TextView

    private var fullBookList: List<Book> = emptyList()

    // This is the only instance of the ViewModel you need.
    // The 'by viewModels()' delegate handles its lifecycle correctly.
    private val favouritesViewModel: FavouritesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchBtn = findViewById(R.id.searchActivityButton)
        addBtn = findViewById(R.id.addButton)
        searchQuery = findViewById(R.id.searchView)
        booksRecyclerView = findViewById(R.id.booksRecyclerView)
        bookCount = findViewById(R.id.bookCounter)


        // --- 2. Remove direct database access to prevent crash ---
        // db = AppDatabase.getDatabase(this) // <-- DELETED
        // bookDAO = db.bookDao()              // <-- DELETED
        // --- 3. Setup the Adapter and RecyclerView ---
        val bookAdapter = BookAdapter { book ->
            // Handle click on book here
            val intent = Intent(this, DetailedBookView::class.java).apply {
                putExtra("id", book.id)

            }
            startActivity(intent)
        }


        booksRecyclerView.layoutManager = GridLayoutManager(this, 3)
        booksRecyclerView.adapter = bookAdapter


        favouritesViewModel.savedBooks.observe(this) { books ->
            this.fullBookList = books
            bookAdapter.setData(books)
            updateBookCount(bookAdapter.itemCount)
        }

        addBtn.setOnClickListener {
            val intent = Intent(this, AddBookActivity::class.java)
            startActivity(intent)
        }

        searchBtn.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }



        searchQuery.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // dont implement
                return false

            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val currentSearchText = newText.orEmpty()

                // --- 4. FIX: Implement the filtering logic ---
                val filteredList = if (currentSearchText.isEmpty()) {
                    // If search is empty, show the full list
                    fullBookList
                } else {
                    // Otherwise, filter the full list
                    fullBookList.filter { book ->
                        // Check if the book's title or author contains the search text (case-insensitive)
                        book.title.contains(currentSearchText, ignoreCase = true) ||
                                book.author.contains(currentSearchText, ignoreCase = true)
                    }
                }
                // Update the adapter with the new filtered list
                bookAdapter.setData(filteredList)
                updateBookCount(bookAdapter.itemCount)
                return true
            }
        })

    }

    private fun updateBookCount(count: Int) {
        bookCount.text = "Total books: $count"

    }
}
