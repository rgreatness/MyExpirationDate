import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myexpirationdate.TAG
import com.example.myexpirationdate.viewmodels.CameraVM
import com.example.myexpirationdate.viewmodels.OpenAiVM

@Composable
fun HomeScreen(cameraVM: CameraVM, openAiVM: OpenAiVM = viewModel(), modifier: Modifier = Modifier) {
    val analysisResult by openAiVM.analysisResult.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Take a Photo to find its Expiration Date")

        Spacer(modifier = Modifier.height(20.dp))

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            if (bitmap != null) {
                cameraVM.onTakePhoto(bitmap)
                openAiVM.analyzeImage(bitmap)
            } else {
                Log.e(TAG, "No bitmap returned from camera intent")
            }
        }

        val takePictureIntent = remember { Intent(MediaStore.ACTION_IMAGE_CAPTURE) }
        Button(onClick = { launcher.launch(takePictureIntent) }) {
            Text("Open Camera")
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (analysisResult.isNotEmpty()) {
            Text(
                text = analysisResult,
                style = typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }

    }
}



