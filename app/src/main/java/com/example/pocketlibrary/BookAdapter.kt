package com.example.pocketlibrary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import coil.load
import androidx.recyclerview.widget.RecyclerView


class BookAdapter (
    private val onClickBook: (Book) -> Unit,
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>(){

    private var books: List<Book> = emptyList()

    fun setData(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }

    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.bookTitle)
        val author: TextView = view.findViewById(R.id.bookAuthor)
        val cover: ImageView = view.findViewById(R.id.book_cover_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]

        holder.title.text = book.title
        holder.author.text = book.author
        holder.cover.load(book.coverUrl) {
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_foreground)
        }
        holder.itemView.setOnClickListener {
            onClickBook(book)
        }
    }

    override fun getItemCount(): Int {
        return books.size
    }
}
