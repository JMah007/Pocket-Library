package com.example.pocketlibrary

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDAO {


    @Insert
    suspend fun insert(book: Book)

    @Insert(onConflict = OnConflictStrategy.REPLACE) // This makes sure duplicates are replaced causing no issue
    suspend fun insertAll(books: List<Book>)

    @Update
    suspend fun update(book: Book)

    @Delete
    suspend fun delete(book: Book)

    @Query("DELETE FROM books")
    suspend fun deleteAll()


    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBookById(id: String)

    @Query("SELECT * FROM books WHERE title = :title AND author = :author")
    suspend fun getBookByDetails(title: String, author: String): Book?

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