package com.example.pocketlibrary

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun DetailedFavourites(){
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Button on the left

            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "back button",
                modifier = Modifier
                    .size(40.dp)
                    .clickable {  }
            )


            // Spacer pushes the next item to the right
            Spacer(modifier = Modifier.weight(1f))

            // Add Icon on the right
            Image(
                painter = painterResource(id = R.drawable.share),
                contentDescription = "share button",
                modifier = Modifier
                    .size(50.dp)
                    .clickable { /* handle click */ }
            )



        }
        Image(
            painter = painterResource(id = R.drawable.favourites),
            contentDescription = "favourites button",
            modifier = Modifier
                .size(50.dp)
                .clickable { /* handle click */ }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    text = "Title: The Art of Kotlin",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Author: Jaeden Mah",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Year: 2025",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }


    }
    }
}