package com.example.pocketlibrary
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.constraintlayout.compose.ConstraintLayout

@Composable
fun MainScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Button on the left

            Image(
                painter = painterResource(id = R.drawable.search),
                contentDescription = "search button",
                modifier = Modifier
                    .size(40.dp)
                    .clickable {  }
            )


            // Spacer pushes the next item to the right
            Spacer(modifier = Modifier.weight(1f))

            // Add Icon on the right
            Image(
                painter = painterResource(id = R.drawable.add),
                contentDescription = "add button",
                modifier = Modifier
                    .size(50.dp)
                    .clickable { /* handle click */ }
            )
        }

        Text(
            text = "Pocket Library",
            fontSize = 35.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

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
                state.results.isEmpty() && state.query.isNotEmpty() -> {
                    Text("No results", modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(state.results) { item ->
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(8.dp)) {
                                    AsyncImage(
                                        model = item.hit.webUrl,
                                        contentDescription = item.hit.tags,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp) // constrain height
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Title: ${item.post.title}", // replace with hit.title or hit.body if available
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Description: ${item.post.body}", // replace with hit.title or hit.body if available
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(Modifier.fillMaxWidth()){
                                        Text(
                                            text = "${item.hit.likes} Likes | ${item.hit.comments} Commentsc| ${item.hit.views} Views | ${item.hit.downloads} Downloads",
                                            style = MaterialTheme.typography.titleMedium
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
