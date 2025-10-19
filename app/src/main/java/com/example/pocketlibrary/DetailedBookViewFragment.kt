package com.example.pocketlibrary

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load

class DetailedBookViewFragment : Fragment() {

    private val favouritesViewModel: FavouritesViewModel by viewModels(ownerProducer = { requireActivity() })

    private lateinit var titleView: TextView
    private lateinit var authorView: TextView
    private lateinit var yearView: TextView
    private lateinit var coverView: ImageView
    private lateinit var editBtn: Button
    private lateinit var shareBtn: Button
    private lateinit var deleteBtn: Button

    private var bookToShare: Book? = null
    private var currentBookId: String? = null //hello

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val readGranted = results[Manifest.permission.READ_CONTACTS] == true
        val smsGranted = results[Manifest.permission.SEND_SMS] == true

        if (readGranted && smsGranted) {
            pickContactLauncher.launch(null)
        } else {
            Toast.makeText(requireContext(), "Permissions required to share book", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickContactLauncher = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) { contactUri: Uri? ->
        if (contactUri != null) {
            loadPhoneNumberAndSendSms(contactUri)
        } else {
            Toast.makeText(requireContext(), "No contact selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentBookId = arguments?.getString("id")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        if (hasReadContacts && hasSendSms) {
            pickContactLauncher.launch(null)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.SEND_SMS
                )
            )
        }
    }

    private fun loadPhoneNumberAndSendSms(contactUri: Uri) {
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)

        requireContext().contentResolver.query(
            contactUri, projection, null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val numberIndex =
                    cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val phoneNumber = cursor.getString(numberIndex)
                sendSms(phoneNumber)
            } else {
                Toast.makeText(requireContext(), "No phone number found", Toast.LENGTH_SHORT).show()
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
