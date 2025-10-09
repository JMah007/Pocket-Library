package com.example.pocketlibrary

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.pocketlibrary.databinding.ActivityAddBookBinding
import kotlinx.coroutines.launch

class AddBookActivity : AppCompatActivity() {
    lateinit var bookCoverInput: ImageView
    lateinit var titleInput: EditText
    lateinit var authorInput: EditText
    lateinit var yearInput: EditText
    lateinit var saveBtn: Button
    private lateinit var db: AppDatabase
    private lateinit var bookDAO: BookDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_book)

        titleInput = findViewById(R.id.title_text_input_layout)
        authorInput = findViewById(R.id.author_text_input_layout)
        yearInput = findViewById(R.id.year_text_input_layout)
        saveBtn = findViewById(R.id.saveBtn)

        db = AppDatabase.getDatabase(this)
        bookDAO = db.bookDao()

        saveBtn.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val author = authorInput.text.toString().trim()
            val year = yearInput.text.toString().trim()

            if (title.isEmpty() || author.isEmpty() || year.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                val book = Book(title = title, author = author, year = year, addedManually = true)
                lifecycleScope.launch {
                    db.bookDao().insert(book)
                    Toast.makeText(this@AddBookActivity, "Book saved", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            // Need to also add implmentation that will add it to firebase


        }
    }
}