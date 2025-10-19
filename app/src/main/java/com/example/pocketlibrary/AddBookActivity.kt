package com.example.pocketlibrary

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.getValue

class AddBookActivity : AppCompatActivity() {
    private lateinit var titleInput: EditText
    private lateinit var authorInput: EditText
    private lateinit var yearInput: EditText
    private lateinit var takePicBtn: Button
    private lateinit var coverView: ImageView
    private lateinit var saveBtn: Button
    private lateinit var db: AppDatabase
    private lateinit var bookDAO: BookDAO

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
        setContentView(R.layout.activity_add_book)

        titleInput = findViewById(R.id.title_text_input_layout)
        authorInput = findViewById(R.id.author_text_input_layout)
        yearInput = findViewById(R.id.year_text_input_layout)
        coverView = findViewById(R.id.book_cover_imageview)
        saveBtn = findViewById(R.id.saveBtn)
        takePicBtn = findViewById(R.id.takePictureBtn)

        db = AppDatabase.getDatabase(this)
        bookDAO = db.bookDao()

        saveBtn.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val author = authorInput.text.toString().trim()
            val year = yearInput.text.toString().trim()

            if (title.isEmpty() || author.isEmpty() || year.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                val book = Book(
                    id = " ",
                    title = title,
                    author = author,
                    year = year,
                    coverUrl = "", // Couldnt make a url for cover so default " "
                    addedManually = true
                )
                favouritesViewModel.addBook(book)
                Toast.makeText(this@AddBookActivity, "Book saved", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        takePicBtn.setOnClickListener {
            // If already gave camera permission then can skip
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