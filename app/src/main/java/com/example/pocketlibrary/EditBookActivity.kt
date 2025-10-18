package com.example.pocketlibrary

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import coil.load

class EditBookActivity : AppCompatActivity() {
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

        bookCoverInput = findViewById(R.id.book_cover_imageview)
        titleInput = findViewById(R.id.title_text_input_layout)
        authorInput = findViewById(R.id.author_text_input_layout)
        coverView = findViewById<ImageView>(R.id.book_cover_imageview)
        yearInput = findViewById(R.id.year_text_input_layout)
        saveBtn = findViewById(R.id.saveBtn)


        val bookId = intent.getStringExtra("id")
        if (!bookId.isNullOrEmpty()) {
            favouritesViewModel.getBookById(bookId).observe(this) { book ->

                titleInput.setText(book?.title)
                authorInput.setText(book?.author)
                yearInput.setText(book?.year)
                coverView.load(book?.coverUrl) {
                    placeholder(R.drawable.ic_launcher_background) // Optional: Show a default image while loading
                    error(R.drawable.ic_launcher_foreground)       // Optional: Show an error image if the URL is bad or loading fails
                }
            }


            // handler for if user clicks button to take cover photo

            saveBtn.setOnClickListener {
                val updatedBook = Book(bookId, titleInput.text.toString(), authorInput.text.toString(), yearInput.text.toString())

                favouritesViewModel.updateBook(updatedBook)
                finish()

            }
        }

    }

}