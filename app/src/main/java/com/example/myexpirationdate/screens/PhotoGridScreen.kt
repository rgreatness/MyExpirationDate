import kotlinx.coroutines.launch
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream

@Composable
fun PhotoGridScreen(photos: List<com.example.myexpirationdate.models.Photo>, modifier: Modifier = Modifier) {
    val cameraVM = viewModel<com.example.myexpirationdate.viewmodels.CameraVM>()
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
                    val bitmapState = produceState<Bitmap?>(initialValue = null, photo.imagePath) {
                        val path = photo.imagePath
                        value = try {
                            if (path?.startsWith("http", ignoreCase = true) == true) {
                                withContext(Dispatchers.IO) {
                                    downloadBitmapFromUrl(path)
                                }
                            } else if (path != null) {
                                BitmapFactory.decodeFile(path)
                            } else {
                                null
                            }
                        } catch (_: Exception) {
                            null
                        }
                    }


                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        val bmp = bitmapState.value
                        if (bmp != null) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Captured photo: ${photo.name}",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("Image not available" )
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

private fun downloadBitmapFromUrl(url: String): Bitmap? {
    return try {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) return null
            val stream: InputStream? = resp.body?.byteStream()
            stream?.use {
                BitmapFactory.decodeStream(it)
            }
        }
    } catch (e: Exception) {
        null
    }
}
