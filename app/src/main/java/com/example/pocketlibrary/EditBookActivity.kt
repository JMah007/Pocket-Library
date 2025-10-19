package com.example.pocketlibrary

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import coil.load

class EditBookActivity : AppCompatActivity() {
    private lateinit var titleInput: EditText
    private lateinit var authorInput: EditText
    private lateinit var yearInput: EditText
    private lateinit var coverView: ImageView
    private lateinit var saveBtn: Button
    private lateinit var takePicBtn: Button

    private val favouritesViewModel: FavouritesViewModel by viewModels()

    private val takeThumbnail = registerForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        callback = { bitmap: Bitmap? ->
            if (bitmap != null) {
                coverView.setImageBitmap(bitmap)
                // Couldvde made a function that saves it to local device and firebase cloud
            } else {
                Toast.makeText(this, "No image captured", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private val requestPermission = registerForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        callback = { granted ->
            if (granted) takeThumbnail.launch(null)
            else Toast.makeText(this, "Permission denied",
                Toast.LENGTH_SHORT).show()
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_book)

        titleInput = findViewById(R.id.title_text_input_layout)
        authorInput = findViewById(R.id.author_text_input_layout)
        coverView = findViewById<ImageView>(R.id.book_cover_imageview)
        yearInput = findViewById(R.id.year_text_input_layout)
        saveBtn = findViewById(R.id.saveBtn)
        takePicBtn = findViewById(R.id.takePictureBtn)

        val bookId = intent.getStringExtra("id")
        if (!bookId.isNullOrEmpty()) {
            var currentBook: Book? = null

            favouritesViewModel.getBookById(bookId).observe(this) { book ->
                currentBook = book

                titleInput.setText(book?.title)
                authorInput.setText(book?.author)
                yearInput.setText(book?.year)
                coverView.load(book?.coverUrl) {
                    placeholder(R.drawable.ic_launcher_background)
                    error(R.drawable.ic_launcher_foreground)
                }
            }

            saveBtn.setOnClickListener {
                currentBook?.let { bookToUpdate ->
                    val updatedBook = Book(
                        id = bookToUpdate.id,
                        title = titleInput.text.toString(),
                        author = authorInput.text.toString(),
                        year = yearInput.text.toString(),
                        coverUrl = bookToUpdate.coverUrl
                    )

                    favouritesViewModel.updateBook(updatedBook)
                    finish()
                }
            }

            takePicBtn.setOnClickListener {
                // If already granted, skip the prompt
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    takeThumbnail.launch(null)
                } else {
                    requestPermission.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }
}
