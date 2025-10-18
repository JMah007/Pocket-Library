package com.example.pocketlibrary

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load

class DetailedBookViewFragment : Fragment() {

    private val PICK_CONTACT_REQUEST = 1001
    private val PERMISSIONS_REQUEST_CODE = 2001

    private val favouritesViewModel: FavouritesViewModel by viewModels(ownerProducer = { requireActivity() })

    private lateinit var titleView: TextView
    private lateinit var authorView: TextView
    private lateinit var yearView: TextView
    private lateinit var coverView: ImageView
    private lateinit var editBtn: Button
    private lateinit var shareBtn: Button
    private lateinit var deleteBtn: Button

    private var bookToShare: Book? = null
    private var currentBookId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentBookId = arguments?.getString("id")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Using the same layout as the Activity
        return inflater.inflate(R.layout.activity_detailed_book_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleView = view.findViewById(R.id.title_text_input_layout)
        authorView = view.findViewById(R.id.author_text_input_layout)
        yearView = view.findViewById(R.id.year_text_input_layout)
        coverView = view.findViewById(R.id.book_cover_imageview)
        editBtn = view.findViewById(R.id.saveBtn)
        deleteBtn = view.findViewById(R.id.DeleteBtn)
        shareBtn = view.findViewById(R.id.shareBtn)

        currentBookId?.let { bookId ->
            favouritesViewModel.getBookById(bookId).observe(viewLifecycleOwner) { book ->
                if (book != null) {

                    bookToShare = book
                    titleView.text = book.title
                    authorView.text = book.author
                    yearView.text = "Published: ${book.year}"
                    coverView.load(book.coverUrl) {
                        placeholder(R.drawable.ic_launcher_background)
                        error(R.drawable.ic_launcher_foreground)
                    }

                    editBtn.setOnClickListener {
                        val intent = Intent(requireContext(), EditBookActivity::class.java).apply {
                            putExtra("id", book.id)
                        }
                        startActivity(intent)
                    }

                    shareBtn.setOnClickListener {
                        checkPermissionsAndPickContact()
                    }

                    deleteBtn.setOnClickListener {
                        favouritesViewModel.deleteBook(bookId)
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        }
    }

    private fun checkPermissionsAndPickContact() {
        val context = requireContext()
        val hasReadContacts = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        val hasSendSms = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        val permissionsToRequest = mutableListOf<String>()
        if (!hasReadContacts) permissionsToRequest.add(Manifest.permission.READ_CONTACTS)
        if (!hasSendSms) permissionsToRequest.add(Manifest.permission.SEND_SMS)

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissions(
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
                Toast.makeText(requireContext(), "Permissions required to share book", Toast.LENGTH_SHORT).show()
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
                requireContext().contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
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
            Toast.makeText(requireContext(), "Book successfully shared via SMS!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance(bookId: String): DetailedBookViewFragment {
            val fragment = DetailedBookViewFragment()
            val args = Bundle().apply {
                putString("id", bookId)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
