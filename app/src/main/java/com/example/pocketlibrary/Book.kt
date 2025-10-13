package com.example.pocketlibrary

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "author") val author: String,
    @ColumnInfo(name = "year") val year: String,
    @ColumnInfo(name = "addedManually") var addedManually: Boolean = false,
    @ColumnInfo(name = "coverUrl") val coverUrl: String? = null
)
