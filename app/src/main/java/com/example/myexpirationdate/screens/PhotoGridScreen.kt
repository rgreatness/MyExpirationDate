package com.example.myexpirationdate.screens


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myexpirationdate.models.Photo
import com.example.myexpirationdate.viewmodels.CameraVM
import kotlinx.coroutines.launch
import coil3.compose.AsyncImage

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PhotoGridScreen(photos: List<Photo>, modifier: Modifier = Modifier) {
    val cameraVM = viewModel<CameraVM>()
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        if (photos.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No photos taken yet.")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(photos) { photo ->

                    Card(
                        modifier = Modifier.padding(4.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = photo.imagePath,
                                contentDescription = "Captured photo: ${photo.name}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f),
                                contentScale = ContentScale.Crop
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = photo.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Expiration date here",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                coroutineScope.launch {
                    cameraVM.clearAllPhotos()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = photos.isNotEmpty()
        ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Delete All Photos")
        }
    }
}
