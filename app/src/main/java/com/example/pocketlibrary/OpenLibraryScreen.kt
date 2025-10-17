package com.example.pocketlibrary

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext



@Composable
fun OpenLibraryScreen(vm: BooksSearchViewModel = viewModel()) {
    val state by vm.state.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = state.query,
            onValueChange = vm::updateQuery,
            label = { Text("Search Open Library") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            // 1. CORRECTED: The function is searchBooks(), not search().
            keyboardActions = KeyboardActions(
                onSearch = { vm.searchBooks() })
        )

        Spacer(Modifier.height(12.dp))

        Box(Modifier.fillMaxSize()) {
            when {
                state.loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                state.error != null -> {
                    Text(
                        text = state.error ?: "Error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Show a helpful message when the screen first loads
                state.results.isEmpty() && state.query.isBlank() -> {
                    Text("Search for a book to begin", modifier = Modifier.align(Alignment.Center))
                }

                state.results.isEmpty() && state.query.isNotEmpty() && !state.loading -> {
                    Text("No results found", modifier = Modifier.align(Alignment.Center))
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        verticalArrangement = Arrangement.spacedBy(8.dp) // Add spacing between items
                    ) {
                        items(state.results) { book ->
                            val context = LocalContext.current
                            Card(Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val intent = Intent(context, DetailedBookViewOnline::class.java)
                                    intent.putExtra("title", book.title)
                                    intent.putExtra("author", book.author)
                                    intent.putExtra("coverUrl", book.coverUrl)
                                    intent.putExtra("publishYear", book.publishYear.toString())
                                    context.startActivity(intent)
                                }) {

                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 2. UNCOMMENTED: The AsyncImage is now active.
                                    AsyncImage(
                                        model = book.coverUrl,
                                        contentDescription = "Cover for ${book.title}",
                                        contentScale = ContentScale.Crop, // Crop is often better for uniform size
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(120.dp)
                                    )

                                    Spacer(Modifier.width(16.dp))

                                    Column {
                                        Text(
                                            text = book.title,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "by ${book.author}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "Published ${book.publishYear}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
