package com.example.pocketlibrary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun BookSearchScreen(vm: BooksSearchViewModel = viewModel()) {
    val results by vm.results.collectAsState()
    var query by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { newQuery ->
                query = newQuery

                searchJob = coroutineScope.launch {
                    if (query.isNotBlank()) {
                        vm.search(query)
                    } else {
                        vm.clearResults()
                    }
                }
            },
            label = { Text("Search by title or author") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Results List ---
        if (results.isEmpty()) {
            Text("No results yet", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(results) { book ->
                    BookResultItem(book)
                    Divider()
                }
            }
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
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(book.coverUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Book Cover",
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(text = book.title, style = MaterialTheme.typography.titleMedium)
            Text(text = "Author: ${book.author}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Year: ${book.year}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}



