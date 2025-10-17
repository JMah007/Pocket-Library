package com.example.pocketlibrary

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import coil.load
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.pocketlibrary.databinding.ActivityDetailedBookViewBinding

class DetailedBookView : AppCompatActivity() {

    private val PICK_CONTACT_REQUEST = 1001
    private val PERMISSIONS_REQUEST_CODE = 2001

    private val favouritesViewModel: FavouritesViewModel by viewModels()
    private lateinit var titleView: TextView
    private lateinit var authorView: TextView
    private lateinit var yearView: TextView
    private lateinit var coverView: ImageView
    private lateinit var editBtn: Button
    private lateinit var shareBtn: Button
    private lateinit var deleteBtn: Button
    private var bookToShare: Book? = null

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
        shareBtn = findViewById(R.id.shareBtn)

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

                shareBtn.setOnClickListener {
                    checkPermissionsAndPickContact()
                }

                deleteBtn.setOnClickListener {
                    favouritesViewModel.deleteBook(bookId)
                    finish()
                }
            }
        }
    }

    private fun checkPermissionsAndPickContact() {
        val hasReadContacts = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        val hasSendSms = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        val permissionsToRequest = mutableListOf<String>()
        if (!hasReadContacts) permissionsToRequest.add(Manifest.permission.READ_CONTACTS)
        if (!hasSendSms) permissionsToRequest.add(Manifest.permission.SEND_SMS)

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
        } else {
            pickContact()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                pickContact()
            } else {
                Toast.makeText(this, "Permissions required to share book", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickContact() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        startActivityForResult(intent, PICK_CONTACT_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { contactUri ->
                val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val phoneNumber = cursor.getString(numberIndex)
                        sendSms(phoneNumber)
                    }
                }
            }
        }
    }

    private fun sendSms(phoneNumber: String) {
        val book = bookToShare ?: return
        val message = "Check out this book: ${book.title} by ${book.author}"

        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(this, "Book shared via SMS!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}