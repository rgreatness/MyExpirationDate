package com.example.myexpirationdate.screens


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myexpirationdate.models.Photo
import com.example.myexpirationdate.viewmodels.CameraVM
import kotlinx.coroutines.launch
import coil3.compose.AsyncImage
import com.example.myexpirationdate.models.ParsedItem

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PhotoScreen(photos: List<Photo>, modifier: Modifier = Modifier) {
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
            LazyColumn (
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(photos) { photo ->

                    val parsed = ParsedItem(
                        name = photo.name,
                        months = photo.acceptableXdate.months,
                        days = photo.acceptableXdate.days,
                        isDonatable = photo.isDonatable,
                    )

                    var rotated by remember{mutableStateOf(false)}

                    val rotation by animateFloatAsState(
                        targetValue = if (rotated) 180f else 0f,
                        animationSpec = tween(500)
                    )

                    val animateFront by animateFloatAsState(
                        targetValue = if (!rotated) 1f else 0f,
                        animationSpec = tween(500)
                    )

                    val animateBack by animateFloatAsState(
                        targetValue = if (rotated) 1f else 0f,
                        animationSpec = tween(500)
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Card(
                            modifier = Modifier
                                .padding(4.dp)
                                .graphicsLayer{
                                    rotationY = rotation
                                    cameraDistance = 8 * density }
                                .clickable{
                                   rotated = !rotated
                                },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            if (rotated){
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .graphicsLayer{
                                            alpha = if (rotated) animateBack else animateFront
                                            rotationY = rotation
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Text(
                                        text = parsed.name,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 10.dp),
                                        style = typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    HorizontalDivider()

                                    Spacer(modifier = Modifier.height(30.dp))

                                    Text(
                                        text = cameraVM.formatMonthsToYearsMonthsDays(parsed.months, parsed.days),
                                        style = typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Text("Donatable", style = typography.titleMedium)
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(if (parsed.isDonatable) "Yes" else "No") },
                                            leadingIcon = {
                                                Icon(
                                                    if (parsed.isDonatable) Icons.Default.Check
                                                    else Icons.Default.Close,
                                                    contentDescription = null
                                                )
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(190.dp))

                                }
                            }else{
                                Column(modifier = Modifier.fillMaxWidth().graphicsLayer{
                                    alpha = if (rotated) animateBack else animateFront
                                    rotationY = rotation
                                }) {
                                    AsyncImage(
                                        model = photo.imagePath,
                                        contentDescription = "Captured photo: ${photo.name}",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f),
                                        contentScale = ContentScale.Crop
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    cameraVM.clearOnePhoto(photo)
                                                }
                                            }
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                                        }
                                    }
                                }
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
