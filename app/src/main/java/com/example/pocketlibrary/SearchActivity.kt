package com.example.pocketlibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val vm: BooksSearchViewModel = viewModel()
            val results by vm.results.collectAsState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Search Bar
                SearchBar { query ->
                    vm.search(query)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Results List
                LazyColumn {
                    items(results) { book ->
                        BookResultItem(book)
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(onSearch: (String) -> Unit) {
    var text by remember { mutableStateOf(TextFieldValue("")) }

    Row(Modifier.fillMaxWidth()) {
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search by title or author") }
        )
        Spacer(Modifier.width(8.dp))
        Button(onClick = { onSearch(text.text) }) {
            Text("Search")
        }
    }
}

@Composable
fun BookResultItem(book: Book) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Book Cover
        AsyncImage(
            model = book.coverUrl,
            contentDescription = "Book Cover",
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Book Info
        Column {
            Text(text = book.title, style = MaterialTheme.typography.titleMedium)
            Text(text = "Author: ${book.author}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Year: ${book.year}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
