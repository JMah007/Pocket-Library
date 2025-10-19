package com.example.pocketlibrary

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import coil.load

class EditBookActivity : AppCompatActivity() {
    // ... (your view declarations)
    lateinit var bookCoverInput: ImageView
    lateinit var titleInput: EditText
    lateinit var authorInput: EditText
    lateinit var yearInput: EditText
    private lateinit var coverView: ImageView
    lateinit var saveBtn: Button
    private val favouritesViewModel: FavouritesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_book)

        // ... (your findViewById calls)
        titleInput = findViewById(R.id.title_text_input_layout)
        authorInput = findViewById(R.id.author_text_input_layout)
        coverView = findViewById<ImageView>(R.id.book_cover_imageview)
        yearInput = findViewById(R.id.year_text_input_layout)
        saveBtn = findViewById(R.id.saveBtn)


        val bookId = intent.getStringExtra("id")
        if (!bookId.isNullOrEmpty()) {
            // 1. Declare a variable to hold the current book.
            //    It's nullable because it will be null until the observer provides it.
            var currentBook: Book? = null

            // 2. The observer fetches the book and populates the 'currentBook' variable.
            favouritesViewModel.getBookById(bookId).observe(this) { book ->
                // Assign the fetched book to our higher-scoped variable
                currentBook = book

                // Now update the UI (this part was already correct)
                titleInput.setText(book?.title)
                authorInput.setText(book?.author)
                yearInput.setText(book?.year)
                coverView.load(book?.coverUrl) {
                    placeholder(R.drawable.ic_launcher_background)
                    error(R.drawable.ic_launcher_foreground)
                }
            }


            // handler for if user clicks button to take cover photo
            // ...

            // 3. The click listener can now access 'currentBook' because it's in the same scope.
            saveBtn.setOnClickListener {
                // Make sure the book has loaded before trying to save
                currentBook?.let { bookToUpdate ->
                    val updatedBook = Book(
                        id = bookToUpdate.id, // Use the non-nullable id
                        title = titleInput.text.toString(),
                        author = authorInput.text.toString(),
                        year = yearInput.text.toString(),
                        coverUrl = bookToUpdate.coverUrl // Use the original URL from the loaded book
                    )

                    favouritesViewModel.updateBook(updatedBook)
                    finish()
                }
            }
        }
    }
}
