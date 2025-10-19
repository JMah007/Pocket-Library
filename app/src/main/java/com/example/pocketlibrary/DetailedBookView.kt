package com.example.pocketlibrary

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import coil.load

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
    private lateinit var backBtn: ImageButton
    private lateinit var deleteBtn: Button

    private var bookToShare: Book? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detailed_book_view)

        titleView = findViewById(R.id.title_text_input_layout)
        authorView = findViewById(R.id.author_text_input_layout)
        yearView = findViewById(R.id.year_text_input_layout)
        coverView = findViewById(R.id.book_cover_imageview)
        editBtn = findViewById(R.id.saveBtn)
        deleteBtn = findViewById(R.id.DeleteBtn)
        shareBtn = findViewById(R.id.shareBtn)
        backBtn = findViewById(R.id.backBtn)

        val bookId = intent.getStringExtra("id")
        if (!bookId.isNullOrEmpty()) {
            favouritesViewModel.getBookById(bookId).observe(this) { book ->
                book?.let { currentBook ->
                    bookToShare = currentBook
                    titleView.text = currentBook.title
                    authorView.text = currentBook.author
                    yearView.text = "Published: ${currentBook.year}"
                    coverView.load(currentBook.coverUrl) {
                        placeholder(R.drawable.ic_launcher_background)
                        error(R.drawable.ic_launcher_foreground)
                    }

                    // Determine if edit button should be shown
                    if (currentBook.addedManually) {
                        editBtn.visibility = android.view.View.VISIBLE
                    } else {
                        editBtn.visibility = android.view.View.GONE
                    }

                    editBtn.setOnClickListener {
                        val intent = Intent(this, EditBookActivity::class.java).apply {
                            putExtra("id", currentBook.id)
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

                    backBtn.setOnClickListener {
                        finish()
                    }
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
                Toast.makeText(this, "Permissions required to share book", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun pickContact() {
        val intent =
            Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        startActivityForResult(intent, PICK_CONTACT_REQUEST)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { contactUri ->
                val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val numberIndex =
                            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val phoneNumber = cursor.getString(numberIndex)
                        sendSms(phoneNumber, this)
                    }
                }
            }
        }
    }

    private fun sendSms(phoneNumber: String, context: Context) {

        val smsUri = "smsto:$phoneNumber".toUri()

        // Get the book details for the message body.
        val book = bookToShare ?: return
        val message = "Check out this book: ${book.title} by ${book.author}"

        val intent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
            putExtra("sms_body", message)
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context, "No messaging app found.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
