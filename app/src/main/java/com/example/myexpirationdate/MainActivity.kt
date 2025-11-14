package com.example.myexpirationdate

import HomeScreen
import PhotoGridScreen
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myexpirationdate.models.Photo
import com.example.myexpirationdate.ui.theme.MyExpirationDateTheme
import com.example.myexpirationdate.viewmodels.CameraVM
import kotlinx.coroutines.launch

val TAG = "MY_TAG"

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(this, CAMERAX_PERMISSIONS, 0)
        }
        enableEdgeToEdge()
        setContent {
            MyExpirationDateTheme {
                val navController = rememberNavController()
                val cameraVM = viewModel<CameraVM>()
                val photos by cameraVM.photos.collectAsState()

                NavHost(navController = navController, startDestination = "main_screen") {
                    composable("main_screen") {
                        MainScreen(
                            photos = photos,
                            onTakePhotoClick = { navController.navigate("camera_screen") }
                        )
                    }
                    composable("camera_screen") {
                        CameraView(
                            context = applicationContext,
                            cameraVM = cameraVM,
                            onPhotoTaken = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun CameraView(context: Context, cameraVM: CameraVM, onPhotoTaken: () -> Unit) {
        val lifecycleOwner = LocalLifecycleOwner.current
        val controller = remember {
            LifecycleCameraController(context).apply {
                setEnabledUseCases(
                    CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE
                )
            }
        }

        DisposableEffect(lifecycleOwner) {
            controller.bindToLifecycle(lifecycleOwner)
            onDispose {}
        }

        Box(modifier = Modifier.fillMaxSize()) {
            CameraPreview(controller, modifier = Modifier.fillMaxSize())

            IconButton(
                onClick = {
                    controller.cameraSelector =
                        if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else CameraSelector.DEFAULT_BACK_CAMERA
                },
                modifier = Modifier
                    .offset(32.dp, 32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Switch Camera"
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(
                    onClick = {
                        takePhoto(
                            controller = controller,
                            onPhotoTaken = { bitmap ->
                                cameraVM.onTakePhoto(bitmap)
                                onPhotoTaken()
                            }
                        )
                        Log.i(TAG, "Click on take Photo")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Take photo"
                    )
                }
            }
        }
    }

    @Composable
    fun CameraPreview(
        controller: LifecycleCameraController,
        modifier: Modifier = Modifier
    ) {
        val lifecycleOwner = LocalLifecycleOwner.current
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    this.scaleType = PreviewView.ScaleType.FILL_CENTER
                    this.controller = controller
                }
            },
            modifier = modifier
        )
    }

    private fun takePhoto(
        controller: LifecycleCameraController,
        onPhotoTaken: (Bitmap) -> Unit
    ) {
        controller.takePicture(
            ContextCompat.getMainExecutor(applicationContext),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    try {
                        val originalBitmap = imageProxyToBitmap(image)
                        if (originalBitmap != null) {
                            val rotationDegrees = image.imageInfo.rotationDegrees
                            val matrix = Matrix().apply {
                                if (controller.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                                    preScale(-1f, 1f)
                                }
                                postRotate(rotationDegrees.toFloat())
                            }
                            val rotatedBitmap = Bitmap.createBitmap(
                                originalBitmap,
                                0,
                                0,
                                originalBitmap.width,
                                originalBitmap.height,
                                matrix,
                                true
                            )
                            onPhotoTaken(rotatedBitmap)
                        } else {
                            Log.e(TAG, "Failed to convert ImageProxy to Bitmap")
                        }
                    } catch (exc: Exception) {
                        Log.e(TAG, "Error processing captured image", exc)
                    } finally {
                        image.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Camera", "Couldn't take photo: ", exception)
                }
            }
        )
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        return try {
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "imageProxyToBitmap failed", e)
            null
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private val CAMERAX_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )
    }
}

@Composable
fun MainScreen(photos: List<Photo>, onTakePhotoClick: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Home", "Photos")

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = if (index == 0) Icons.Default.Home else Icons.Default.PhotoLibrary,
                                contentDescription = title
                            )
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> HomeScreen(onTakePhotoClick = onTakePhotoClick)
                1 -> PhotoGridScreen(photos = photos)
            }
        }
    }
}
