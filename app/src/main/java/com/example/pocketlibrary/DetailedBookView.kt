package com.example.pocketlibrary

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import coil.load
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class DetailedBookView : AppCompatActivity() {

    private val favouritesViewModel: FavouritesViewModel by viewModels()
    private lateinit var titleView: TextView
    private lateinit var authorView: TextView
    private lateinit var yearView: TextView
    private lateinit var coverView: ImageView
    private lateinit var editBtn: Button
    private lateinit var deleteBtn: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detailed_book_view)

        titleView = findViewById<TextView>(R.id.title_text_input_layout)
        authorView = findViewById<TextView>(R.id.author_text_input_layout)
        yearView = findViewById<TextView>(R.id.year_text_input_layout)
        coverView = findViewById<ImageView>(R.id.book_cover_imageview)
        editBtn = findViewById<Button>(R.id.saveBtn)
        deleteBtn = findViewById<Button>(R.id.DeleteBtn)

        val bookId = intent.getStringExtra("id")
        if (!bookId.isNullOrEmpty()) {
            favouritesViewModel.getBookById(bookId).observe(this) { book ->

                titleView.text = book?.title
                authorView.text = book?.author
                yearView.text = "Published: ${book?.year}"
                coverView.load(book?.coverUrl) {
                    placeholder(R.drawable.ic_launcher_background) // Optional: Show a default image while loading
                    error(R.drawable.ic_launcher_foreground)       // Optional: Show an error image if the URL is bad or loading fails
                }



                // Restructure to only show this if the book is added manually by checking the boolean status
                editBtn.setOnClickListener {
                    val intent = Intent(this, EditBookActivity::class.java).apply {
                        // Pass the most current data to the edit screen
                        putExtra("id", book?.id)
                    }
                    startActivity(intent)
                }

                deleteBtn.setOnClickListener {
                    favouritesViewModel.deleteBook(bookId)
                    finish()
                }
            }

        }

    }
}