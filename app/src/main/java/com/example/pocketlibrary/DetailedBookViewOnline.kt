package com.example.pocketlibrary

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import androidx.lifecycle.lifecycleScope
import coil.load // Import Coil for image loading
import com.google.android.material.textfield.TextInputEditText // Import TextInputEditText
import kotlinx.coroutines.launch

class DetailedBookViewOnline : AppCompatActivity() {
    private val favouritesViewModel: FavouritesViewModel by viewModels()

    // 1. FIX: Change TextView to the correct type
    private lateinit var titleView: TextView
    private lateinit var coverView: ImageView
    private lateinit var authorView: TextView
    private lateinit var yearView: TextView
    private lateinit var addDeleteBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detailed_book_view_online)

        // 2. FIX: Use the correct view type and ID (VERIFY IDs IN YOUR XML)
        // I am guessing the IDs for the editable fields.
        titleView = findViewById(R.id.title_text_input_layout) // Example ID, check your XML
        authorView = findViewById(R.id.author_text_input_layout) // Example ID, check your XML
        yearView = findViewById(R.id.year_text_input_layout) // Example ID, check your XML
        addDeleteBtn = findViewById(R.id.addBtn)
        coverView = findViewById(R.id.book_cover_imageview)

        // Get data from intent
        val title = intent.getStringExtra("title")
        val author = intent.getStringExtra("author")
        val coverUrl = intent.getStringExtra("coverUrl")
        val year = intent.getStringExtra("publishYear")

        // Populate views
        titleView.setText(title) // Use .setText() for EditTexts
        authorView.setText(author)
        yearView.setText(year)
        coverView.load(coverUrl) {
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_foreground)
        }

        // 3. FIX: Check initial book status in a coroutine to set button text
        lifecycleScope.launch {
            val existingBook = favouritesViewModel.getBookByDetails(title ?: "", author ?: "")
            if (existingBook != null) {
                addDeleteBtn.text = "Remove From Favourites"
            } else {
                addDeleteBtn.text = "Add To Favourites"
            }
        }

        // 4. FIX: Correct the click listener logic entirely
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
