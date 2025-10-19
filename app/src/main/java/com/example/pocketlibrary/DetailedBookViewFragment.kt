package com.example.pocketlibrary

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
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
    private var currentBookId: String? = null

    private val pickContactLauncher = registerForActivityResult(ActivityResultContracts.PickContact()) { contactUri ->
        contactUri?.let { uri ->
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
            requireContext().contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val phoneNumber = cursor.getString(numberIndex)
                    sendSms(phoneNumber)
                }
            }
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
    ): View? = inflater.inflate(R.layout.activity_detailed_book_view, container, false)

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

                    shareBtn.setOnClickListener { checkPermissionsAndPickContact() }

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
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_CONTACTS)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissions(permissionsToRequest.toTypedArray(), 2001)
        } else {
            pickContactLauncher.launch(null)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2001 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            pickContactLauncher.launch(null)
        } else {
            Toast.makeText(requireContext(), "Permission required to share book", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendSms(phoneNumber: String) {
        val book = bookToShare ?: return
        val message = "Check out this book: ${book.title} by ${book.author}"
        val intent = Intent(Intent.ACTION_SENDTO, "smsto:$phoneNumber".toUri()).apply {
            putExtra("sms_body", message)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No messaging app found.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance(bookId: String): DetailedBookViewFragment {
            return DetailedBookViewFragment().apply {
                arguments = Bundle().apply { putString("id", bookId) }
            }
        }
    }
}

