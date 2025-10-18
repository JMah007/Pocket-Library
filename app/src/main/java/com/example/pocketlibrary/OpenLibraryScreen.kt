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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun OpenLibraryScreen(vm: BooksSearchViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val tablet = screenWidthDp >= 600

    val context = LocalContext.current
    var selectedBook by remember { mutableStateOf<Books?>(null) }

    Row(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(if (tablet) 0.5f else 1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = vm::updateQuery,
                label = { Text("Search Open Library") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardActions = KeyboardActions(onSearch = { vm.searchBooks() })
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
                    state.results.isEmpty() && state.query.isBlank() -> {
                        Text("Search for a book to begin", Modifier.align(Alignment.Center))
                    }
                    state.results.isEmpty() && state.query.isNotEmpty() && !state.loading -> {
                        Text("No results found", Modifier.align(Alignment.Center))
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(1),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.results) { book ->
                                Card(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (tablet) {
                                                selectedBook = book
                                            } else {
                                                val intent = Intent(context, DetailedBookViewOnline::class.java)
                                                intent.putExtra("title", book.title)
                                                intent.putExtra("author", book.author)
                                                intent.putExtra("coverUrl", book.coverUrl)
                                                intent.putExtra("publishYear", book.publishYear.toString())
                                                context.startActivity(intent)
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = book.coverUrl,
                                            contentDescription = "Cover for ${book.title}",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .width(80.dp)
                                                .height(120.dp)
                                        )
                                        Spacer(Modifier.width(16.dp))
                                        Column {
                                            Text(text = book.title, style = MaterialTheme.typography.titleMedium)
                                            Spacer(Modifier.height(4.dp))
                                            Text(text = "by ${book.author}", style = MaterialTheme.typography.bodySmall)
                                            Spacer(Modifier.height(4.dp))
                                            Text(text = "Published ${book.publishYear}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Right pane: Tablet detail view
        if (tablet) {
            Box(
                Modifier
                    .weight(0.5f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                selectedBook?.let { book ->
                    DetailedBookViewOnlineTablet(book)
                } ?: run {
                    Text("Select a book to see details", Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun DetailedBookViewOnlineTablet(
    book: Books,
    favouritesViewModel: FavouritesViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    var isFavourite by remember { mutableStateOf(false) }

    // Check if the book is in favourites on composition
    LaunchedEffect(book) {
        val existing = favouritesViewModel.getBookByDetails(book.title, book.author)
        isFavourite = existing != null
    }

    Column(Modifier.fillMaxSize()) {
        AsyncImage(
            model = book.coverUrl,
            contentDescription = "Cover for ${book.title}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("Title: ${book.title}", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text("Author: ${book.author}", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text("Published: ${book.publishYear}", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            scope.launch {
                val existing = favouritesViewModel.getBookByDetails(book.title, book.author)
                if (existing != null) {
                    favouritesViewModel.deleteBook(existing.id)
                    isFavourite = false
                } else {
                    favouritesViewModel.addBook(
                        Book(
                            id = " ", // will be generated
                            title = book.title,
                            author = book.author,
                            year = book.publishYear.toString(),
                            coverUrl = book.coverUrl ?: "",
                            addedManually = false
                        )
                    )
                    isFavourite = true
                }
            }
        }) {
            Text(if (isFavourite) "Remove From Favourites" else "Add To Favourites")
        }
    }
}