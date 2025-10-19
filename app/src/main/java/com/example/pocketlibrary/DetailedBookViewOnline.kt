package com.example.pocketlibrary

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load // Import Coil for image loading
import kotlinx.coroutines.launch

class DetailedBookViewOnline : AppCompatActivity() {
    private val favouritesViewModel: FavouritesViewModel by viewModels()

    private lateinit var titleView: TextView
    private lateinit var coverView: ImageView
    private lateinit var authorView: TextView
    private lateinit var yearView: TextView
    private lateinit var addDeleteBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detailed_book_view_online)

        titleView = findViewById(R.id.title_text_input_layout)
        authorView = findViewById(R.id.author_text_input_layout)
        yearView = findViewById(R.id.year_text_input_layout)
        addDeleteBtn = findViewById(R.id.addBtn)
        coverView = findViewById(R.id.book_cover_imageview)

        val title = intent.getStringExtra("title")
        val author = intent.getStringExtra("author")
        val coverUrl = intent.getStringExtra("coverUrl")
        val year = intent.getStringExtra("publishYear")

        titleView.setText(title)
        authorView.setText(author)
        yearView.setText(year)
        coverView.load(coverUrl) {
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_foreground)
        }

        lifecycleScope.launch {
            val existingBook = favouritesViewModel.getBookByDetails(title ?: "", author ?: "")
            if (existingBook != null) {
                addDeleteBtn.text = "Remove From Favourites"
            } else {
                addDeleteBtn.text = "Add To Favourites"
            }
        }

        addDeleteBtn.setOnClickListener {
            lifecycleScope.launch {
                val bookInDb = favouritesViewModel.getBookByDetails(title ?: "", author ?: "")

                if (bookInDb != null) {
                    // If book exists, delete it
                    favouritesViewModel.deleteBook(bookInDb.id)
                    addDeleteBtn.text = "Add To Favourites"
                } else {
                    // If book doesn't exist, add it
                    val newBook = Book(
                        id = " ", // Firebase will generate the ID
                        title = title ?: "No title",
                        author = author ?: "No author",
                        year = year ?: "NA",
                        coverUrl = coverUrl ?: "",
                        addedManually = false
                    )
                    favouritesViewModel.addBook(newBook)
                    addDeleteBtn.text = "Remove From Favourites"
                }
            }
        }
    }
}
