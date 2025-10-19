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
    lateinit var bookCoverInput: ImageView
    lateinit var titleInput: EditText
    lateinit var authorInput: EditText
    lateinit var yearInput: EditText
    lateinit var takePicBtn: Button
    private lateinit var coverView: ImageView
    lateinit var saveBtn: Button
    private lateinit var db: AppDatabase
    private lateinit var bookDAO: BookDAO

    private val favouritesViewModel: FavouritesViewModel by viewModels()

    private val takeThumbnail = registerForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        callback = { bitmap: Bitmap? ->
            if (bitmap != null) {
                coverView.setImageBitmap(bitmap)
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
        coverView = findViewById<ImageView>(R.id.book_cover_imageview)
        saveBtn = findViewById(R.id.saveBtn)
        takePicBtn = findViewById<Button>(R.id.takePictureBtn)

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
                val book = Book(
                    id = " ",
                    title = title,
                    author = author,
                    year = year,
                    coverUrl = "",
                    addedManually = true
                )
                favouritesViewModel.addBook(book)
                Toast.makeText(this@AddBookActivity, "Book saved", Toast.LENGTH_SHORT).show()
                finish()

            }

        }

        takePicBtn.setOnClickListener {
            // If already granted, skip the prompt
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.CAMERA // FIX: Changed to CAMERA permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // You already have permission, launch the camera
                takeThumbnail.launch(null)
            } else {
                // You don't have permission, request it.
                // NOTE: 'requestPermission' is already configured to call 'takeThumbnail.launch(null)' if granted.
                requestPermission.launch(Manifest.permission.CAMERA) // FIX: Changed to CAMERA permission
            }
        }

    }
}