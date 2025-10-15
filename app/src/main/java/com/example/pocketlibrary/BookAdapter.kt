package com.example.pocketlibrary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.semantics.error
import androidx.recyclerview.widget.RecyclerView


class BookAdapter (
    private val onClickBook: (Book) -> Unit,
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>(){

    private var books: List<Book> = emptyList()

    // This function is how your Activity gives the list of books to the adapter.
    // Renamed for clarity.
    fun setData(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged() // Refreshes the entire list
    }

    // This class holds the view references for a single item in your list.
    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Make sure the IDs in your R.id.____ match your item_book.xml file exactly
        val title: TextView = view.findViewById(R.id.bookTitle)
        val author: TextView = view.findViewById(R.id.bookAuthor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    // This function connects your data to your ViewHolder's views.
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]

        // Set the text for title and author
        holder.title.text = book.title
        holder.author.text = book.author

        holder.itemView.setOnClickListener {
            onClickBook(book)
        }


    }

    override fun getItemCount(): Int {
        return books.size
    }
}
