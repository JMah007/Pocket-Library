package com.example.pocketlibrary

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var searchBtn: ImageButton
    private lateinit var addBtn: ImageButton
    private lateinit var searchQuery: SearchView
    private lateinit var booksRecyclerView: RecyclerView
    private lateinit var bookCount: TextView
    private var fullBookList: List<Book> = emptyList()

    private val favouritesViewModel: FavouritesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchBtn = findViewById(R.id.searchActivityButton)
        addBtn = findViewById(R.id.addButton)
        searchQuery = findViewById(R.id.searchView)
        booksRecyclerView = findViewById(R.id.booksRecyclerView)
        bookCount = findViewById(R.id.bookCounter)

        val bookAdapter = BookAdapter { book ->
            if (isTabletLayout()) {
                val fragment = DetailedBookViewFragment.newInstance(book.id)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, fragment)
                    .commit()
            } else {
                val intent = Intent(this, DetailedBookView::class.java).apply {
                    putExtra("id", book.id)
                }
                startActivity(intent)
            }
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

                val filteredList = if (currentSearchText.isEmpty()) {
                    // If search has no query, show the full favourites list
                    fullBookList
                } else {
                    // Otherwise, filter the full list
                    fullBookList.filter { book ->
                        book.title.contains(currentSearchText, ignoreCase = true) ||
                                book.author.contains(currentSearchText, ignoreCase = true)
                    }
                }
                bookAdapter.setData(filteredList)
                updateBookCount(bookAdapter.itemCount)
                return true
            }
        })
    }

    private fun isTabletLayout(): Boolean {
        return findViewById<View?>(R.id.detailContainer) != null
    }

    private fun updateBookCount(count: Int) {
        bookCount.text = "Total books: $count"

    }
}
