package com.example.pocketlibrary

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.SearchView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var searchBtn: ImageButton
    private lateinit var addBtn: ImageButton
    private lateinit var searchQuery: SearchView
    private lateinit var booksRecyclerView: RecyclerView

    // This is the only instance of the ViewModel you need.
    // The 'by viewModels()' delegate handles its lifecycle correctly.
    private val favouritesViewModel: FavouritesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --- 1. Find all views by their ID ---
        searchBtn = findViewById(R.id.searchActivityButton)
        addBtn = findViewById(R.id.addButton)
        searchQuery = findViewById(R.id.searchView)
        booksRecyclerView = findViewById(R.id.booksRecyclerView)

        // --- 2. Remove direct database access to prevent crash ---
        // db = AppDatabase.getDatabase(this) // <-- DELETED
        // bookDAO = db.bookDao()              // <-- DELETED
        // --- 3. Setup the Adapter and RecyclerView ---
        val bookAdapter = BookAdapter()
        // FIX: Use GridLayoutManager and specify 3 columns (spanCount).
        booksRecyclerView.layoutManager = GridLayoutManager(this, 3)
        booksRecyclerView.adapter = bookAdapter


        // --- 4. THIS IS THE FIX: Observe the ViewModel for data ---
        // This connects your UI to your data layer safely.
        // The code inside the brackets runs on the main thread automatically
        // when the data is ready.
        favouritesViewModel.savedBooks.observe(this) { books ->
            // The 'books' variable is the List<Book> from your database.
            // When we receive it, we give it to the adapter to display.
            bookAdapter.setData(books)
        }

        // --- 5. Set up button click listeners ---
        addBtn.setOnClickListener {
            val intent = Intent(this, AddBookActivity::class.java)
            startActivity(intent)
        }

        searchBtn.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }
    }
}
