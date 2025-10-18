package com.example.pocketlibrary

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import coil.load
import com.example.pocketlibrary.databinding.ActivityAddBookBinding
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.launch
import kotlin.getValue

class AddBookActivity : AppCompatActivity() {
    lateinit var bookCoverInput: ImageView
    lateinit var titleInput: EditText
    lateinit var authorInput: EditText
    lateinit var yearInput: EditText
    private lateinit var coverView: ImageView
    lateinit var saveBtn: Button
    private lateinit var db: AppDatabase
    private lateinit var bookDAO: BookDAO

    private val favouritesViewModel: FavouritesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)


        titleInput = findViewById(R.id.title_text_input_layout)
        authorInput = findViewById(R.id.author_text_input_layout)
        yearInput = findViewById(R.id.year_text_input_layout)
        coverView = findViewById<ImageView>(R.id.book_cover_imageview)
        saveBtn = findViewById(R.id.saveBtn)

        db = AppDatabase.getDatabase(this)
        bookDAO = db.bookDao()

        saveBtn.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val author = authorInput.text.toString().trim()
            val year = yearInput.text.toString().trim()
            // Need to make handler to handle taking cover photo and then saving it here (probs not url). The handler can be an onlcicklistner on teh imagecover location

            if (title.isEmpty() || author.isEmpty() || year.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                val book = Book(id = " ", title = title, author = author, year = year, coverUrl = "", addedManually = true)
                favouritesViewModel.addBook(book)
                    Toast.makeText(this@AddBookActivity, "Book saved", Toast.LENGTH_SHORT).show()
                    finish()

            }

        }
    }
}