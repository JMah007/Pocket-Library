package com.example.pocketlibrary

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDAO {


    @Insert
    suspend fun insert(book: Book)

    @Update
    suspend fun update(book: Book)

    @Delete
    suspend fun delete(book: Book)


    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBookById(id: String)





    // This is a "one-shot" read. It fetches the data once and then it's done.
// 1. REMOVE the 'suspend' keyword. Flow queries are not suspend functions.
// 2. CHANGE the return type to Flow<List<Book>>.
    @Query("SELECT * FROM books")
    fun getAllBooksFlow(): Flow<List<Book>>


    @Query("SELECT * FROM books WHERE title LIKE :title")
    suspend fun getBooksByTitle(title: String): List<Book>

    @Query("SELECT * FROM books WHERE author LIKE :author")
    suspend fun getBooksByAuthor(author: String): List<Book>


    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: String): Book? // Returns a nullable Book


    @Query("SELECT * FROM books WHERE id = :id")
    fun getBookByIdLive(id: String): LiveData<Book?>





}