package com.example.pocketlibrary

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.load
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.io.path.exists

class EditBookActivity : AppCompatActivity() {
    // ... (your view declarations)
    lateinit var bookCoverInput: ImageView
    lateinit var titleInput: EditText
    lateinit var authorInput: EditText
    lateinit var yearInput: EditText
    private lateinit var coverView: ImageView
    lateinit var saveBtn: Button
    lateinit var takePicBtn: Button
    private val favouritesViewModel: FavouritesViewModel by viewModels()

    private var coverPublicUrl: String? = null

    private val takeThumbnail = registerForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        callback = { bitmap: Bitmap? ->
            if (bitmap != null) {
                coverView.setImageBitmap(bitmap) // Sets image i edit page straight away for preview
                // call method that uploads to firebase cloud
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

        // ... (your findViewById calls)
        titleInput = findViewById(R.id.title_text_input_layout)
        authorInput = findViewById(R.id.author_text_input_layout)
        coverView = findViewById<ImageView>(R.id.book_cover_imageview)
        yearInput = findViewById(R.id.year_text_input_layout)
        saveBtn = findViewById(R.id.saveBtn)
        takePicBtn = findViewById(R.id.takePictureBtn)



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

                    val finalCoverUrl = coverPublicUrl ?: bookToUpdate.coverUrl
                    val updatedBook = Book(
                        id = bookToUpdate.id, // Use the non-nullable id
                        title = titleInput.text.toString(),
                        author = authorInput.text.toString(),
                        year = yearInput.text.toString(),
                        coverUrl = finalCoverUrl // Use the original URL from the loaded book
                    )

                    favouritesViewModel.updateBook(updatedBook)
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

    private fun uploadImageToFirebaseStorage(bitmap: Bitmap) {
        val localFileUri = saveBitmapToCache(bitmap)
        if (localFileUri == null) {
            Toast.makeText(this, "Failed to create local file", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Uploading cover...", Toast.LENGTH_SHORT).show()
        val storageRef = Firebase.storage.reference
        // Create a unique path for the image
        val imageRef = storageRef.child("covers/${System.currentTimeMillis()}.jpg")

        imageRef.putFile(localFileUri)
            .addOnSuccessListener {
                // Image uploaded successfully, now get the download URL
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    coverPublicUrl = downloadUri.toString()
                    Toast.makeText(this, "Upload complete!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("FirebaseStorage", "Upload failed", e)
            }
    }

    // --- NEW HELPER FUNCTION TO SAVE BITMAP LOCALLY ---
    private fun saveBitmapToCache(bitmap: Bitmap): Uri? {
        return try {
            val imageDir = File(cacheDir, "images")
            if (!imageDir.exists()) imageDir.mkdirs()

            val imageFile = File(imageDir, "${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()

            FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", imageFile)
        } catch (e: IOException) {
            Log.e("SaveBitmap", "Error saving bitmap to cache", e)
            null
        }
    }


}
